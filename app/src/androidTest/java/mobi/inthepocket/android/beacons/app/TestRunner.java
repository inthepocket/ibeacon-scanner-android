package mobi.inthepocket.android.beacons.app;

import android.app.Application;
import android.content.Context;
import android.support.test.runner.AndroidJUnitRunner;

/**
 * Created by elias on 19/11/2016.
 */

public class TestRunner extends AndroidJUnitRunner
{
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        return super.newApplication(cl, TestApplication.class.getName(), context);
    }
}
