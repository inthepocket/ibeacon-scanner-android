package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import mobi.inthepocket.android.beacons.ibeaconscanner.database.BeaconsSeenManager;
import mobi.inthepocket.android.beacons.ibeaconscanner.database.BeaconsSeenProvider;

/**
 * Removes the beacons from the database and communicates this to a target service (given by client).
 * <p>
 * Should only be triggered
 * - when the beacon is no longer detected
 * - after a beacon has timed out - see {@link com.firebase.jobdispatcher.FirebaseJobDispatcher}.
 */
public class OnExitJobService extends JobService
{
    private static final int JOB_ID = 9859;
    private static final String EXIT_BEACON_UUID = "beacon_to_exit_UUID";
    private static final String EXIT_BEACON_MAJOR = "beacon_to_exit_major_value";
    private static final String EXIT_BEACON_MINOR = "beacon_to_exit_minor_value";
    private static final String EXIT_CALLBACK_SERVICE_CLASS_NAME = "callback_service_class_name";
    private static final String EXIT_TIMEOUT = "timeout_for_beacon";
    private static final String TAG = "OnExitJobService";

    private BeaconsSeenProvider beaconsSeenProvider;

    /**
     * Put all relevant info into a bundle to use when launching this service.
     *
     * @param beacon        Beacon to exit
     * @param exitTimeout   Timeout for beacon
     * @param targetService Service to launch when beacon is exited
     * @return A {@link Bundle} containing all info required for exiting the beacon at a later time
     * and notifying a subscribed client.
     */
    public static Bundle buildExtras(@NonNull final Beacon beacon, final Long exitTimeout, @Nullable final Class<?> targetService)
    {
        final Bundle extras = new Bundle();
        extras.putString(EXIT_BEACON_UUID, beacon.getUUID().toString());
        extras.putInt(EXIT_BEACON_MAJOR, beacon.getMajor());
        extras.putInt(EXIT_BEACON_MINOR, beacon.getMinor());
        if (targetService != null)
        {
            extras.putString(EXIT_CALLBACK_SERVICE_CLASS_NAME, targetService.getName());
        }
        extras.putLong(EXIT_TIMEOUT, exitTimeout);

        return extras;
    }

    /**
     * @see JobService#onStartJob(JobParameters)
     */
    @Override
    public boolean onStartJob(final JobParameters job)
    {
        Log.d(TAG, "OnExitJobService#onStartJob");
        if (job.getExtras() != null && job.getExtras().containsKey(EXIT_BEACON_UUID))
        {
            final String uuid = job.getExtras().getString(EXIT_BEACON_UUID);

            if (!TextUtils.isEmpty(uuid))
            {
                final int major = job.getExtras().getInt(EXIT_BEACON_MAJOR);
                final int minor = job.getExtras().getInt(EXIT_BEACON_MINOR);
                final long timeout = job.getExtras().getLong(EXIT_TIMEOUT, BuildConfig.BEACON_EXIT_TIME_IN_MILLIS);

                this.beaconsSeenProvider = new BeaconsSeenProvider(this.getApplicationContext());
                final BeaconsSeenManager manager = new BeaconsSeenManager(this.beaconsSeenProvider, timeout);
                final Beacon beacon = manager.getBeaconFromDatabase(uuid, major, minor);

                if (beacon != null)
                {
                    manager.removeBeaconFromDatabase(beacon);
                    Log.d(TAG, "Removed beacon " + beacon.getUUID() + " from database");

                    final String className = job.getExtras().getString(EXIT_CALLBACK_SERVICE_CLASS_NAME);
                    final Class<?> targetService;
                    try
                    {
                        targetService = TextUtils.isEmpty(className) ? null : Class.forName(className);
                        if (targetService != null)
                        {
                            final Intent targetServiceIntent = new Intent(getApplicationContext(), targetService);
                            targetServiceIntent.putExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_LAUNCH_SERVICE_CLASS_NAME, targetService.getName());
                            targetServiceIntent.putExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_BEACON_DETECTION, beacon);
                            targetServiceIntent.putExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_BEACON_EXITED, true);
                            JobIntentService.enqueueWork(getApplicationContext(), targetService, JOB_ID, targetServiceIntent);
                            Log.d(TAG, "Target service notified about beacon exit " + beacon.getUUID());
                        }
                    }
                    catch (ClassNotFoundException e)
                    {
                        Log.e(TAG, "Target class was not found. Ensure you're passing the fully qualified namespace.", e);
                    }
                }
            }
        }
        else
        {
            Log.w(TAG, "Could not remove beacon on timeout; received null as a beacon.");
        }

        return false;
    }

    /**
     * @see JobService#onStopJob(JobParameters)
     */
    @Override
    public boolean onStopJob(final JobParameters job)
    {
        Log.d(TAG, "OnExitJobService#onStopJob");
        if (this.beaconsSeenProvider != null)
        {
            this.beaconsSeenProvider.destroy();
        }

        return false;
    }
}
