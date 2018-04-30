package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.util.Log;

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
    public static final String TAG = DefaultScanService.class.getSimpleName();
    private final Context context;

    private final BluetoothFactory bluetoothFactory;
    private final BackportScanCallback backportScanCallback;
    private final PendingIntent scannerPendingIntent;
    private final BeaconsSeenProvider beaconsSeenProvider;
    private final Class<?> targetService;

    private final AddBeaconsHandler addBeaconsHandler;
    private final Object timeoutObject;
    private final Set<Beacon> beacons;

    private DefaultScanService(@NonNull final Initializer initializer)
    {
        this.context = initializer.context;
        this.beaconsSeenProvider = new BeaconsSeenProvider(initializer.context);
        this.backportScanCallback = new BackportScanCallback(initializer.context, initializer.targetService, initializer.exitTimeoutInMillis);
        this.scannerPendingIntent = this.createOreoScanCallbackIntent(initializer.targetService, initializer.exitTimeoutInMillis);
        this.targetService = initializer.targetService;

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
        this.beaconsSeenProvider.destroy();
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

            this.sendErrorIntent(Error.NO_BLUETOOTH_PERMISSION);
        }
        else
        {
            if (!BluetoothUtils.hasBluetoothLE(this.context))
            {
                canScan = false;

                this.sendErrorIntent(Error.NO_BLUETOOTH_LE);
            }

            if (!BluetoothUtils.isBluetoothOn())
            {
                canScan = false;

                this.sendErrorIntent(Error.BLUETOOTH_OFF);
            }
        }

        if (!LocationUtils.isLocationOn(this.context))
        {
            canScan = false;

            this.sendErrorIntent(Error.LOCATION_OFF);
        }

        if (!PermissionUtils.isLocationGranted(this.context))
        {
            canScan = false;

            this.sendErrorIntent(Error.NO_LOCATION_PERMISSION);
        }

        if (canScan && this.bluetoothFactory.getBluetoothLeScanner() != null)
        {
            // stop scanning
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                this.bluetoothFactory.getBluetoothLeScanner().stopScan(this.scannerPendingIntent);
            }
            else
            {
                this.bluetoothFactory.getBluetoothLeScanner().stopScan(this.backportScanCallback);
            }

            // start scanning
            if (!this.beacons.isEmpty())
            {
                final List<ScanFilter> scanFilters = new ArrayList<>();

                for (final Beacon beacon : this.beacons)
                {
                    scanFilters.add(ScanFilterUtils.getScanFilter(beacon));
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    final int result = this.bluetoothFactory.getBluetoothLeScanner().startScan(scanFilters, getScanSettings(), this.scannerPendingIntent);
                    if (result != 0)
                    {
                        Log.e(TAG, "Failed to start background scan on Android O. Code: " + result);
                    }
                }
                else
                {
                    this.bluetoothFactory.getBluetoothLeScanner().startScan(scanFilters, getScanSettings(), this.backportScanCallback);
                }
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

    /**
     * Low power scan results in the background will be delivered via a PendingIntent.
     *
     * @param targetService       target service to launch when beacons have been entered or exited
     * @param exitTimeoutInMillis beacon exit timeout in ms
     */
    @TargetApi(Build.VERSION_CODES.O)
    private PendingIntent createOreoScanCallbackIntent(final Class<?> targetService, final long exitTimeoutInMillis)
    {
        final Intent intent = new Intent(this.context, BluetoothScanBroadcastReceiver.class);
        intent.putExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_LAUNCH_SERVICE_CLASS_NAME, targetService.getName());
        intent.putExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_EXITED_TIMEOUT_MS, exitTimeoutInMillis);
        return PendingIntent.getBroadcast(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void sendErrorIntent(Error error)
    {
        final Intent intent = new Intent(this.context, this.targetService);
        intent.putExtra(BluetoothScanBroadcastReceiver.ERROR_CODE, error);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try
        {
            pendingIntent.send();
        }
        catch (final PendingIntent.CanceledException e)
        {
            Log.e(TAG, "Sending Broadcast intent was not possible: " + e.getMessage());
        }
    }

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
        private Class<?> targetService;

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
         * Define the service to launch on Android O and later when a beacon update needs to be communicated.
         * Required for {@link Build.VERSION_CODES#O} and later.
         *
         * @param targetService to launch
         */
        public Initializer setTargetService(final Class<?> targetService)
        {
            this.targetService = targetService;

            return this;
        }

        /**
         * Returns the {@link DefaultScanService.Initializer} and validates if the configuration is valid and sets default values.
         *
         * @throws IllegalArgumentException when no target service has been declared for implementations running on {@link Build.VERSION_CODES#O} and later.
         * @return {@link DefaultScanService.Initializer}
         */
        public DefaultScanService build() throws IllegalArgumentException
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this.targetService == null)
            {
                throw new IllegalArgumentException("You need to define a target service for the iBeaconScanner library to publish beacon activity to.");
            }

            return new DefaultScanService(this);
        }
    }

    //endregion
}
