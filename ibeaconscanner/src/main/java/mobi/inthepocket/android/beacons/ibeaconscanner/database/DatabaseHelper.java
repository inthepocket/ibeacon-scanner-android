package mobi.inthepocket.android.beacons.ibeaconscanner.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by eliaslecomte on 27/09/2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "ibeacon_scanner";
    private static final int DATABASE_VERSION = 1;

    final BeaconsSeenTable beaconsSeenTable;

    /**
     * Helper class to create and upgrade all tables used in iBeacon Scanner Android.
     *
     * @param context to create database with
     */
    public DatabaseHelper(final Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.beaconsSeenTable = new BeaconsSeenTable();
    }

    @Override
    public void onCreate(final SQLiteDatabase database)
    {
        this.beaconsSeenTable.onCreate(database);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase database, final int oldVersion, final int newVersion)
    {
        this.beaconsSeenTable.onUpgrade(database, oldVersion, newVersion);
    }
}
