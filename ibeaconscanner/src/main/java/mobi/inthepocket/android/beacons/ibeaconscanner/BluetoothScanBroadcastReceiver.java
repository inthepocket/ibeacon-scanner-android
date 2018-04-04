package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.annotation.TargetApi;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.JobIntentService;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import mobi.inthepocket.android.beacons.ibeaconscanner.database.BeaconsSeenManager;
import mobi.inthepocket.android.beacons.ibeaconscanner.database.BeaconsSeenProvider;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.BeaconUtils;

/**
 * Receives and processes the result of a bluetooth scan.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothScanBroadcastReceiver extends BroadcastReceiver
{
    private static final int JOB_ID = 9859;

    /**
     * Backport of {@link BluetoothLeScanner#EXTRA_CALLBACK_TYPE}
     */
    public static final String CALLBACK_TYPE = "android.bluetooth.le.extra.CALLBACK_TYPE";
    /**
     * Backport of {@link BluetoothLeScanner#EXTRA_LIST_SCAN_RESULT}
     */
    public static final String LIST_SCAN_RESULT = "android.bluetooth.le.extra.LIST_SCAN_RESULT";
    /**
     * Backport of {@link BluetoothLeScanner#EXTRA_ERROR_CODE}
     */
    public static final String ERROR_CODE = "android.bluetooth.le.extra.ERROR_CODE";

    /**
     * {@link JobIntentService} class to post the beacons found to.
     */
    public static final String IBEACON_SCAN_LAUNCH_SERVICE_CLASS_NAME = "ibeacon_scan_service_class_name_to_launch";
    /**
     * Timeout after which a beacon is considered exited.
     */
    public static final String IBEACON_SCAN_EXITED_TIMEOUT_MS = "ibeacon_scan_exited_timeout";
    /**
     * Tag used to report the beacon updated during a scan.
     */
    public static final String IBEACON_SCAN_BEACON_DETECTION = "ibeacon_scan_beacon_detection";
    /**
     * Tag used to report that a beacon has been detected during a scan.
     */
    public static final String IBEACON_SCAN_BEACON_ENTERED = "ibeacon_scan_beacon_detection_enter";
    /**
     * Tag used to report that a beacon was no longer detected during a scan.
     */
    public static final String IBEACON_SCAN_BEACON_EXITED = "ibeacon_scan_beacon_detection_exit";

    private static final String TAG = "BluetoothScanBrdcstRcvr";
    private BeaconsSeenManager beaconsSeenManager;
    private Context context;
    private Class<?> targetService;
    private FirebaseJobDispatcher dispatcher;
    private long beaconExitTimeoutInMillis;

    /**
     * @see BroadcastReceiver#onReceive(Context, Intent)
     */
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        this.dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        this.beaconExitTimeoutInMillis = intent.getLongExtra(IBEACON_SCAN_EXITED_TIMEOUT_MS, BuildConfig.BEACON_EXIT_TIME_IN_MILLIS);
        final BeaconsSeenProvider provider = new BeaconsSeenProvider(context);
        this.beaconsSeenManager = new BeaconsSeenManager(provider, this.beaconExitTimeoutInMillis);
        this.context = context;
        final String className = intent.getStringExtra(IBEACON_SCAN_LAUNCH_SERVICE_CLASS_NAME);
        try
        {
            this.targetService = TextUtils.isEmpty(className) ? null : Class.forName(className);
        }
        catch (final ClassNotFoundException e)
        {
            Log.e(TAG, "Target class was not found. Ensure you're passing the fully qualified namespace.", e);
        }

        final int bleCallbackType = intent.getIntExtra(this.getCallbackIntentKey(), -1);
        if (bleCallbackType != -1)
        {
            final int errorCode = intent.getIntExtra(this.getErrorCodeIntentKey(), -1); // e.g.  ScanCallback.SCAN_FAILED_INTERNAL_ERROR
            if (errorCode != -1)
            {
                Log.w(TAG, "Passive background scan failed.  Code; " + errorCode);
            }
            final Iterable<ScanResult> scanResults = intent.getParcelableArrayListExtra(this.getListScanResultIntentKey());
            for (final ScanResult scanResult : scanResults)
            {
                this.processScanResult(scanResult);
            }
        }

        provider.destroy();
    }

    private void processScanResult(final ScanResult scanResult)
    {
        Log.d(TAG, "In the range of beacon: " + scanResult.toString());

        // get Scan Record byte array (Be warned, this can be null)
        if (scanResult.getScanRecord() != null)
        {
            final Pair<Boolean, Integer> isBeacon = BeaconUtils.isBeaconPattern(scanResult);

            if (isBeacon.first)
            {
                final Beacon beacon = BeaconUtils.createBeaconFromScanRecord(scanResult.getScanRecord().getBytes(), isBeacon.second);

                // see if the beacon was not yet triggered
                if (!this.beaconsSeenManager.hasBeaconBeenTriggered(beacon))
                {
                    // this beacon is not yet in our database, trigger {@link IBeaconScanner.Callback#didEnterBeacon}
                    if (this.targetService != null)
                    {
                        final Intent targetServiceIntent = new Intent(this.context, this.targetService);
                        targetServiceIntent.putExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_BEACON_DETECTION, beacon);
                        targetServiceIntent.putExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_BEACON_ENTERED, true);
                        JobIntentService.enqueueWork(this.context, this.targetService, JOB_ID, targetServiceIntent);
                        Log.d(TAG, "Target service notified about beacon enter " + beacon.getUUID());
                    }

                    // add the enter beacon to database
                    this.beaconsSeenManager.addBeaconToDatabase(beacon);

                    // schedule beacon timeout
                    Log.d(TAG, "Beacon triggered, scheduled exit JobService");
                    final int windowStart = (int) (this.beaconExitTimeoutInMillis / 1000L);
                    final Bundle extras = OnExitJobService.buildExtras(beacon, this.beaconExitTimeoutInMillis, this.targetService);

                    this.dispatcher.mustSchedule(this.dispatcher.newJobBuilder()
                            .setExtras(extras)
                            .setLifetime(Lifetime.FOREVER)
                            .setService(OnExitJobService.class)
                            .setRecurring(false)
                            .setReplaceCurrent(true)
                            .setTag(beacon.getUUID().toString())
                            .setTrigger(Trigger.executionWindow(windowStart, windowStart + 1))
                            .build());

                }
                else
                {
                    Log.d(TAG, "beacon not triggered");
                }
            }
        }
    }

    //region Private helpers

    private String getCallbackIntentKey()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? BluetoothLeScanner.EXTRA_CALLBACK_TYPE
                : CALLBACK_TYPE;
    }

    private String getErrorCodeIntentKey()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? BluetoothLeScanner.EXTRA_ERROR_CODE
                : ERROR_CODE;
    }

    private String getListScanResultIntentKey()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT
                : LIST_SCAN_RESULT;
    }

    //endregion
}
