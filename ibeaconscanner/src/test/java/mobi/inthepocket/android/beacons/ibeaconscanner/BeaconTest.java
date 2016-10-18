package mobi.inthepocket.android.beacons.ibeaconscanner;

import junit.framework.Assert;

import org.junit.Test;

import java.util.UUID;

import mobi.inthepocket.android.beacons.ibeaconscanner.exceptions.IllegalMajorException;
import mobi.inthepocket.android.beacons.ibeaconscanner.exceptions.IllegalMinorException;
import mobi.inthepocket.android.beacons.ibeaconscanner.exceptions.IllegalUUIDException;

import static org.junit.Assert.assertNotNull;

/**
 * Created by eliaslecomte on 30/09/2016.
 */

public class BeaconTest
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
            final Beacon beacon = new Beacon.Builder()
                    .setUUID(EXAMPLE_BEACON_1_UUID)
                    .setMajor(major)
                    .setMinor(EXAMPLE_BEACON_1_MINOR)
                    .build();

            assertNotNull(beacon);

            major++;
        }
    }

    @Test
    public void testAllMinors()
    {
        int minor = 1;

        while (minor <= 65535)
        {
            final Beacon beacon = new Beacon.Builder()
                    .setUUID(EXAMPLE_BEACON_1_UUID)
                    .setMajor(EXAMPLE_BEACON_1_MAJOR)
                    .setMinor(minor)
                    .build();

            assertNotNull(beacon);

            minor++;
        }
    }

    @Test
    public void testWithRandomUUID()
    {
        final UUID uuid = UUID.randomUUID();

        final Beacon beacon = new Beacon.Builder()
                .setUUID(uuid)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        assertNotNull(beacon);
    }

    @Test(expected = IllegalUUIDException.class)
    public void testWithoutUUID()
    {
        final Beacon beacon = new Beacon.Builder()
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongUUIDString()
    {
        final Beacon beacon = new Beacon.Builder()
                .setUUID(new String())
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();
    }

    @Test(expected = IllegalMajorException.class)
    public void testNegativeMajor() throws Exception
    {
        final Beacon beacon = new Beacon.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(-1)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();
    }

    @Test(expected = IllegalMinorException.class)
    public void testNegativeMinor() throws Exception
    {
        final Beacon beacon = new Beacon.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(-1)
                .build();
    }

    @Test(expected = IllegalMajorException.class)
    public void testToHighMajor() throws Exception
    {
        final Beacon beacon = new Beacon.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(MAJOR_MINOR_MAX_VALUE + 1)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();
    }

    @Test(expected = IllegalMinorException.class)
    public void testToHighMinor() throws Exception
    {
        final Beacon beacon = new Beacon.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(MAJOR_MINOR_MAX_VALUE + 1)
                .build();
    }

    @Test
    public void testEqualBeacons()
    {
        final Beacon beacon1 = new Beacon.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        final Beacon beacon2 = new Beacon.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        Assert.assertEquals(beacon1, beacon2);
    }

    @Test
    public void testNotEqualBeaconsByUUID()
    {
        final Beacon beacon1 = new Beacon.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        final Beacon beacon2 = new Beacon.Builder()
                .setUUID(UUID.randomUUID())
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        Assert.assertNotSame(beacon1, beacon2);
    }

    @Test
    public void testNotEqualBeaconsByMajor()
    {
        final Beacon beacon1 = new Beacon.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        final Beacon beacon2 = new Beacon.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR + 1)
                .build();

        Assert.assertNotSame(beacon1, beacon2);
    }
}
