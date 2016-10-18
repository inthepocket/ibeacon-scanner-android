package mobi.inthepocket.android.beacons.app.application;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

import mobi.inthepocket.android.beacons.ibeaconscanner.IBeaconScanner;

/**
 * Created by eliaslecomte on 27/09/2016.
 */

public class AppApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // initialize LeakCanary
        if (LeakCanary.isInAnalyzerProcess(this))
        {
            return;
        }
        LeakCanary.install(this);

        // initialize Facebook Stetho
        Stetho.initializeWithDefaults(this);

        // initialize In The Pockets iBeaconScanner
        IBeaconScanner.initialize(IBeaconScanner.newInitializer(this).build());
    }
}
