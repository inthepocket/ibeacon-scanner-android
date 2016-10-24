package mobi.inthepocket.android.beacons.ibeaconscanner.database;

import android.content.ContentValues;
import android.database.Cursor;

import mobi.inthepocket.android.beacons.ibeaconscanner.interfaces.BeaconInterface;

/**
 * Created by eliaslecomte on 27/09/2016.
 */

public class BeaconSeen
{
    private String uuid;
    private int major;
    private int minor;
    private long timestamp;

    //region ContentValues

    public void constructFromCursor(final Cursor cursor)
    {
        this.uuid = DatabaseUtils.getString(cursor, BeaconsSeenTable.COLUMN_BEACON_UUID, BeaconsSeenTable.TABLE_NAME);
        this.major = DatabaseUtils.getInt(cursor, BeaconsSeenTable.COLUMN_BEACON_MAJOR, BeaconsSeenTable.TABLE_NAME);
        this.minor = DatabaseUtils.getInt(cursor, BeaconsSeenTable.COLUMN_BEACON_MINOR, BeaconsSeenTable.TABLE_NAME);
        this.timestamp = DatabaseUtils.getInt(cursor, BeaconsSeenTable.COLUMN_TIMESTAMP, BeaconsSeenTable.TABLE_NAME);
    }

    public ContentValues getContentValues()
    {
        return getContentValues(this.uuid, this.major, this.minor, this.timestamp);
    }

    public static ContentValues getContentValues(final BeaconInterface beacon, final long timestamp)
    {
        return getContentValues(beacon.getUUID().toString(), beacon.getMajor(), beacon.getMinor(), timestamp);
    }

    public static ContentValues getContentValues(final String uuid, final int major, final int minor, final long timestamp)
    {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(BeaconsSeenTable.COLUMN_BEACON_UUID, uuid);
        contentValues.put(BeaconsSeenTable.COLUMN_BEACON_MAJOR, major);
        contentValues.put(BeaconsSeenTable.COLUMN_BEACON_MINOR, minor);
        contentValues.put(BeaconsSeenTable.COLUMN_TIMESTAMP, timestamp);

        return contentValues;
    }

    //endregion

    //region Getters

    public String getUuid()
    {
        return this.uuid;
    }

    public int getMajor()
    {
        return this.major;
    }

    public int getMinor()
    {
        return this.minor;
    }

    public long getTimestamp()
    {
        return this.timestamp;
    }

    //endregion
}
