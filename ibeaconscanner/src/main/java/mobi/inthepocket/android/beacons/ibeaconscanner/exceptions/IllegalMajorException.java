package mobi.inthepocket.android.beacons.ibeaconscanner.exceptions;

/**
 * Created by eliaslecomte on 03/10/2016.
 */

public class IllegalMajorException extends IllegalArgumentException
{
    public IllegalMajorException()
    {
        super("Major should be a number from 0 to 65535.");
    }
}
