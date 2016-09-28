package mobi.inthepocket.android.beacons.app.application;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by eliaslecomte on 27/09/2016.
 */

public class AppApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // initialize Facebook Stetho
        Stetho.initializeWithDefaults(this);
    }
}
