package mobi.inthepocket.android.beacons.ibeaconscanner.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import mobi.inthepocket.android.beacons.ibeaconscanner.BuildConfig;
import mobi.inthepocket.android.beacons.ibeaconscanner.database.BeaconsSeenTable;
import mobi.inthepocket.android.beacons.ibeaconscanner.database.DatabaseHelper;

/**
 * Created by eliaslecomte on 27/09/2016.
 */

public class BeaconsSeenProvider extends ContentProvider
{
    private static final String PROVIDER_NAME = BuildConfig.APPLICATION_ID + ".providers.BeaconsSeenProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME);
    public static final Uri CONTENT_URI_ITEM = Uri.parse("content://" + PROVIDER_NAME + "/item/");

    private static final int LIST = 1;
    private static final int ITEM = 2;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private SQLiteDatabase database;

    static
    {
        uriMatcher.addURI(PROVIDER_NAME, "item/*/#/#", ITEM);
        uriMatcher.addURI(PROVIDER_NAME, "/*", LIST);
    }

    @Override
    public boolean onCreate()
    {
        //noinspection ConstantConditions
        final DatabaseHelper databaseHelper = new DatabaseHelper(this.getContext());
        this.database = databaseHelper.getWritableDatabase();

        return this.database != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull final Uri uri, final String[] projection, String selection, String[] selectionArgs, final String sortOrder)
    {
        switch (uriMatcher.match(uri))
        {
            case ITEM:

                final List<String> pathSegments = uri.getPathSegments();
                final int size = pathSegments.size();
                final String uuid = pathSegments.get(size - 3);
                final String major = pathSegments.get(size - 2);
                final String minor = pathSegments.get(size - 1);

                selection = BeaconsSeenTable.COLUMN_BEACON_UUID + " = ? AND "
                        + BeaconsSeenTable.COLUMN_BEACON_MAJOR + " = ? AND "
                        + BeaconsSeenTable.COLUMN_BEACON_MINOR + " = ?";
                selectionArgs = new String[] { uuid, major, minor };

                break;

            case LIST:

                // todo: implement a query that takes into account the timestamp
                break;
        }

        final Cursor cursor = this.database.query(getTableName(), projection, selection, selectionArgs, null, null, sortOrder);

        final Context context = this.getContext();
        if (context != null)
        {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull final Uri uri)
    {
        switch (uriMatcher.match(uri))
        {
            case LIST:

                return "vnd.android.cursor.dir/" + PROVIDER_NAME;

            case ITEM:

                return "vnd.android.cursor.item/" + PROVIDER_NAME;

            default:

                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues values)
    {
        long rowID = 0;

        try
        {
            rowID = this.database.replace(getTableName(), null, values);
        }
        catch (final SQLException sqlException)
        {
        }

        // success
        if (rowID > 0)
        {
            final Uri uriResult = ContentUris.withAppendedId(uri, rowID);

            final Context context = this.getContext();
            if (context != null)
            {
                context.getContentResolver().notifyChange(uri, null);
            }

            return uriResult;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull final Uri uri, String selection, String[] selectionArgs)
    {
        if (uriMatcher.match(uri) == ITEM)
        {
            final List<String> pathSegments = uri.getPathSegments();
            final int size = pathSegments.size();
            final String uuid = pathSegments.get(size - 3);
            final String major = pathSegments.get(size - 2);
            final String minor = pathSegments.get(size - 1);

            selection = BeaconsSeenTable.COLUMN_BEACON_UUID + " = ? AND "
                    + BeaconsSeenTable.COLUMN_BEACON_MAJOR + " = ? AND "
                    + BeaconsSeenTable.COLUMN_BEACON_MINOR + " = ?";
            selectionArgs = new String[]{uuid, major, minor};
        }

        int count = 0;
        try
        {
            count = this.database.delete(getTableName(), selection, selectionArgs);
        }
        catch (final SQLException ex)
        {
        }

        final Context context = this.getContext();
        if (count > 0 && context != null)
        {
            context.getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public int update(@NonNull final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs)
    {
        int count = 0;

        try
        {
            count = this.database.update(getTableName(), values, selection, selectionArgs);
        }
        catch (final SQLException ex)
        {
        }


        final Context context = this.getContext();
        if (count > 0 && context != null)
        {
            context.getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    //region Properties

    private static String getTableName()
    {
        return BeaconsSeenTable.TABLE_NAME;
    }

    //endregion
}
