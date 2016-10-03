package mobi.inthepocket.android.beacons.ibeaconscanner.utils;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by eliaslecomte on 29/09/2016.
 */

public final class BluetoothUtils
{
    private BluetoothUtils()
    {
    }

    /**
     * Determine weather the device has BLE.
     *
     * @param context  where from you determine if the device has BLE
     * @return True if the device has BLE.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean hasBluetoothLE(final Context context)
    {
        return (context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
    }

    /**
     * Determine if bluetooth is turned on.
     *
     * @return True if bloetuuth is turned on.
     */
    public static boolean isBluetoothOn()
    {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }
}
