package mobi.inthepocket.android.beacons.ibeaconscanner.exceptions;

/**
 * Created by eliaslecomte on 03/10/2016.
 */

public class IllegalUUID2Exception extends IllegalArgumentException
{
    public IllegalUUID2Exception()
    {
        super("Uuid is not set");
    }
}
