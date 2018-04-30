package mobi.inthepocket.android.beacons.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import mobi.inthepocket.android.beacons.app.R;
import mobi.inthepocket.android.beacons.ibeaconscanner.Beacon;
import mobi.inthepocket.android.beacons.ibeaconscanner.BluetoothScanBroadcastReceiver;

/**
 * Target service responsible for receiving beacon updates from the library (and communicating them).
 */
public class BeaconActivityService extends JobIntentService
{
    private static final String TAG = "BeaconActivityService";

    private static final int NOTIFICATION_ID = 65;

    /**
     * @see JobIntentService#onHandleWork(Intent)
     */
    @Override
    protected void onHandleWork(@NonNull final Intent intent)
    {
        final Beacon beacon = intent.getParcelableExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_BEACON_DETECTION);
        final boolean enteredBeacon = intent.getBooleanExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_BEACON_ENTERED, false);
        final boolean exitedBeacon = intent.getBooleanExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_BEACON_EXITED, false);

        if (beacon != null)
        {
            String logMessage = "";
            if (enteredBeacon)
            {
                Log.d(TAG, "entered beacon " + beacon.getUUID());
                logMessage = getString(R.string.notification_enter, beacon.getUUID(), beacon.getMajor(), beacon.getMinor());
            }
            else if (exitedBeacon)
            {
                Log.d(TAG, "exited beacon " + beacon.getUUID());
                logMessage = getString(R.string.notification_exit, beacon.getUUID(), beacon.getMajor(), beacon.getMinor());
            }

            // Create notification channel if required
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(new NotificationChannel(TAG, "Beacon Activity", NotificationManager.IMPORTANCE_LOW));
            }

            final Notification notification = new NotificationCompat.Builder(this, TAG)
                    .setAutoCancel(true)
                    .setContentText(logMessage)
                    .setContentTitle("Beacon activity")
                    .setGroup(TAG)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(logMessage))
                    .build();

            final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(TAG, NOTIFICATION_ID, notification);
        }
        else
        {
            // TODO: 14/03/2018 Add error support
            Toast.makeText(this, "Could not scan due to " + "an unknown error (TBA)", Toast.LENGTH_LONG).show();
        }
    }
}
