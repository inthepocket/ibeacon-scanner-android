package mobi.inthepocket.android.beacons.ibeaconscanner.exceptions;

/**
 * Created by eliaslecomte on 03/10/2016.
 */

public class IllegalUUIDException extends IllegalArgumentException
{
    public IllegalUUIDException()
    {
        super("Uuid is not set");
    }
}
