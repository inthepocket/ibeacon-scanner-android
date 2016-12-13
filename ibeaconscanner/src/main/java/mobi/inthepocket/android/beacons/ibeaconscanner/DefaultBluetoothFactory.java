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
    private BluetoothAdapter bluetoothAdapter;

    /**
     * Attaches the {@link #bluetoothAdapter} if it is null.
     *
     * @return true if the {@link BluetoothAdapter} and {@link BluetoothLeScanner} are available
     */
    @Override
    public boolean canAttachBluetoothAdapter()
    {
        // try to get the BluetoothAdapter
        // apps running in a Samsung Knox container will crash with a SecurityException
        if (this.bluetoothAdapter == null)
        {
            try
            {
                this.bluetoothAdapter = this.getBluetoothAdapter();
            }
            catch (final SecurityException securityException)
            {
                return false;
            }
        }

        // try to get the BluetoothLeScanner
        // apps without Bluetooth permission will crash with a SecurityException
        try
        {
            this.bluetoothAdapter.getBluetoothLeScanner();
        }
        catch (final SecurityException securityException)
        {
            return false;
        }

        return true;
    }

    /**
     * @return the {@link BluetoothLeScanner}
     */
    @Override
    @Nullable
    public BluetoothLeScanner getBluetoothLeScanner()
    {
        return this.canAttachBluetoothAdapter() ? this.bluetoothAdapter.getBluetoothLeScanner() : null;
    }

    /**
     * @return {@link BluetoothAdapter#getDefaultAdapter()}
     */
    public BluetoothAdapter getBluetoothAdapter()
    {
        return BluetoothAdapter.getDefaultAdapter();
    }
}
