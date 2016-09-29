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

import java.util.Arrays;

import mobi.inthepocket.android.beacons.ibeaconscanner.utils.ScanFilterUtils;

/**
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RegionManager
{
    private final static String TAG = RegionManager.class.getSimpleName();

    private static RegionManager instance;

    private final ScannerScanCallback scannerScanCallback;
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothLeScanner bluetoothLeScanner;

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

        this.scannerScanCallback = new ScannerScanCallback(contentResolver, initializer.exitTimeoutInMillis);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothLeScanner = this.bluetoothAdapter.getBluetoothLeScanner();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startMonitoring(@NonNull final Region region)
    {
        final ScanFilter scanFilter = ScanFilterUtils.getScanFilter(region);
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
        this.scannerScanCallback.setCallback(callback);
    }

    //region Helpers

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static ScanSettings getScanSettings()
    {
        final ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setReportDelay(0);
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }


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

    //region Callback interface

    public interface Callback
    {
        void didEnterRegion(Region region);
        void didExitRegion(Region region);
        void monitoringDidFail(Error error);
    }

    //endregion
}
