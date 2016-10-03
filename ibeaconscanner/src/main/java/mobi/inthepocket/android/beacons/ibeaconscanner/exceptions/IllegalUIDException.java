package mobi.inthepocket.android.beacons.ibeaconscanner.exceptions;

/**
 * Created by eliaslecomte on 03/10/2016.
 */

public class IllegalUIDException extends IllegalArgumentException
{
    public IllegalUIDException()
    {
        super("Uuid is not set");
    }
}
