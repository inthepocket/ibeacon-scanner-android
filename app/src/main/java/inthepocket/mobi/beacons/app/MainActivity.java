package inthepocket.mobi.beacons.app;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.UUID;

import inthepocket.mobi.beacons.ibeaconscanning.Error;
import inthepocket.mobi.beacons.ibeaconscanning.Region;
import inthepocket.mobi.beacons.ibeaconscanning.RegionManager;
import rx.Observer;

public class MainActivity extends AppCompatActivity implements RegionManager.Callback
{
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String EXAMPLE_BEACON_1_UUID = "84be19d4-797d-11e5-8bcf-feff819cdc9f";
    private final static int EXAMPLE_BEACON_1_MAJOR = 1;
    private final static int EXAMPLE_BEACON_1_MINOR = 2;

    private TextView textViewLog;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.textViewLog = (TextView) this.findViewById(R.id.textview_log);

        RxPermissions.getInstance(this)
                .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(new Observer<Boolean>()
                           {
                               @Override
                               public void onCompleted()
                               {

                               }

                               @Override
                               public void onError(final Throwable e)
                               {

                               }

                               @Override
                               public void onNext(final Boolean granted)
                               {
                                   if (granted)
                                   {
                                       final Region region = new Region.RegionBuilder()
                                               .setId1(UUID.fromString(EXAMPLE_BEACON_1_UUID))
                                               .setId2(EXAMPLE_BEACON_1_MAJOR)
                                               .setId3(EXAMPLE_BEACON_1_MINOR).build();

                                       final RegionManager regionManager = new RegionManager();
                                       regionManager.startMonitoring(region, MainActivity.this);
                                   }
                                   else
                                   {
                                       // Oops permission denied
                                   }
                               }
                           });


    }

    //region Callback

    @Override
    public void didEnterRegion(final Region region)
    {
        final String logMessage = String.format("Entered region with UUID %s and major %s and minor %s.", region.getId1(), region.getId2(), region.getId3());
        Log.d(TAG, logMessage);

        final String logText = MainActivity.this.textViewLog.getText() != null ? MainActivity.this.textViewLog.getText().toString() : "";

        MainActivity.this.textViewLog.setText(logMessage + "\n" + logText);

    }

    @Override
    public void didExitRegion(final Region region)
    {

    }

    @Override
    public void monitoringDidFail(final Error error)
    {

    }

    //endregion
}
