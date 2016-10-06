package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import mobi.inthepocket.android.beacons.ibeaconscanner.database.BeaconSeen;
import mobi.inthepocket.android.beacons.ibeaconscanner.database.BeaconsSeenTable;
import mobi.inthepocket.android.beacons.ibeaconscanner.handlers.OnExitHandler;
import mobi.inthepocket.android.beacons.ibeaconscanner.handlers.TimeoutHandler;
import mobi.inthepocket.android.beacons.ibeaconscanner.providers.BeaconsSeenProvider;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.ConversionUtils;

/**
 * Created by eliaslecomte on 28/09/2016.
 *
 * Class is responseable for:
 * - Reading beacon information from {@link ScanResult}'s that are triggered by the {@link android.bluetooth.le.BluetoothLeScanner}.
 * - Bookkeeping of beacon triggers so it can determine weather a beacon entered or exited a {@link Beacon}.
 * - Calling {@link IBeaconScanner.Callback#didEnterBeacon(Beacon)} and
 *  {@link IBeaconScanner.Callback#didExitBeacon(Beacon)}.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScannerScanCallback extends ScanCallback implements TimeoutHandler.TimeoutCallback<Beacon>
{
    private final static String TAG = ScanCallback.class.getSimpleName();

    private final ContentResolver contentResolver;
    private final OnExitHandler onExitHandler;
    private final long postDelayedInMillis;
    private IBeaconScanner.Callback callback;

    public ScannerScanCallback(@NonNull final ContentResolver contentResolver, final long postDelayedInMillis)
    {
        this.contentResolver = contentResolver;
        this.onExitHandler = new OnExitHandler(this, postDelayedInMillis);
        this.postDelayedInMillis = postDelayedInMillis;

        // remove obsolete BeaconSeen entries
        this.removeObsoleteBeaconSeenEntries();

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
        Log.d(TAG, "Found a beacon: " + scanResult.toString());

        super.onScanResult(callbackType, scanResult);

        // get Scan Record byte array (Be warned, this can be null)
        if (scanResult.getScanRecord() != null)
        {
            final byte[] scanRecord = scanResult.getScanRecord().getBytes();

            // get Rssi of device
            final int rssi = scanResult.getRssi();

            int startByte = 2;
            boolean patternFound = false;
            while (startByte <= 5)
            {
                if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && // identifies an iBeacon
                        ((int) scanRecord[startByte + 3] & 0xff) == 0x15)
                {
                    // identifies correct data length
                    patternFound = true;
                    break;
                }
                startByte++;
            }

            if (patternFound)
            {
                // get the UUID from the hex result
                final byte[] uuidBytes = new byte[16];
                System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                final UUID uuid = ConversionUtils.bytesToUuid(uuidBytes);

                // get the major from hex result
                //final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);
                final int major = ConversionUtils.byteArrayToInteger(Arrays.copyOfRange(scanRecord, startByte + 20, startByte + 22));

                // get the minor from hex result
                //final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);
                final int minor = ConversionUtils.byteArrayToInteger(Arrays.copyOfRange(scanRecord, startByte + 22, startByte + 24));

                final Beacon beacon = new Beacon.Builder()
                        .setUUID(uuid)
                        .setMajor(major)
                        .setMinor(minor)
                        .build();

                // see if the beacon was not yet triggered
                final Uri uri = getItemUri(beacon);
                final Cursor cursor = this.contentResolver.query(uri, null, null, null, null);
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

                    if (beaconSeens.size() == 0)
                    {
                        // this beacon is not yet in our database, trigger {@link IBeaconScanner.Callback#didEnterBeacon}
                        if (this.callback != null)
                        {
                            this.callback.didEnterBeacon(beacon);
                        }
                    }

                    // add the enter beacon to database
                    this.contentResolver.insert(uri, BeaconSeen.getContentValues(beacon, SystemClock.elapsedRealtime()));

                    // add beacon to our onExitHandler
                    this.onExitHandler.passItem(beacon);
                }
            }
        }
    }

    //endregion

    //region Helpers

    /**
     * Normally there should be no {@link BeaconSeen} entries in our database, but in the case the
     * library was stopped abruptly, this removes {@link BeaconSeen} entries from the {@link BeaconsSeenTable}
     * that are in the future (what happens when you restart your device as {@link SystemClock#elapsedRealtime()}
     * returns the time since boot.
     */
    private void removeObsoleteBeaconSeenEntries()
    {
        final String where = BeaconsSeenTable.COLUMN_TIMESTAMP + " > ? OR "
                + BeaconsSeenTable.COLUMN_TIMESTAMP + " < ?";
        final String[] selectionArgs = new String[]{String.valueOf(SystemClock.elapsedRealtime()), String.valueOf(SystemClock.elapsedRealtime() - this.postDelayedInMillis)};
        this.contentResolver.delete(BeaconsSeenProvider.CONTENT_URI, where, selectionArgs);
    }

    /**
     * Restarts countdown timers for entered {@link Beacon}'s that were not yet exited.
     */
    private void resumeExits()
    {
        final Cursor cursor = this.contentResolver.query(BeaconsSeenProvider.CONTENT_URI, null, null, null, null);
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

            for (final BeaconSeen beaconSeen : beaconSeens)
            {
                final Beacon beacon = new Beacon.Builder().setUUID(beaconSeen.getUuid())
                        .setMajor(beaconSeen.getMajor())
                        .setMinor(beaconSeen.getMinor())
                        .build();
                this.onExitHandler.passItem(beacon);
            }
        }
    }

    private static Uri getItemUri(@NonNull final Beacon beacon)
    {
        return Uri.withAppendedPath(BeaconsSeenProvider.CONTENT_URI_ITEM, beacon.getUUID().toString()
                + "/" + beacon.getMajor()
                + "/" + beacon.getMinor());
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
        this.contentResolver.delete(getItemUri(beacon), null, null);
    }

    //endregion
}
