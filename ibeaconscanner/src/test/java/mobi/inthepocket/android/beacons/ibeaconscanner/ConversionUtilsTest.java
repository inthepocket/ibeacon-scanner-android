package mobi.inthepocket.android.beacons.ibeaconscanner;

import junit.framework.Assert;

import org.junit.Test;

import java.util.UUID;

import mobi.inthepocket.android.beacons.ibeaconscanner.utils.ConversionUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by eliaslecomte on 27/09/2016.
 */

public class ConversionUtilsTest
{
    private static final int MAJOR_MINOR_MAX_VALUE = 65535;

    private static final String EXAMPLE_BEACON_1_UUID = "84be19d4-797d-11e5-8bcf-feff819cdc9f";
    private static final int EXAMPLE_BEACON_1_MAJOR = 1;
    private static final int EXAMPLE_BEACON_1_MINOR = 2;

    @Test
    public void testBytesToUuid()
    {
        final byte[] byteArray = { (byte) -124, (byte) -66, (byte) 25, (byte) -44, (byte) 121, (byte) 125, (byte) 17, (byte) -27, (byte) -117, (byte) -49, (byte) -2, (byte) -1, (byte) -127, (byte) -100, (byte) -36, (byte) -97};

        final UUID uuid = ConversionUtils.bytesToUuid(byteArray);

        assertNotNull(uuid);
        assertEquals(uuid, UUID.fromString(EXAMPLE_BEACON_1_UUID));
    }

    @Test
    public void testExampleBeacon1ScanFilter()
    {
        final Beacon beacon = Beacon.newBuilder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        final byte[] byteArray = ConversionUtils.UuidToByteArray(beacon.getUUID());

        assertNotNull(byteArray);
    }

    @Test
    public void testCrashingMajorMinors()
    {
        final byte[] bytes = ConversionUtils.integerToByteArray(80);

        assertNotNull(bytes);
    }

    @Test
    public void testCreatingAllTheMajors()
    {
        int major = 1;

        while (major <= 65535)
        {
            final byte[] bytes = ConversionUtils.integerToByteArray(major);

            assertNotNull(bytes);

            major++;
        }
    }

    @Test
    public void testMajorByteToInts()
    {
        int major = 1;

        while (major <= MAJOR_MINOR_MAX_VALUE)
        {
            final byte[] bytes = ConversionUtils.integerToByteArray(major);

            Assert.assertEquals(major, ConversionUtils.byteArrayToInteger(bytes));

            major++;
        }
    }
}
