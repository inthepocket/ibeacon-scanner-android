package mobi.inthepocket.android.beacons.ibeaconscanner.database;

import android.database.Cursor;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eliaslecomte on 27/09/2016.
 */

public class DatabaseUtils
{
    /**
     * An mapping of index-names to their index to boos the performance!
     */
    private final static Map<String, Integer> sIndices = new HashMap<>();

    /**
     * @param c
     * @param columnName
     * @param projectionKey (if provided, columnIndex is cached which will boost performance while looping)
     * @return string value of the columnname
     * @since 1.0.0
     */
    public static String getString(final Cursor c, final String columnName, final String projectionKey)
    {
        try
        {
            if (validateCursor(c, columnName))
            {
                String str = c.getString(getIndex(c, columnName, projectionKey));

                if (str == null)
                {
                    str = "";
                }

                return str;
            }

            return "";
        }
        catch (final IllegalArgumentException e)
        {
            return "";
        }
    }

    /**
     * @param c
     * @param columnName
     * @param projectionKey (if provided, columnIndex is cached which will boost performance while looping)
     * @return int value of the columnname
     * @since 1.0.0
     */
    public static int getInt(final Cursor c, final String columnName, final String projectionKey)
    {
        try
        {
            if (validateCursor(c, columnName))
            {
                return c.getInt(getIndex(c, columnName, projectionKey));
            }

            return -1;
        }
        catch (final IllegalArgumentException e)
        {
            return -1;
        }
    }

    //region Helpers

    /**
     * Clear the indices-hashmap
     * @since 1.0.0
     */
    public static void clearIndices()
    {
        sIndices.clear();
    }

    /**
     * Get the index for the given cursor and columnName.
     * If you provide a table-parameter, then once an index was retrieved from the cursor, it is saved in the local HashMap to boost performance.
     * Call {@link DatabaseUtils#clearIndices()} ()} to clear the list
     * @param c
     * @param columnName (if null, the calculated index it NOT cached for later use
     * @param projectionKey
     * @return
     */
    private static int getIndex(final Cursor c, final String columnName, final String projectionKey)
    {
        if (TextUtils.isEmpty(projectionKey))
        {
            return c.getColumnIndexOrThrow(columnName);
        }
        final String key = new StringBuilder(projectionKey).append(columnName).toString();
        if (sIndices.containsKey(key))
        {
            return sIndices.get(key);
        }
        final int calculatedIndex = c.getColumnIndexOrThrow(columnName);
        sIndices.put(key, calculatedIndex);

        return calculatedIndex;
    }

    /**
     * @param c
     * @param columnName
     * @return true if the cursor is not null, not closed, not after the last entry, not before the first entry.
     */
    private static boolean validateCursor(final Cursor c, final String columnName)
    {
        return c != null && !c.isClosed() && !c.isAfterLast() && !c.isBeforeFirst();
    }

    //endregion
}
