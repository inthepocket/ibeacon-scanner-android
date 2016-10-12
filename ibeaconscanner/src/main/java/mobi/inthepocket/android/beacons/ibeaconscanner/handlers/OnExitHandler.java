package mobi.inthepocket.android.beacons.ibeaconscanner.handlers;

import android.support.annotation.NonNull;

import mobi.inthepocket.android.beacons.ibeaconscanner.Beacon;

/**
 * Created by eliaslecomte on 28/09/2016.
 *
 * This class generates {@link mobi.inthepocket.android.beacons.ibeaconscanner.handlers.TimeoutHandler.TimeoutCallback#timedOut(Object)}
 * when a region has not been entered for {@link TimeoutHandler#timeoutInMillis}.
 */

public class OnExitHandler extends TimeoutHandler<Beacon>
{
    public OnExitHandler(@NonNull final TimeoutCallback<Beacon> exitCallback, final long exitTimeoutInMillis)
    {
        super(exitCallback, exitTimeoutInMillis);
    }
}
