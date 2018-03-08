package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import java.util.List;

import mobi.inthepocket.android.beacons.ibeaconscanner.database.BeaconSeen;
import mobi.inthepocket.android.beacons.ibeaconscanner.database.BeaconsSeenProvider;
import mobi.inthepocket.android.beacons.ibeaconscanner.database.BeaconsSeenManager;
import mobi.inthepocket.android.beacons.ibeaconscanner.handlers.OnExitHandler;
import mobi.inthepocket.android.beacons.ibeaconscanner.handlers.TimeoutHandler;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.BeaconUtils;

/**
 * Created by eliaslecomte on 28/09/2016.
 * <p>
 * Class is responsible for:
 * - Reading beacon information from {@link ScanResult}s that are triggered by the {@link android.bluetooth.le.BluetoothLeScanner}.
 * - Bookkeeping of beacon triggers so it can determine weather a beacon entered or exited a {@link Beacon}.
 * - Calling {@link IBeaconScanner.Callback#didEnterBeacon(Beacon)} and
 * {@link IBeaconScanner.Callback#didExitBeacon(Beacon)}.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScannerScanCallback extends ScanCallback implements TimeoutHandler.TimeoutCallback<Beacon>
{
    private static final String TAG = ScanCallback.class.getSimpleName();

    private final BeaconsSeenManager beaconsSeenManager;
    private final OnExitHandler onExitHandler;
    private IBeaconScanner.Callback callback;

    /**
     * Creates a new ScannerScanCallback.
     *
     * @param beaconsSeenProvider       data service responsible for bookkeeping
     * @param beaconExitTimeoutInMillis timeout after which a beacon is considered exited
     */
    public ScannerScanCallback(@NonNull final BeaconsSeenProvider beaconsSeenProvider, final long beaconExitTimeoutInMillis)
    {
        this.beaconsSeenManager = new BeaconsSeenManager(beaconsSeenProvider, beaconExitTimeoutInMillis);
        this.onExitHandler = new OnExitHandler(this, beaconExitTimeoutInMillis);

        // remove obsolete BeaconSeen entries
        this.beaconsSeenManager.removeObsoleteBeaconSeenEntries();

        // reschedule ongoing exits via {@link OnExitHandler}
        this.resumeExits();
    }

    public void setCallback(@NonNull final IBeaconScanner.Callback callback)
    {
        this.callback = callback;
    }

    //region ScanCallback.onScanResult

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onScanResult(final int callbackType, final ScanResult scanResult)
    {
        Log.d(TAG, "In the range of beacon: " + scanResult.toString());

        super.onScanResult(callbackType, scanResult);

        // get Scan Record byte array (Be warned, this can be null)
        if (scanResult.getScanRecord() != null)
        {
            final Pair<Boolean, Integer> isBeacon = BeaconUtils.isBeaconPattern(scanResult);

            if (isBeacon.first)
            {
                final Beacon beacon = BeaconUtils.createBeaconFromScanRecord(scanResult.getScanRecord().getBytes(), isBeacon.second);

                // see if the beacon was not yet triggered
                if (this.beaconsSeenManager.hasBeaconBeenTriggered(beacon))
                {
                    // this beacon is not yet in our database, trigger {@link IBeaconScanner.Callback#didEnterBeacon}
                    if (this.callback != null)
                    {
                        this.callback.didEnterBeacon(beacon);
                    }

                    // add the enter beacon to database
                    this.beaconsSeenManager.addBeaconToDatabase(beacon);

                    // add beacon to our onExitHandler
                    this.onExitHandler.passItem(beacon);
                }
            }
        }
    }

    //endregion

    //region Helpers

    /**
     * Restarts countdown timers for entered {@link Beacon}s that were not yet exited.
     */
    private void resumeExits()
    {
        final List<BeaconSeen> beaconSeens = this.beaconsSeenManager.fetchPresentBeacons();

        for (final BeaconSeen beaconSeen : beaconSeens)
        {
            final Beacon beacon = Beacon.newBuilder()
                    .setUUID(beaconSeen.getUuid())
                    .setMajor(beaconSeen.getMajor())
                    .setMinor(beaconSeen.getMinor())
                    .build();
            this.onExitHandler.passItem(beacon);
        }
    }

    //endregion

    //region TimeoutHandler.TimeoutCallback

    @Override
    public void timedOut(@NonNull final Beacon beacon)
    {
        // exit happened, pass to callback
        if (this.callback != null)
        {
            this.callback.didExitBeacon(beacon);
        }

        // remove entry from database
        this.beaconsSeenManager.removeBeaconFromDatabase(beacon);
    }

    //endregion
}
