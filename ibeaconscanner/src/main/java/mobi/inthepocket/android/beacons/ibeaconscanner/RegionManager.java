package mobi.inthepocket.android.beacons.ibeaconscanner;

/**
 * Created by eliaslecomte on 23/09/2016.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mobi.inthepocket.android.beacons.ibeaconscanner.handlers.AddRegionsHandler;
import mobi.inthepocket.android.beacons.ibeaconscanner.handlers.TimeoutHandler;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.BluetoothUtils;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.LocationUtils;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.PermissionUtils;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.ScanFilterUtils;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class RegionManager implements TimeoutHandler.TimeoutCallback<Object>
{
    private final static String TAG = RegionManager.class.getSimpleName();

    private static RegionManager instance;

    private final Context context;
    private BluetoothLeScanner bluetoothLeScanner;
    private final ScannerScanCallback scannerScanCallback;
    private Callback callback;

    private final AddRegionsHandler addRegionsHandler;
    private final Object timeoutObject;
    private final Set<Region> regions;

    public static RegionManager getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("You need to initialize RegionManager first in your Application class or in your Service onCreate");
        }

        return instance;
    }

    public static void initialize(@NonNull final Initializer initializer)
    {
        instance = new RegionManager(initializer);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private RegionManager(@NonNull final Initializer initializer)
    {
        final ContentResolver contentResolver = initializer.context.getContentResolver();

        this.context = initializer.context;
        this.scannerScanCallback = new ScannerScanCallback(contentResolver, initializer.exitTimeoutInMillis);
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        this.addRegionsHandler = new AddRegionsHandler(this, BuildConfig.ADD_REGION_TIMEOUT_IN_MILLIS);
        this.timeoutObject = new Object();
        this.regions = new HashSet<>();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void startMonitoring(@NonNull final Region region)
    {
        this.regions.add(region);
        this.addRegionsHandler.passItem(this.timeoutObject);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void stopMonitoring(@NonNull final Region region)
    {
        this.regions.remove(region);
        this.addRegionsHandler.passItem(this.timeoutObject);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void start()
    {
        this.addRegionsHandler.passItem(this.timeoutObject);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void stop()
    {
        this.regions.clear();
        this.addRegionsHandler.passItem(this.timeoutObject);
    }

    //region Properties

    public static Initializer newInitializer(@NonNull final Context context)
    {
        return new Initializer(context);
    }

    /**
     * Set the {@link Callback} that will get notified for {@link Region} enters, exits or if an {@link Error}
     * happened.
     *
     * @param callback
     */
    public void setCallback(@NonNull final Callback callback)
    {
        this.callback = callback;
        this.scannerScanCallback.setCallback(callback);
    }

    //endregion

    //region Helpers

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static ScanSettings getScanSettings()
    {
        final ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setReportDelay(0);
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }

    //region TimeoutHandler.TimeoutCallback<Void>

    @Override
    public void timedOut(final Object anObject)
    {
        // no new regions have been added or removed for {@link BuildConfig#ADD_REGION_TIMEOUT_IN_MILLIS}, stop
        // previous scan and start a new scan with the regions we should monitor.

        // check if we can scan
        boolean canScan = true;

        if (!BluetoothUtils.hasBluetoothLE(this.context))
        {
            canScan = false;

            if (this.callback != null)
            {
                this.callback.monitoringDidFail(Error.NO_BLUETOOTH_LE);
            }
        }

        if (!BluetoothUtils.isBluetoothOn())
        {
            canScan = false;

            if (this.callback != null)
            {
                this.callback.monitoringDidFail(Error.BLUETOOTH_OFF);
            }
        }

        if (!LocationUtils.isLocationOn(this.context))
        {
            canScan = false;

            if (this.callback != null)
            {
                this.callback.monitoringDidFail(Error.LOCATION_OFF);
            }
        }

        if (!PermissionUtils.isLocationGranted(this.context))
        {
            canScan = false;

            if (this.callback != null)
            {
                this.callback.monitoringDidFail(Error.NO_LOCATION_PERMISSION);
            }
        }

        if (canScan)
        {
            // may have to reattach the le scanner
            if (this.bluetoothLeScanner == null)
            {
                final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            }

            // stop scanning
            this.bluetoothLeScanner.stopScan(RegionManager.this.scannerScanCallback);

            // start scanning
            if (this.regions.size() > 0)
            {
                final List<ScanFilter> scanFilters = new ArrayList<>();

                for (final Region region : this.regions)
                {
                    scanFilters.add(ScanFilterUtils.getScanFilter(region));
                }

                this.bluetoothLeScanner.startScan(scanFilters, getScanSettings(), this.scannerScanCallback);
            }
        }
    }

    //endregion

    //region Initializer

    public static final class Initializer
    {
        private final Context context;
        private long exitTimeoutInMillis;

        private Initializer(@NonNull final Context context)
        {
            this.context = context.getApplicationContext();
        }

        public void setExitTimeoutInMillis(final long exitTimeoutInMillis)
        {
            this.exitTimeoutInMillis = exitTimeoutInMillis;
        }

        public Initializer build()
        {
            if (this.exitTimeoutInMillis == 0)
            {
                this.exitTimeoutInMillis = BuildConfig.BEACON_EXIT_TIME_IN_MILLIS;
            }

            return this;
        }
    }

    //endregion

    //region Callback

    public interface Callback
    {
        void didEnterRegion(Region region);

        void didExitRegion(Region region);

        void monitoringDidFail(Error error);
    }

    //endregion
}
