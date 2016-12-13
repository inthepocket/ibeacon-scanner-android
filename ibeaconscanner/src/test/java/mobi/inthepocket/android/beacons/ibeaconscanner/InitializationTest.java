package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Created by eliaslecomte on 12/12/2016.
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = Build.VERSION_CODES.LOLLIPOP)
public class InitializationTest
{
    @Test
    public void regularInitialize()
    {
        IBeaconScanner.initialize(IBeaconScanner.newInitializer(RuntimeEnvironment.application).build());
    }
}
