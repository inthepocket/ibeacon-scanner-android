package mobi.inthepocket.android.beacons.ibeaconscanner;

import org.junit.Test;

import mobi.inthepocket.android.beacons.ibeaconscanner.utils.ConversionUtils;

import static org.junit.Assert.assertNotNull;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 *
 * // todo: unit test the ConversionUtils
 */
public class ConversionUtilsTest
{
    private final static String EXAMPLE_BEACON_1_UUID = "84be19d4-797d-11e5-8bcf-feff819cdc9f";
    private final static int EXAMPLE_BEACON_1_MAJOR = 1;
    private final static int EXAMPLE_BEACON_1_MINOR = 2;

    @Test
    public void testExampleBeacon1ScanFilter() throws Exception
    {
        final Region region = new Region.Builder()
                .setUUID(EXAMPLE_BEACON_1_UUID)
                .setMajor(EXAMPLE_BEACON_1_MAJOR)
                .setMinor(EXAMPLE_BEACON_1_MINOR)
                .build();

        final byte[] byteArray = ConversionUtils.UuidToByteArray(region.getUUID());

        assertNotNull(byteArray);
    }
}
