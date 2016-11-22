package mobi.inthepocket.android.beacons.app;

import android.app.Application;

import mobi.inthepocket.android.beacons.ibeaconscanner.IBeaconScanner;
import mobi.inthepocket.android.beacons.ibeaconscanner.ScanService;

import static org.mockito.Mockito.mock;

/**
 * Created by elias on 19/11/2016.
 */

public class TestApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // initialize In The Pockets iBeaconScanner with a mocked ScanService (to avoid bluetooth)
        IBeaconScanner.initialize(mock(ScanService.class));
    }
}
