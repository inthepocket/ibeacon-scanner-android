package mobi.inthepocket.android.beacons.ibeaconscanner.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by eliaslecomte on 27/09/2016.
 */

public class BeaconsSeenTable
{
    public static final String TABLE_NAME = "beacons_seen";

    public static final  String COLUMN_BEACON_UUID = "uuid",
            COLUMN_BEACON_MAJOR = "major",
            COLUMN_BEACON_MINOR = "minor",
            COLUMN_TIMESTAMP = "timestamp";

    private static final String DATABASE_CREATE = "create table if not exists " + TABLE_NAME
            + "("
            + COLUMN_BEACON_UUID + " text, "
            + COLUMN_BEACON_MAJOR + " integer, "
            + COLUMN_BEACON_MINOR + " integer, "
            + COLUMN_TIMESTAMP + " integer, "
            + "UNIQUE ( "
            + COLUMN_BEACON_UUID + ", "
            + COLUMN_BEACON_MAJOR + ", "
            + COLUMN_BEACON_MINOR
            + ")"
            + ");";

    public void onCreate(final SQLiteDatabase database)
    {
        database.execSQL(DATABASE_CREATE);
    }

    public void onUpgrade(final SQLiteDatabase database, final int oldVersion, final int newVersion)
    {

    }
}
