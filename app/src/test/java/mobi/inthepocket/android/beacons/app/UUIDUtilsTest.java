package mobi.inthepocket.android.beacons.app;

import org.junit.Test;

import mobi.inthepocket.android.beacons.app.utils.UUIDUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by eliaslecomte on 29/10/2016.
 */
public class UUIDUtilsTest
{
    private static final String EXAMPLE_BEACON_1_UUID = "84be19d4-797d-11e5-8bcf-feff819cdc9f";

    @Test
    public void testValidUUID()
    {
        assertTrue(UUIDUtils.isValidUUID(EXAMPLE_BEACON_1_UUID));
    }

    @Test
    public void testInvalidUUID()
    {
        assertFalse(UUIDUtils.isValidUUID("k;lasdf;kljadsf;klajdfs;lkjasdf;lkjasdf;ljkasdf;laksdjf;alskdfjaadsf;kl"));
    }
}
