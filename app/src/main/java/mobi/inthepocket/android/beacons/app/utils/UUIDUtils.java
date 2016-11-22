package mobi.inthepocket.android.beacons.app.utils;

import java.util.regex.Pattern;

/**
 * Created by eliaslecomte on 24/10/2016.
 */

public final class UUIDUtils
{
    private UUIDUtils()
    {
    }

    /**
     *
     * @param uuid
     * @return true if {@code uuid} matches the UUID pattern
     */
    public static boolean isValidUUID(final String uuid)
    {
        return Pattern.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}", uuid);
    }
}
