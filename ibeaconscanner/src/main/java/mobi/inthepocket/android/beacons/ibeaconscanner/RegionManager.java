package mobi.inthepocket.android.beacons.ibeaconscanner;

/**
 * Created by eliaslecomte on 23/09/2016.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import java.lang.*;
import java.util.Arrays;
import java.util.UUID;

import mobi.inthepocket.android.beacons.ibeaconscanner.utils.ConversionUtils;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.ScanFilterUtils;

/**
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RegionManager
{
    private final static String TAG = RegionManager.class.getSimpleName();

    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothLeScanner bluetoothLeScanner;

    private Callback callback;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RegionManager()
    {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothLeScanner = this.bluetoothAdapter.getBluetoothLeScanner();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startMonitoring(@NonNull final Region region, @NonNull final Callback callback)
    {
        this.callback = callback;

        final ScanFilter scanFilter = ScanFilterUtils.getScanFilter(region);

        this.bluetoothLeScanner.startScan(Arrays.asList(scanFilter), getScanSettings(), this.scanCallback);
    }

    //region ScanCallback


    private final ScanCallback scanCallback = new ScanCallback()
    {
        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void onScanResult(final int callbackType, final ScanResult scanResult)
        {
            Log.d(TAG, "Found a beacon: " + scanResult.toString());

            super.onScanResult(callbackType, scanResult);

            // get Scan Record byte array (Be warned, this can be null)
            if (scanResult.getScanRecord() != null)
            {
                final byte[] scanRecord = scanResult.getScanRecord().getBytes();

                // get Rssi of device
                final int rssi = scanResult.getRssi();

                int startByte = 2;
                boolean patternFound = false;
                while (startByte <= 5)
                {
                    if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //identifies an iBeacon
                            ((int) scanRecord[startByte + 3] & 0xff) == 0x15)
                    { //Identifies correct data length
                        patternFound = true;
                        break;
                    }
                    startByte++;
                }

                if (patternFound)
                {
                    // get the UUID from the hex result
                    final byte[] uuidBytes = new byte[16];
                    System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                    final UUID uuid = ConversionUtils.bytesToUuid(uuidBytes);

                    // get the major from hex result
                    final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

                    // get the minor from hex result
                    final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

                    if (RegionManager.this.callback != null)
                    {
                        final Region region = new Region.Builder()
                                .setUUID(uuid)
                                .setMajor(major)
                                .setMinor(minor)
                                .build();

                        RegionManager.this.callback.didEnterRegion(region);
                    }
                }
            }
        }
    };

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

    //region Callback interface

    public interface Callback
    {
        void didEnterRegion(Region region);
        void didExitRegion(Region region);
        void monitoringDidFail(Error error);
    }

    //endregion
}
