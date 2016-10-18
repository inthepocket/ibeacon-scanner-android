package mobi.inthepocket.android.beacons.ibeaconscanner.handlers;

/**
 * Created by eliaslecomte on 28/09/2016.
 */

public class AddBeaconsHandler extends TimeoutHandler<Object>
{
    public AddBeaconsHandler(final TimeoutCallback<Object> timeoutCallback, final long timeoutInMillis)
    {
        super(timeoutCallback, timeoutInMillis);
    }
}
