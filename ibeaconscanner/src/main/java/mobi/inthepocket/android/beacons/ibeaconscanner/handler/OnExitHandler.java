package mobi.inthepocket.android.beacons.ibeaconscanner.handler;

import android.support.annotation.NonNull;

import mobi.inthepocket.android.beacons.ibeaconscanner.Region;

/**
 * Created by eliaslecomte on 28/09/2016.
 *
 * This class generates {@link mobi.inthepocket.android.beacons.ibeaconscanner.handler.TimeoutHandler.TimeoutCallback#timedOut(Object)}
 * when a region has not been entered for {@link TimeoutHandler#timeoutInMillis}.
 */

public class OnExitHandler extends TimeoutHandler<Region>
{
    public OnExitHandler(@NonNull final TimeoutCallback<Region> exitCallback, final long exitTimeoutInMillis)
    {
        super(exitCallback, exitTimeoutInMillis);
    }
}
