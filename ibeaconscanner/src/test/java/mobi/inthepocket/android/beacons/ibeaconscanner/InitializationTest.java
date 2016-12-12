package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import mobi.inthepocket.android.beacons.ibeaconscanner.interfaces.BluetoothFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by eliaslecomte on 12/12/2016.
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = Build.VERSION_CODES.LOLLIPOP)
public class InitializationTest
{
    @Test
    public void init()
    {
        IBeaconScanner.initialize(IBeaconScanner.newInitializer(RuntimeEnvironment.application).build());
    }

    @Test
    public void initWithSamsungKnox()
    {
        // apps running in a Samsung Knox container do not have Bluetooth permission.

        // create a BluetoothFactory that always throws the SecurityException like when on Samsung Knox
        final BluetoothFactory bluetoothFactory = mock(BluetoothFactory.class);
        Mockito.doThrow(new SecurityException("Need BLUETOOTH permission: Neither user xxxxx nor current process has android.permission.BLUETOOTH"))
                .when(bluetoothFactory).createBluetoothLeScanner();

        // create a ScanService with the BluetoothFactory set
        final DefaultScanService scanService = IBeaconScanner.newInitializer(RuntimeEnvironment.application)
                .setBluetoothFactory(bluetoothFactory)
                .build();

        IBeaconScanner.initialize(scanService);

        // test start scanning logic
        scanService.timedOut(new Object());

        verify(bluetoothFactory, times(1)).createBluetoothLeScanner();
        verify(bluetoothFactory, times(2)).getBluetoothLeScanner();
    }
}
