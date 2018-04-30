package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link android.bluetooth.le.ScanCallback} that bridges the gap between API level 26's
 * {@link android.bluetooth.le.BluetoothLeScanner#startScan(List, ScanSettings, PendingIntent)}
 * & {@link android.bluetooth.le.BluetoothLeScanner#startScan(List, ScanSettings, ScanCallback)}.
 *
 * It tries to mimic the behaviour of sending a {@link PendingIntent} to the {@link BluetoothScanBroadcastReceiver}
 * on Android versions below O.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class BackportScanCallback extends ScanCallback
{
    private static final String TAG = "BackportScanCallback";

    private final Context context;
    private final Class<?> targetService;
    private final long exitTimeoutInMillis;

    BackportScanCallback(final Context context, final Class<?> targetService, final long exitTimeoutInMillis)
    {
        this.context = context;
        this.targetService = targetService;
        this.exitTimeoutInMillis = exitTimeoutInMillis;
    }

    @Override
    public void onScanResult(final int callbackType, final ScanResult result)
    {
        super.onScanResult(callbackType, result);

        Intent intent = this.constructBaseIntent();
        intent = this.appendScanResultParameters(intent, callbackType, result);

        this.sendBroadcastIntent(intent);
    }

    @Override
    public void onScanFailed(final int errorCode)
    {
        super.onScanFailed(errorCode);

        Intent intent = this.constructBaseIntent();
        intent = this.appendScanFailedParameters(intent, errorCode);

        this.sendBroadcastIntent(intent);
    }

    private Intent constructBaseIntent()
    {
        final Intent intent = new Intent(this.context, BluetoothScanBroadcastReceiver.class);
        intent.putExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_LAUNCH_SERVICE_CLASS_NAME, this.targetService.getName());
        intent.putExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_EXITED_TIMEOUT_MS, this.exitTimeoutInMillis);

        return intent;
    }

    private Intent appendScanResultParameters(@NonNull final Intent intent,
                                              final int callbackType,
                                              final ScanResult scanResult)
    {
        intent.putExtra(BluetoothScanBroadcastReceiver.CALLBACK_TYPE, callbackType);

        final ArrayList<ScanResult> scanResults = new ArrayList<>();
        scanResults.add(scanResult);
        intent.putParcelableArrayListExtra(BluetoothScanBroadcastReceiver.LIST_SCAN_RESULT, scanResults);

        return intent;
    }

    private Intent appendScanFailedParameters(@NonNull final Intent intent,
                                              final int errorCode)
    {
        intent.putExtra(BluetoothScanBroadcastReceiver.ERROR_CODE, errorCode);

        return intent;
    }

    private void sendBroadcastIntent(@NonNull final Intent intent)
    {
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        try
        {
            pendingIntent.send();
        }
        catch (final PendingIntent.CanceledException e)
        {
            Log.e(TAG, "Sending Broadcast intent was not possible: " + e.getMessage());
        }
    }
}
