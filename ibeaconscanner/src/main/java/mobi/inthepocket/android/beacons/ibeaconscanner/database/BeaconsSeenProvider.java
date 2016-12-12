package mobi.inthepocket.android.beacons.ibeaconscanner.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * As {@link android.content.ContentProvider} can't be used in library projects, we created {@link BeaconsSeenProvider},
 * which is our entry point to the database. All queries are created here.
 */

public class BeaconsSeenProvider
{
    public static final String PROVIDER_NAME = "ibeaconscanner";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME);
    public static final Uri CONTENT_URI_ITEM = Uri.parse("content://" + PROVIDER_NAME + "/item/");

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int LIST = 1;
    private static final int ITEM = 2;

    private final SQLiteDatabase database;

    static
    {
        uriMatcher.addURI(PROVIDER_NAME, "item/*/#/#", ITEM);
        uriMatcher.addURI(PROVIDER_NAME, "/*", LIST);
    }

    /**
     * Create the database manager {@link BeaconsSeenProvider}.
     *
     * @param context to manage the database with
     */
    public BeaconsSeenProvider(final Context context)
    {
        final DatabaseHelper databaseHelper = new DatabaseHelper(context);
        this.database = databaseHelper.getWritableDatabase();
    }

    /**
     * Query with {@code uri} parameters.
     *
     * @param uri parameters for the query
     * @return cursor with query results
     */
    public Cursor query(@NonNull final Uri uri)
    {
        String selection = null;
        String[] selectionArgs = null;

        final List<String> pathSegments = uri.getPathSegments();

        switch (uriMatcher.match(uri))
        {
            case ITEM:

                final String uuid = pathSegments.get(1);
                final String major = pathSegments.get(2);
                final String minor = pathSegments.get(3);

                selection = BeaconsSeenTable.COLUMN_BEACON_UUID + " = ? AND "
                        + BeaconsSeenTable.COLUMN_BEACON_MAJOR + " = ? AND "
                        + BeaconsSeenTable.COLUMN_BEACON_MINOR + " = ?";
                selectionArgs = new String[]{uuid, major, minor};

                break;

            case LIST:

                final long exitTimeInMillis = Long.parseLong(pathSegments.get(0));
                selection = BeaconsSeenTable.COLUMN_TIMESTAMP + " < ? AND "
                        + BeaconsSeenTable.COLUMN_TIMESTAMP + " > ?";
                selectionArgs = new String[]{String.valueOf(SystemClock.elapsedRealtime()), String.valueOf(SystemClock.elapsedRealtime() - exitTimeInMillis)};

                break;

        }

        return this.database.query(BeaconsSeenTable.TABLE_NAME, null, selection, selectionArgs, null, null, null);
    }

    /**
     * Insert {@code values} with parameters {@code uri}.
     *
     * @param uri parameters for the insert
     * @param values to insert
     * @return cursor with query results
     */
    public Uri insert(@NonNull final Uri uri, @NonNull final ContentValues values)
    {
        long rowID = 0;

        try
        {
            rowID = this.database.replace(BeaconsSeenTable.TABLE_NAME, null, values);
        }
        catch (final SQLException sqlException)
        {
        }

        // success
        if (rowID > 0)
        {
            return ContentUris.withAppendedId(uri, rowID);
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    /**
     * Delete {@code uri}.
     *
     * @param uri to delete
     * @return number of rows that are deleted
     */
    public int delete(@NonNull final Uri uri)
    {
        String selection = null;
        String[] selectionArgs = null;

        final List<String> pathSegments = uri.getPathSegments();

        switch (uriMatcher.match(uri))
        {
            case ITEM:

                final String uuid = pathSegments.get(1);
                final String major = pathSegments.get(2);
                final String minor = pathSegments.get(3);

                selection = BeaconsSeenTable.COLUMN_BEACON_UUID + " = ? AND "
                        + BeaconsSeenTable.COLUMN_BEACON_MAJOR + " = ? AND "
                        + BeaconsSeenTable.COLUMN_BEACON_MINOR + " = ?";
                selectionArgs = new String[]{uuid, major, minor};

                break;

            case LIST:

                // removes everything after current {@link SystemClock#elapsedRealtime} and everything before
                // {@link SystemClock#elapsedRealtime} - beacon exit time.

                final long exitTimeInMillis = Long.parseLong(pathSegments.get(0));

                selection = BeaconsSeenTable.COLUMN_TIMESTAMP + " > ? OR "
                        + BeaconsSeenTable.COLUMN_TIMESTAMP + " < ?";
                selectionArgs = new String[]{String.valueOf(SystemClock.elapsedRealtime()), String.valueOf(SystemClock.elapsedRealtime() - exitTimeInMillis)};

                break;

        }

        int count = 0;
        try
        {
            count = this.database.delete(BeaconsSeenTable.TABLE_NAME, selection, selectionArgs);
        }
        catch (final SQLException ex)
        {
        }

        return count;
    }
}
