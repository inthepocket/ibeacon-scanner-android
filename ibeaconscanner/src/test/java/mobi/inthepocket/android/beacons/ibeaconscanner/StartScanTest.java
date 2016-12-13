package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import mobi.inthepocket.android.beacons.ibeaconscanner.interfaces.BluetoothFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by eliaslecomte on 13/12/2016.
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = Build.VERSION_CODES.LOLLIPOP)
public class StartScanTest
{
    @Test
    public void startWithBluetoothOff()
    {
        final BluetoothFactory bluetoothFactory = mock(BluetoothFactory.class);
        when(bluetoothFactory.canAttachBluetoothAdapter()).thenReturn(true);
        when(bluetoothFactory.getBluetoothLeScanner()).thenReturn(null);

        // create a ScanService with the BluetoothFactory set
        final DefaultScanService scanService = IBeaconScanner.newInitializer(RuntimeEnvironment.application)
                .setBluetoothFactory(bluetoothFactory)
                .build();

        IBeaconScanner.initialize(scanService);

        // test start scanning
        scanService.timedOut(new Object());

        verify(bluetoothFactory, times(1)).canAttachBluetoothAdapter();
        verify(bluetoothFactory, times(0)).getBluetoothLeScanner();
    }

    @Test
    public void startWithMissingPermissions()
    {
        final BluetoothFactory bluetoothFactory = mock(BluetoothFactory.class);
        when(bluetoothFactory.canAttachBluetoothAdapter()).thenReturn(false);
        when(bluetoothFactory.getBluetoothLeScanner()).thenReturn(null);

        // create a ScanService with the BluetoothFactory set
        final DefaultScanService scanService = IBeaconScanner.newInitializer(RuntimeEnvironment.application)
                .setBluetoothFactory(bluetoothFactory)
                .build();

        IBeaconScanner.initialize(scanService);

        // test start scanning
        scanService.timedOut(new Object());

        verify(bluetoothFactory, times(1)).canAttachBluetoothAdapter();
        verify(bluetoothFactory, times(0)).getBluetoothLeScanner();
    }
}
