package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by eliaslecomte on 13/12/2016.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class DefaultScanServiceTest
{
    @Test
    public void testAppRunningInSamsungKnoxContainer()
    {
        final DefaultBluetoothFactory defaultBluetoothFactory = mock(DefaultBluetoothFactory.class);
        Mockito.doThrow(new SecurityException("Need BLUETOOTH permission: Neither user xxxxx nor current process has android.permission.BLUETOOTH"))
                .when(defaultBluetoothFactory).getBluetoothAdapter();
        when(defaultBluetoothFactory.canAttachBluetoothAdapter()).thenCallRealMethod();
        when(defaultBluetoothFactory.getBluetoothLeScanner()).thenCallRealMethod();

        Assert.assertEquals(false, defaultBluetoothFactory.canAttachBluetoothAdapter());
        Assert.assertEquals(null, defaultBluetoothFactory.getBluetoothLeScanner());
    }

    @Test
    public void testSamsungKnox()
    {

    }

    @Test
    public void testMissingBluetoothPermission()
    {
        final BluetoothAdapter bluetoothAdapter = mock(BluetoothAdapter.class);
        Mockito.doThrow(new SecurityException("Need BLUETOOTH permission: Neither user xxxxx nor current process has android.permission.BLUETOOTH"))
                .when(bluetoothAdapter).getBluetoothLeScanner();

        final DefaultBluetoothFactory defaultBluetoothFactory = mock(DefaultBluetoothFactory.class);
        when(defaultBluetoothFactory.canAttachBluetoothAdapter()).thenCallRealMethod();
        when(defaultBluetoothFactory.getBluetoothLeScanner()).thenCallRealMethod();
        when(defaultBluetoothFactory.getBluetoothAdapter()).thenReturn(bluetoothAdapter);

        Assert.assertEquals(false, defaultBluetoothFactory.canAttachBluetoothAdapter());
        Assert.assertEquals(null, defaultBluetoothFactory.getBluetoothLeScanner());
    }

    @Test
    public void testBluetoothOff()
    {
        final BluetoothAdapter bluetoothAdapter = mock(BluetoothAdapter.class);
        when(bluetoothAdapter.getBluetoothLeScanner()).thenReturn(null);

        final DefaultBluetoothFactory defaultBluetoothFactory = mock(DefaultBluetoothFactory.class);
        when(defaultBluetoothFactory.canAttachBluetoothAdapter()).thenCallRealMethod();
        when(defaultBluetoothFactory.getBluetoothLeScanner()).thenCallRealMethod();
        when(defaultBluetoothFactory.getBluetoothAdapter()).thenReturn(bluetoothAdapter);

        Assert.assertEquals(true, defaultBluetoothFactory.canAttachBluetoothAdapter());
        Assert.assertEquals(null, defaultBluetoothFactory.getBluetoothLeScanner());
    }
}
