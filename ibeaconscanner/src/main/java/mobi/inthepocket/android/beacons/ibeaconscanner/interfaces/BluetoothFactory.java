package mobi.inthepocket.android.beacons.ibeaconscanner.interfaces;

import android.annotation.TargetApi;
import android.bluetooth.le.BluetoothLeScanner;
import android.os.Build;

/**
 * BluetoothFactory is responsible for managing the {@link BluetoothLeScanner} used.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public interface BluetoothFactory
{
    /**
     * Create a {@link BluetoothLeScanner}. Throws a {@link SecurityException} when the bluetooth permission is not granted.
     */
    void createBluetoothLeScanner();

    /**
     * @return a {@link BluetoothLeScanner}
     */
    BluetoothLeScanner getBluetoothLeScanner();
}
