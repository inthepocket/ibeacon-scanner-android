package mobi.inthepocket.android.beacons.ibeaconscanner.exceptions;

/**
 * Created by eliaslecomte on 03/10/2016.
 */

public class IllegalMinorException extends IllegalArgumentException
{
    public IllegalMinorException()
    {
        super("Minor should be a number from 0 to 65535.");
    }
}
