package mobi.inthepocket.android.beacons.app;

import android.Manifest;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mobi.inthepocket.android.beacons.app.rxjava.RxObserver;
import mobi.inthepocket.android.beacons.app.utils.InputFilterMinMax;
import mobi.inthepocket.android.beacons.app.utils.UUIDUtils;
import mobi.inthepocket.android.beacons.ibeaconscanner.Beacon;
import mobi.inthepocket.android.beacons.ibeaconscanner.Error;
import mobi.inthepocket.android.beacons.ibeaconscanner.IBeaconScanner;

public class MainActivity extends AppCompatActivity implements IBeaconScanner.Callback
{
    @BindView(R.id.textview_log)
    TextView textViewLog;
    @BindView(R.id.textinputlayout_uuid)
    TextInputLayout textInputLayoutUuid;
    @BindView(R.id.edittext_uuid)
    TextInputEditText editTextUuid;
    @BindView(R.id.textinputlayout_major)
    TextInputLayout textInputLayoutMajor;
    @BindView(R.id.edittext_major)
    TextInputEditText editTextMajor;
    @BindView(R.id.textinputlayout_minor)
    TextInputLayout textInputLayoutMinor;
    @BindView(R.id.edittext_minor)
    TextInputEditText editTextMinor;
    @BindView(R.id.button_start)
    Button buttonStartScanning;
    @BindView(R.id.button_stop)
    Button buttonStopScanning;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        IBeaconScanner.getInstance().setCallback(this);

        // add input filters on {@link EditText}s.
        this.editTextMajor.setFilters(new InputFilter[]{new InputFilterMinMax(1, 65535)});
        this.editTextMinor.setFilters(new InputFilter[]{new InputFilterMinMax(1, 65535)});

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
                        } else
                        {
                            MainActivity.this.beaconsEnabled(false);
                            // Oh no permission denied
                        }
                    }
                });
    }

    /**
     * Start monitoring for {@link Beacon} provided with UI.
     */
    @OnClick(R.id.button_start)
    public void onButtonStartClicked()
    {
        if (this.isValid())
        {
            IBeaconScanner.getInstance().startMonitoring(Beacon.newBuilder()
                    .setUUID(this.editTextUuid.getText().toString())
                    .setMajor(Integer.valueOf(this.editTextMajor.getText().toString()))
                    .setMinor(Integer.valueOf(this.editTextMinor.getText().toString()))
                    .build());
        }
    }

    /**
     * Stop all beacon monitoring.
     */
    @OnClick(R.id.button_stop)
    public void onButtonStopClicked()
    {
        IBeaconScanner.getInstance().stop();
    }

    //region Callback

    @Override
    public void didEnterBeacon(final Beacon beacon)
    {
        final String logMessage = String.format("Entered beacon with UUID %s and major %s and minor %s.", beacon.getUUID(), beacon.getMajor(), beacon.getMinor());
        this.updateLog(logMessage);
    }

    @Override
    public void didExitBeacon(final Beacon beacon)
    {
        final String logMessage = String.format("Exited beacon with UUID %s and major %s and minor %s.", beacon.getUUID(), beacon.getMajor(), beacon.getMinor());
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
    }

    private boolean isValid()
    {
        boolean isValid = true;

        final String uuid = this.editTextUuid.getText().toString();
        if (!UUIDUtils.isValidUUID(uuid))
        {
            isValid = false;
            this.textInputLayoutUuid.setError(this.getString(R.string.uuid_error));
        }
        else
        {
            this.textInputLayoutUuid.setError(null);
        }

        final String major = this.editTextMajor.getText().toString();
        if (TextUtils.isEmpty(major))
        {
            isValid = false;
            this.textInputLayoutMajor.setError(this.getString(R.string.major_error));
        }
        else
        {
            this.textInputLayoutMajor.setError(null);
        }

        final String minor = this.editTextMinor.getText().toString();
        if (TextUtils.isEmpty(minor))
        {
            isValid = false;
            this.textInputLayoutMinor.setError(this.getString(R.string.minor_error));
        }
        else
        {
            this.textInputLayoutMinor.setError(null);
        }

        return isValid;
    }

    private void updateLog(final String logMessage)
    {
        final String logText = this.textViewLog.getText().toString();

        this.textViewLog.setText(logMessage + "\n\n" + logText);
    }

    //endregion
}
