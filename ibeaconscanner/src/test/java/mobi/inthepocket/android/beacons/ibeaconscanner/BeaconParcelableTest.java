package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.os.Parcel;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by eliaslecomte on 07/12/2016.
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class BeaconParcelableTest
{
    private static final String UUID = "0cba6230-bc70-11e6-a4a6-cec0c932ce01";
    private static final int MAJOR = 15;
    private static final int MINOR = 555;

    @Test
    public void testBeaconParcel()
    {
        final Beacon beacon = Beacon.newBuilder().setUUID(UUID)
                .setMajor(MAJOR)
                .setMinor(MINOR)
                .build();

        final Parcel beaconParcel = Parcel.obtain();
        beacon.writeToParcel(beaconParcel, 0);
        beaconParcel.setDataPosition(0);

        final Beacon beaconFromParcel = Beacon.CREATOR.createFromParcel(beaconParcel);

        Assert.assertEquals(beacon.getUUID(), beaconFromParcel.getUUID());
        Assert.assertEquals(beacon.getMajor(), beaconFromParcel.getMajor());
        Assert.assertEquals(beacon.getMinor(), beaconFromParcel.getMinor());
        Assert.assertEquals(UUID, beaconFromParcel.getUUID().toString());
        Assert.assertEquals(MAJOR, beaconFromParcel.getMajor());
        Assert.assertEquals(MINOR, beaconFromParcel.getMinor());
    }
}
