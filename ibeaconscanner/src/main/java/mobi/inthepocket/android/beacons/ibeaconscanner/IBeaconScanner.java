package mobi.inthepocket.android.beacons.ibeaconscanner;

/**
 *  Initialization and configuration entry point for iBeacon Scanner Android.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mobi.inthepocket.android.beacons.ibeaconscanner.database.BeaconsSeenProvider;
import mobi.inthepocket.android.beacons.ibeaconscanner.handlers.AddBeaconsHandler;
import mobi.inthepocket.android.beacons.ibeaconscanner.handlers.TimeoutHandler;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.BluetoothUtils;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.LocationUtils;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.PermissionUtils;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.ScanFilterUtils;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class IBeaconScanner implements TimeoutHandler.TimeoutCallback<Object>
{
    private static IBeaconScanner instance;

    private final Context context;
    private BluetoothLeScanner bluetoothLeScanner;
    private final ScannerScanCallback scannerScanCallback;
    private Callback callback;

    private final AddBeaconsHandler addBeaconsHandler;
    private final Object timeoutObject;
    private final Set<Beacon> beacons;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private IBeaconScanner(@NonNull final Initializer initializer)
    {
        final BeaconsSeenProvider beaconsSeenProvider = new BeaconsSeenProvider(initializer.context);

        this.context = initializer.context;
        this.scannerScanCallback = new ScannerScanCallback(beaconsSeenProvider, initializer.exitTimeoutInMillis);
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        this.addBeaconsHandler = new AddBeaconsHandler(this, initializer.addBeaconTimeoutInMillis);
        this.timeoutObject = new Object();
        this.beacons = new HashSet<>();
    }

    public static IBeaconScanner getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("You need to initialize IBeaconScanner first in your Application class or in your Service onCreate");
        }

        return instance;
    }

    /**
     * Initialize {@link IBeaconScanner} with {@code initializer}.
     *
     * @param initializer You pass a {@link Context} object and can modify settings like the exit timeout ({@link Initializer#setExitTimeoutInMillis(long)})
     */
    public static void initialize(@NonNull final Initializer initializer)
    {
        instance = new IBeaconScanner(initializer);
    }

    /**
     * Start monitoring {@code beacon} and all previously added {@link Beacon}s.
     *
     * @param beacon to start monitoring
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void startMonitoring(@NonNull final Beacon beacon)
    {
        this.beacons.add(beacon);
        this.addBeaconsHandler.passItem(this.timeoutObject);
    }

    /**
     * Stop monitoring for {@code beacon}. Keep monitoring for other {@link Beacon}s that were added with
     * {@link #startMonitoring(Beacon)}.
     *
     * @param beacon to stop monitoring
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void stopMonitoring(@NonNull final Beacon beacon)
    {
        this.beacons.remove(beacon);
        this.addBeaconsHandler.passItem(this.timeoutObject);
    }

    /**
     * Starts monitoring for previously added {@link Beacon}s.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void start()
    {
        this.addBeaconsHandler.passItem(this.timeoutObject);
    }

    /**
     * Stops monitoring and removes all monitored {@link Beacon}s.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void stop()
    {
        this.beacons.clear();
        this.addBeaconsHandler.passItem(this.timeoutObject);
    }

    //region Properties

    /**
     * @param context to create {@link IBeaconScanner} with
     * @return new {@link Initializer}
     */
    public static Initializer newInitializer(@NonNull final Context context)
    {
        return new Initializer(context);
    }

    /**
     * Set the {@link Callback} that will get notified for {@link Beacon} enters, exits or if an {@link Error}
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
        // no new beacons have been added or removed for {@link BuildConfig#ADD_REGION_TIMEOUT_IN_MILLIS}, stop
        // previous scan and start a new scan with the beacons we should monitor.

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
            this.bluetoothLeScanner.stopScan(IBeaconScanner.this.scannerScanCallback);

            // start scanning
            if (!this.beacons.isEmpty())
            {
                final List<ScanFilter> scanFilters = new ArrayList<>();

                for (final Beacon beacon : this.beacons)
                {
                    scanFilters.add(ScanFilterUtils.getScanFilter(beacon));
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
        private long addBeaconTimeoutInMillis;

        private Initializer(@NonNull final Context context)
        {
            this.context = context.getApplicationContext();
        }

        /**
         * After {@code exitTimeoutInMillis}, when a beacon is not seen, the exit callback:
         * {@link Callback#didExitBeacon(Beacon)} is called.
         *
         * @param exitTimeoutInMillis
         * @return {@link Initializer}
         */
        public Initializer setExitTimeoutInMillis(final long exitTimeoutInMillis)
        {
            this.exitTimeoutInMillis = exitTimeoutInMillis;

            return this;
        }

        /**
         * Everytime you start or stop monitoring a {@link Beacon}, we wait {@code addBeaconTimeoutInMillis} before
         * changes are applied. If you add several {@link Beacon}s, this will evade that scans are stop-started everytime.
         * Starting from Android N, if you start a scan more than 5 times in 30 seconds, scans are blocked.
         *
         * @param addBeaconTimeoutInMillis
         * @return {@link Initializer}
         */
        public Initializer setAddBeaconTimeoutInMillis(final long addBeaconTimeoutInMillis)
        {
            this.addBeaconTimeoutInMillis = addBeaconTimeoutInMillis;

            return this;
        }

        /**
         * Returns the {@link Initializer} and validates if the configuration is valid and sets default values.
         *
         * @return {@link Initializer}
         */
        public Initializer build()
        {
            if (this.exitTimeoutInMillis == 0)
            {
                this.exitTimeoutInMillis = BuildConfig.BEACON_EXIT_TIME_IN_MILLIS;
            }

            if (this.addBeaconTimeoutInMillis == 0)
            {
                this.addBeaconTimeoutInMillis = BuildConfig.ADD_BEACON_TIMEOUT_IN_MILLIS;
            }

            return this;
        }
    }

    //endregion

    //region Callback

    /**
     * Callback object that notifies of {@link Beacon} enter, exits or moinotiring fails.
     */
    public interface Callback
    {
        /**
         * Device is inside the range of {@code beacon}.
         *
         * @param beacon
         */
        void didEnterBeacon(Beacon beacon);

        /**
         * Device is outside the range of {@code beacon}.
         *
         * @param beacon
         */
        void didExitBeacon(Beacon beacon);

        /**
         * Monitoring could not start due to {@code error}.
         *
         * @param error that happened
         */
        void monitoringDidFail(Error error);
    }

    //endregion
}
