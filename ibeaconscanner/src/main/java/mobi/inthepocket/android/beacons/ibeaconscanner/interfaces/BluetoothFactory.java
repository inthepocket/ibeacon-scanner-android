package mobi.inthepocket.android.beacons.ibeaconscanner.interfaces;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.os.Build;
import android.support.annotation.Nullable;

/**
 * BluetoothFactory is responsible for managing the {@link BluetoothLeScanner} used.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public interface BluetoothFactory
{
    /**
     * Attaches the {@link BluetoothAdapter} if it is null.
     *
     * @return true if the {@link BluetoothAdapter} and {@link BluetoothLeScanner} are available
     */
    boolean canAttachBluetoothAdapter();

    /**
     * @return a {@link BluetoothLeScanner}
     */
    @Nullable
    BluetoothLeScanner getBluetoothLeScanner();
}
