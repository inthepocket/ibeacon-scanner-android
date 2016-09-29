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

    private final ScannerScanCallback scannerScanCallback;
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothLeScanner bluetoothLeScanner;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RegionManager(@NonNull final Context context, @NonNull final Callback callback) // todo get Application Context with an Init method
    {
        final ContentResolver contentResolver = context.getApplicationContext().getContentResolver();

        this.scannerScanCallback = new ScannerScanCallback(contentResolver, callback, BuildConfig.BEACON_EXIT_TIME_IN_MILLIS);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothLeScanner = this.bluetoothAdapter.getBluetoothLeScanner();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startMonitoring(@NonNull final Region region)
    {
        final ScanFilter scanFilter = ScanFilterUtils.getScanFilter(region);

        this.bluetoothLeScanner.startScan(Arrays.asList(scanFilter), getScanSettings(), this.scannerScanCallback);
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
