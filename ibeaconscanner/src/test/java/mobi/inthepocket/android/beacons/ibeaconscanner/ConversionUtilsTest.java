package mobi.inthepocket.android.beacons.ibeaconscanner;

import junit.framework.Assert;

import org.junit.Test;

import mobi.inthepocket.android.beacons.ibeaconscanner.utils.ConversionUtils;

import static org.junit.Assert.assertNotNull;

/**
 * Created by eliaslecomte on 27/09/2016.
 */

public class ConversionUtilsTest
{
    private final static int MAJOR_MINOR_MAX_VALUE = 65535;

    private final static String EXAMPLE_BEACON_1_UUID = "84be19d4-797d-11e5-8bcf-feff819cdc9f";
    private final static int EXAMPLE_BEACON_1_MAJOR = 1;
    private final static int EXAMPLE_BEACON_1_MINOR = 2;

    @Test
    public void testExampleBeacon1ScanFilter()
    {
        final Region region = new Region.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        final byte[] byteArray = ConversionUtils.UuidToByteArray(region.getUUID());

        assertNotNull(byteArray);
    }

    @Test
    public void testCrashingMajorMinors()
    {
        final int i = Integer.parseInt("80", 16);
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
