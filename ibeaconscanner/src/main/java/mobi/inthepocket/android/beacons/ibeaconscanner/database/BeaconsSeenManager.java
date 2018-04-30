package mobi.inthepocket.android.beacons.ibeaconscanner.database;

import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import mobi.inthepocket.android.beacons.ibeaconscanner.Beacon;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.BeaconUtils;

/**
 * Manager class wrapping {@link BeaconsSeenProvider} to provide helper functions for bookkeeping of
 * beacon triggers to determine weather a beacon entered or exited a {@link Beacon}.
 */
public class BeaconsSeenManager
{
    private final BeaconsSeenProvider beaconsSeenProvider;
    private final long beaconExitTimeoutInMillis;

    /**
     * Creates a new BeaconsSeenManager.
     *
     * @param beaconsSeenProvider       data service responsible for bookkeeping
     * @param beaconExitTimeoutInMillis timeout after which a beacon is considered exited
     */
    public BeaconsSeenManager(@NonNull final BeaconsSeenProvider beaconsSeenProvider, final long beaconExitTimeoutInMillis)
    {
        this.beaconsSeenProvider = beaconsSeenProvider;
        this.beaconExitTimeoutInMillis = beaconExitTimeoutInMillis;
    }

    /**
     * Checks in the database whether a beacon has been triggered.
     *
     * @param beacon detected beacon
     * @return true when the beacon has been triggered before, false if it has yet to be triggered.
     */
    public boolean hasBeaconBeenTriggered(final Beacon beacon)
    {
        final Uri uri = BeaconUtils.getItemUri(beacon);
        Cursor cursor = null;
        try
        {
            cursor = this.beaconsSeenProvider.query(uri);
            if (cursor != null)
            {
                final List<BeaconSeen> beaconSeens = new ArrayList<>();
                if (cursor.moveToFirst())
                {
                    do
                    {
                        final BeaconSeen beaconSeen = new BeaconSeen();
                        beaconSeen.constructFromCursor(cursor);
                        beaconSeens.add(beaconSeen);
                    }
                    while (cursor.moveToNext());
                }

                if (beaconSeens.isEmpty())
                {
                    // this beacon is not yet in our database
                    return false;
                }
            }
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }

        return true;
    }

    /**
     * Add a detected beacon to the database.
     *
     * @param beacon detected beacon
     */
    public void addBeaconToDatabase(@NonNull final Beacon beacon)
    {
        final Uri uri = BeaconUtils.getItemUri(beacon);
        this.beaconsSeenProvider.insert(uri, BeaconSeen.getContentValues(beacon, SystemClock.elapsedRealtime()));
    }

    /**
     * Finds a single beacon in the database based on its UUID, major and minor values.
     *
     * @param uuid  UUID of the desired beacon
     * @param major major value of the desired beacon
     * @param minor minor value of the desired beacon
     * @return {@link Beacon} that has been located in the database, or null if not found.
     */
    public Beacon getBeaconFromDatabase(@NonNull final String uuid, final int major, final int minor)
    {
        Beacon beacon = null;
        final Uri uri = BeaconUtils.getItemUri(uuid, major, minor);
        Cursor cursor = null;
        try
        {
            cursor = this.beaconsSeenProvider.query(uri);
            if (cursor != null && cursor.moveToFirst())
            {
                final BeaconSeen beaconSeen = new BeaconSeen();
                beaconSeen.constructFromCursor(cursor);
                beacon = Beacon.newBuilder()
                        .setUUID(beaconSeen.getUuid())
                        .setMajor(beaconSeen.getMajor())
                        .setMinor(beaconSeen.getMinor())
                        .build();
            }
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return beacon;
    }

    /**
     * Remove exited beacon from the database.
     *
     * @param beacon exited beacon
     */
    public void removeBeaconFromDatabase(@NonNull final Beacon beacon)
    {
        this.beaconsSeenProvider.delete(BeaconUtils.getItemUri(beacon));
    }

    /**
     * Normally there should be no {@link BeaconSeen} entries in our database, but in the case the
     * library was stopped abruptly, this removes {@link BeaconSeen} entries from the {@link BeaconsSeenTable}
     * that are in the future (what happens when you restart your device as {@link SystemClock#elapsedRealtime()}
     * returns the time since boot) or beacons that not have been exited in time.
     */
    public void removeObsoleteBeaconSeenEntries()
    {
        this.beaconsSeenProvider.delete(Uri.withAppendedPath(BeaconsSeenProvider.CONTENT_URI, String.valueOf(this.beaconExitTimeoutInMillis)));
    }

    /**
     * Fetch all beacons that were not yet exited.
     *
     * @return a list of known beacons that have yet to be exited.
     */
    public List<BeaconSeen> fetchPresentBeacons()
    {
        final List<BeaconSeen> beaconSeens = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            cursor = this.beaconsSeenProvider.query(BeaconsSeenProvider.CONTENT_URI);
            if (cursor != null && cursor.moveToFirst())
            {
                do
                {
                    final BeaconSeen beaconSeen = new BeaconSeen();
                    beaconSeen.constructFromCursor(cursor);
                    beaconSeens.add(beaconSeen);
                }
                while (cursor.moveToNext());
            }
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return beaconSeens;
    }
}
