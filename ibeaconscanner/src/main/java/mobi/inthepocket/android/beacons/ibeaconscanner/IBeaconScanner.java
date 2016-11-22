package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

/**
 * Initialization and configuration entry point for iBeacon Scanner Android.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class IBeaconScanner
{
    private static IBeaconScanner instance;

    private final ScanService scanService;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private IBeaconScanner(@NonNull final ScanService scanService)
    {
        this.scanService = scanService;
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
     * @param initializer you pass a {@link Context} object and can modify settings like the exit timeout ({@link DefaultScanService.Initializer#setExitTimeoutInMillis(long)})
     */
    public static void initialize(@NonNull final DefaultScanService.Initializer initializer)
    {
        instance = new IBeaconScanner(DefaultScanService.initialize(initializer));
    }

    /**
     * Initialize {@link IBeaconScanner} with {@code scanService}. Makes creating instrumentation tests that run on emulators easier.
     *
     * @param scanService to create the IBeaconScanner with
     */
    public static void initialize(@NonNull final ScanService scanService)
    {
        instance = new IBeaconScanner(scanService);
    }

    //region ScanService

    /**
     * @see ScanService#startMonitoring(Beacon)
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void startMonitoring(@NonNull final Beacon beacon)
    {
        this.scanService.startMonitoring(beacon);
    }

    /**
     * @see ScanService#stopMonitoring(Beacon)
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void stopMonitoring(@NonNull final Beacon beacon)
    {
        this.scanService.stopMonitoring(beacon);
    }

    /**
     * @see ScanService#start()
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void start()
    {
        this.scanService.start();
    }

    /**
     * @see ScanService#stop()
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public void stop()
    {
        this.scanService.stop();
    }

    /**
     * @see ScanService#setCallback(Callback)
     */
    public void setCallback(@NonNull final Callback callback)
    {
        this.scanService.setCallback(callback);
    }

    //endregion

    //region Properties

    /**
     * @param context to create {@link IBeaconScanner} with
     * @return new {@link DefaultScanService.Initializer}
     */
    public static DefaultScanService.Initializer newInitializer(@NonNull final Context context)
    {
        return DefaultScanService.newInitializer(context);
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
         * @param beacon device is in range of
         */
        void didEnterBeacon(Beacon beacon);

        /**
         * Device is outside the range of {@code beacon}.
         *
         * @param beacon device is no longer in range of
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
