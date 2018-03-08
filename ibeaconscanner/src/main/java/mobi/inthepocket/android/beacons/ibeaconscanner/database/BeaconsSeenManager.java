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
    private BeaconsSeenProvider beaconsSeenProvider;
    private long beaconExitTimeoutInMillis;

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
        final Uri uri = BeaconUtils.getItemUri(BeaconsSeenProvider.CONTENT_URI_ITEM, beacon);
        final Cursor cursor = this.beaconsSeenProvider.query(uri);
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

            cursor.close();

            if (beaconSeens.isEmpty())
            {
                // this beacon is not yet in our database
                return false;
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
        final Uri uri = BeaconUtils.getItemUri(BeaconsSeenProvider.CONTENT_URI_ITEM, beacon);
        this.beaconsSeenProvider.insert(uri, BeaconSeen.getContentValues(beacon, SystemClock.elapsedRealtime()));
    }

    /**
     * Remove exited beacon from the database.
     *
     * @param beacon exited beacon
     */
    public void removeBeaconFromDatabase(@NonNull final Beacon beacon)
    {
        this.beaconsSeenProvider.delete(BeaconUtils.getItemUri(BeaconsSeenProvider.CONTENT_URI_ITEM, beacon));
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
        final Cursor cursor = this.beaconsSeenProvider.query(BeaconsSeenProvider.CONTENT_URI);
        if (cursor != null)
        {
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

            cursor.close();
        }
        return beaconSeens;
    }
}
