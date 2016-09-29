package mobi.inthepocket.android.beacons.ibeaconscanner.handlers;

/**
 * Created by eliaslecomte on 28/09/2016.
 */

public class AddRegionsHandler extends TimeoutHandler<Object>
{
    public AddRegionsHandler(final TimeoutCallback<Object> timeoutCallback, final long timeoutInMillis)
    {
        super(timeoutCallback, timeoutInMillis);
    }
}
