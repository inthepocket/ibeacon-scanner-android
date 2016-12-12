package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.os.Build;
import android.support.annotation.Nullable;

import mobi.inthepocket.android.beacons.ibeaconscanner.interfaces.BluetoothFactory;

/**
 * Created by eliaslecomte on 12/12/2016.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class DefaultBluetoothFactory implements BluetoothFactory
{
    private BluetoothLeScanner bluetoothLeScanner;

    /**
     * Creates the {@link #bluetoothLeScanner} if it is null. Throws a {@link SecurityException} when the bluetooth permission is not granted.
     */
    @Override
    public void createBluetoothLeScanner()
    {
        if (this.bluetoothLeScanner == null)
        {

            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
    }

    /**
     * @return the {@link BluetoothLeScanner}
     */
    @Override
    @Nullable
    public BluetoothLeScanner getBluetoothLeScanner()
    {
        return this.bluetoothLeScanner;
    }
}
