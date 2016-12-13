package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.Manifest;
import android.annotation.TargetApi;
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
import mobi.inthepocket.android.beacons.ibeaconscanner.interfaces.BluetoothFactory;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.BluetoothUtils;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.LocationUtils;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.PermissionUtils;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.ScanFilterUtils;

/**
 * A {@link ScanService} will monitor for the {@link Beacon}s passed via {@link #startMonitoring(Beacon)},
 * notify you when you are in range of a monitored {@link Beacon} and persist information in a database so
 * that it continues to work after a service restart.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
final class DefaultScanService implements ScanService, TimeoutHandler.TimeoutCallback<Object>
{
    private final Context context;
    private final BluetoothFactory bluetoothFactory;
    private final ScannerScanCallback scannerScanCallback;

    private IBeaconScanner.Callback callback;

    private final AddBeaconsHandler addBeaconsHandler;
    private final Object timeoutObject;
    private final Set<Beacon> beacons;

    private DefaultScanService(@NonNull final Initializer initializer)
    {
        final BeaconsSeenProvider beaconsSeenProvider = new BeaconsSeenProvider(initializer.context);

        this.context = initializer.context;
        this.scannerScanCallback = new ScannerScanCallback(beaconsSeenProvider, initializer.exitTimeoutInMillis);

        this.bluetoothFactory = initializer.bluetoothFactory;

        this.addBeaconsHandler = new AddBeaconsHandler(this, initializer.addBeaconTimeoutInMillis);
        this.timeoutObject = new Object();
        this.beacons = new HashSet<>();
    }

    static DefaultScanService initialize(@NonNull final DefaultScanService.Initializer initializer)
    {
        return new DefaultScanService(initializer);
    }

    //region ScanService

    /**
     * @see ScanService#startMonitoring(Beacon)
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    @Override
    public void startMonitoring(@NonNull final Beacon beacon)
    {
        this.beacons.add(beacon);
        this.addBeaconsHandler.passItem(this.timeoutObject);
    }

    /**
     * @see ScanService#stopMonitoring(Beacon)
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    @Override
    public void stopMonitoring(@NonNull final Beacon beacon)
    {
        this.beacons.remove(beacon);
        this.addBeaconsHandler.passItem(this.timeoutObject);
    }

    /**
     * @see ScanService#start()
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    @Override
    public void start()
    {
        this.addBeaconsHandler.passItem(this.timeoutObject);
    }

    /**
     * @see ScanService#stop()
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    @Override
    public void stop()
    {
        this.beacons.clear();
        this.addBeaconsHandler.passItem(this.timeoutObject);
    }

    /**
     * @see ScanService#setCallback(IBeaconScanner.Callback)
     */
    @Override
    public void setCallback(@NonNull final IBeaconScanner.Callback callback)
    {
        this.callback = callback;
        this.scannerScanCallback.setCallback(callback);
    }

    //endregion

    //region TimeoutHandler.TimeoutCallback<Void>

    @Override
    public void timedOut(final Object anObject)
    {
        // no new beacons have been added or removed for {@link BuildConfig#ADD_REGION_TIMEOUT_IN_MILLIS}, stop
        // previous scan and start a new scan with the beacons we should monitor.

        // check if we can scan
        boolean canScan = true;

        // reattach the BluetoothAdapter
        if (!this.bluetoothFactory.canAttachBluetoothAdapter())
        {
            canScan = false;

            if (this.callback != null)
            {
                this.callback.monitoringDidFail(Error.NO_BLUETOOTH_PERMISSION);
            }
        }
        else
        {
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

        if (canScan && this.bluetoothFactory.getBluetoothLeScanner() != null)
        {
            // stop scanning
            this.bluetoothFactory.getBluetoothLeScanner().stopScan(DefaultScanService.this.scannerScanCallback);

            // start scanning
            if (!this.beacons.isEmpty())
            {
                final List<ScanFilter> scanFilters = new ArrayList<>();

                for (final Beacon beacon : this.beacons)
                {
                    scanFilters.add(ScanFilterUtils.getScanFilter(beacon));
                }

                this.bluetoothFactory.getBluetoothLeScanner().startScan(scanFilters, getScanSettings(), this.scannerScanCallback);
            }
        }
    }

    //endregion

    //region Properties

    /**
     * @param context to create {@link IBeaconScanner} with
     * @return new {@link Initializer}
     */
    static Initializer newInitializer(@NonNull final Context context)
    {
        return new Initializer(context);
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

    //endregion

    //region Initializer

    /**
     * Builder class to create a {@link DefaultScanService}.
     */
    public static final class Initializer
    {
        private final Context context;
        private long exitTimeoutInMillis;
        private long addBeaconTimeoutInMillis;
        private BluetoothFactory bluetoothFactory;

        private Initializer(@NonNull final Context context)
        {
            this.context = context.getApplicationContext();
        }

        /**
         * After {@code exitTimeoutInMillis}, when a beacon is not seen, the exit callback:
         * {@link IBeaconScanner.Callback#didExitBeacon(Beacon)} is called.
         *
         * @param exitTimeoutInMillis beacon exit timeout in ms
         * @return {@link DefaultScanService.Initializer}
         */
        public Initializer setExitTimeoutInMillis(final long exitTimeoutInMillis)
        {
            this.exitTimeoutInMillis = exitTimeoutInMillis;

            return this;
        }

        /**
         * Every time you start or stop monitoring a {@link Beacon}, we wait {@code addBeaconTimeoutInMillis} before
         * changes are applied. If you add several {@link Beacon}s, this will evade that scans are stop-started every time.
         * Starting from Android N, if you start a scan more than 5 times in 30 seconds, scans are blocked.
         *
         * @param addBeaconTimeoutInMillis add beacon timeout in ms
         * @return {@link DefaultScanService.Initializer}
         */
        public Initializer setAddBeaconTimeoutInMillis(final long addBeaconTimeoutInMillis)
        {
            this.addBeaconTimeoutInMillis = addBeaconTimeoutInMillis;

            return this;
        }

        /**
         * Additionally you can set a {@link BluetoothFactory} responsible for creating a {@link android.bluetooth.le.BluetoothLeScanner}.
         *
         * @param bluetoothFactory to use
         * @return {@link DefaultScanService.Initializer}
         */
        public Initializer setBluetoothFactory(final BluetoothFactory bluetoothFactory)
        {
            this.bluetoothFactory = bluetoothFactory;

            return this;
        }

        /**
         * Returns the {@link DefaultScanService.Initializer} and validates if the configuration is valid and sets default values.
         *
         * @return {@link DefaultScanService.Initializer}
         */
        public DefaultScanService build()
        {
            if (this.exitTimeoutInMillis == 0)
            {
                this.exitTimeoutInMillis = BuildConfig.BEACON_EXIT_TIME_IN_MILLIS;
            }

            if (this.addBeaconTimeoutInMillis == 0)
            {
                this.addBeaconTimeoutInMillis = BuildConfig.ADD_BEACON_TIMEOUT_IN_MILLIS;
            }

            if (this.bluetoothFactory == null)
            {
                this.bluetoothFactory = new DefaultBluetoothFactory();
            }

            return new DefaultScanService(this);
        }
    }

    //endregion
}
