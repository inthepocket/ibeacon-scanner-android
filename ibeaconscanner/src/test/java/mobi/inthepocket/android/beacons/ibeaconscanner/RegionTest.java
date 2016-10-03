package mobi.inthepocket.android.beacons.ibeaconscanner;

import junit.framework.Assert;

import org.junit.Test;

import java.util.UUID;

import mobi.inthepocket.android.beacons.ibeaconscanner.exceptions.IllegalMajorException;
import mobi.inthepocket.android.beacons.ibeaconscanner.exceptions.IllegalMinorException;
import mobi.inthepocket.android.beacons.ibeaconscanner.exceptions.IllegalUIDException;

import static org.junit.Assert.assertNotNull;

/**
 * Created by eliaslecomte on 30/09/2016.
 */

public class RegionTest
{
    private final static int MAJOR_MINOR_MAX_VALUE = 65535;

    private final static String EXAMPLE_BEACON_1_UUID = "84be19d4-797d-11e5-8bcf-feff819cdc9f";
    private final static int EXAMPLE_BEACON_1_MAJOR = 1;
    private final static int EXAMPLE_BEACON_1_MINOR = 2;

    @Test
    public void testAllMajors()
    {
        int major = 1;

        while (major <= 65535)
        {
            final Region region = new Region.Builder()
                    .setUUID(EXAMPLE_BEACON_1_UUID)
                    .setMajor(major)
                    .setMinor(EXAMPLE_BEACON_1_MINOR)
                    .build();

            assertNotNull(region);

            major++;
        }
    }

    @Test
    public void testAllMinors()
    {
        int minor = 1;

        while (minor <= 65535)
        {
            final Region region = new Region.Builder()
                    .setUUID(EXAMPLE_BEACON_1_UUID)
                    .setMajor(EXAMPLE_BEACON_1_MAJOR)
                    .setMinor(minor)
                    .build();

            assertNotNull(region);

            minor++;
        }
    }

    @Test
    public void testWithRandomUUID()
    {
        final UUID uuid = UUID.randomUUID();

        final Region region = new Region.Builder()
                .setUUID(uuid)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        assertNotNull(region);
    }

    @Test(expected = IllegalUIDException.class)
    public void testWithoutUUID()
    {
        final Region region = new Region.Builder()
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongUUIDString()
    {
        final Region region = new Region.Builder()
                .setUUID(new String())
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();
    }

    @Test(expected = IllegalMajorException.class)
    public void testNegativeMajor() throws Exception
    {
        final Region region = new Region.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(-1)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();
    }

    @Test(expected = IllegalMinorException.class)
    public void testNegativeMinor() throws Exception
    {
        final Region region = new Region.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(-1)
                .build();
    }

    @Test(expected = IllegalMajorException.class)
    public void testToHighMajor() throws Exception
    {
        final Region region = new Region.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(MAJOR_MINOR_MAX_VALUE + 1)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();
    }

    @Test(expected = IllegalMinorException.class)
    public void testToHighMinor() throws Exception
    {
        final Region region = new Region.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(MAJOR_MINOR_MAX_VALUE + 1)
                .build();
    }

    @Test
    public void testEqualRegions()
    {
        final Region region1 = new Region.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        final Region region2 = new Region.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        Assert.assertEquals(region1, region2);
    }

    @Test
    public void testNotEqualRegionsByUUID()
    {
        final Region region1 = new Region.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        final Region region2 = new Region.Builder()
                .setUUID(UUID.randomUUID())
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        Assert.assertNotSame(region1, region2);
    }

    @Test
    public void testNotEqualRegionsByMajor()
    {
        final Region region1 = new Region.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        final Region region2 = new Region.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR + 1)
                .build();

        Assert.assertNotSame(region1, region2);
    }
}
