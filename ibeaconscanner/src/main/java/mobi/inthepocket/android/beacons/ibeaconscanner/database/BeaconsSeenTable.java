package mobi.inthepocket.android.beacons.ibeaconscanner.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by eliaslecomte on 27/09/2016.
 */

public class BeaconsSeenTable
{
    public final static String TABLE_NAME = "beaconsseen";

    public final static String COLUMN_BEACON_UUID = "uuid",
            COLUMN_BEACON_MAJOR = "major",
            COLUMN_BEACON_MINOR = "minor",
            COLUMN_TIMESTAMP = "timestamp";

    private final static String DATABASE_CREATE = "create table if not exists " + TABLE_NAME
            + "("
            + COLUMN_BEACON_UUID + " text, "
            + COLUMN_BEACON_MAJOR + " int, "
            + COLUMN_BEACON_MINOR + " int, "
            + COLUMN_TIMESTAMP + " int, "
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
