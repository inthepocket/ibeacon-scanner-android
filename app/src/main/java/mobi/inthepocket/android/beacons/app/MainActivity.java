package mobi.inthepocket.android.beacons.app;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import mobi.inthepocket.android.beacons.app.rxjava.RxObserver;
import mobi.inthepocket.android.beacons.ibeaconscanner.Error;
import mobi.inthepocket.android.beacons.ibeaconscanner.Region;
import mobi.inthepocket.android.beacons.ibeaconscanner.RegionManager;

public class MainActivity extends AppCompatActivity implements RegionManager.Callback
{
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String EXAMPLE_BEACON_1_UUID = "84be19d4-797d-11e5-8bcf-feff819cdc9f";
    private final static int EXAMPLE_BEACON_1_MAJOR = 1;
    private final static int EXAMPLE_BEACON_1_MINOR = 1;
    private final static String EXAMPLE_BEACON_2_UUID = "00000000-0000-0000-0000-000000000008";
    private final static int EXAMPLE_BEACON_2_MAJOR = 10;
    private final static int EXAMPLE_BEACON_2_MINOR = 8;

    private TextView textViewLog;
    private Button buttonAddFiveMore;
    private Button buttonStartScanning;
    private Button buttonStopScanning;

    private RegionManager regionManager;
    private int nextMinor = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.regionManager = RegionManager.getInstance();
        this.regionManager.setCallback(this);

        this.textViewLog = (TextView) this.findViewById(R.id.textview_log);
        this.buttonStartScanning = (Button) this.findViewById(R.id.button_start);
        this.buttonStartScanning.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {
                MainActivity.this.regionManager.startMonitoring(new Region.Builder()
                        .setUUID(EXAMPLE_BEACON_2_UUID)
                        .setMajor(EXAMPLE_BEACON_2_MAJOR)
                        .setMinor(EXAMPLE_BEACON_2_MINOR)
                        .build());
            }
        });
        this.buttonStopScanning= (Button) this.findViewById(R.id.button_stop);
        this.buttonStopScanning.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {
                MainActivity.this.nextMinor = 1;
                MainActivity.this.regionManager.stop();
            }
        });
        this.buttonAddFiveMore = (Button) this.findViewById(R.id.button_add_five_more);
        this.buttonAddFiveMore.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {
                final int till = MainActivity.this.nextMinor + 5;
                for (int i = MainActivity.this.nextMinor; i <= till ; i++)
                {
                    MainActivity.this.regionManager.startMonitoring(new Region.Builder()
                            .setUUID(EXAMPLE_BEACON_1_UUID)
                            .setMajor(EXAMPLE_BEACON_1_MAJOR)
                            .setMinor(i)
                            .build());
                }

                MainActivity.this.nextMinor = till+1;
            }
        });

        RxPermissions.getInstance(this)
                .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(new RxObserver<Boolean>()
                           {
                               @Override
                               public void onNext(final Boolean granted)
                               {
                                   if (granted)
                                   {
                                       MainActivity.this.beaconsEnabled(true);
                                   }
                                   else
                                   {
                                       MainActivity.this.beaconsEnabled(false);
                                       // Oops permission denied
                                   }
                               }
                           });


    }

    //region Callback

    @Override
    public void didEnterRegion(final Region region)
    {
        final String logMessage = String.format("Entered region with UUID %s and major %s and minor %s.", region.getUUID(), region.getMajor(), region.getMinor());
        this.updateLog(logMessage);
    }

    @Override
    public void didExitRegion(final Region region)
    {
        final String logMessage = String.format("Exited region with UUID %s and major %s and minor %s.", region.getUUID(), region.getMajor(), region.getMinor());
        this.updateLog(logMessage);
    }

    @Override
    public void monitoringDidFail(final Error error)
    {
        Toast.makeText(MainActivity.this, "Could not scan due to " + error.name(), Toast.LENGTH_LONG).show();
    }

    //endregion

    //region View

    private void beaconsEnabled(final boolean isEnabled)
    {
        this.buttonStartScanning.setEnabled(isEnabled);
        this.buttonStopScanning.setEnabled(isEnabled);
        this.buttonAddFiveMore.setEnabled(isEnabled);
    }

    private void updateLog(final String logMessage)
    {
        final String logText = this.textViewLog.getText().toString();

        this.textViewLog.setText(logMessage + "\n\n" + logText);
    }

    //endregion
}
