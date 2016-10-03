package mobi.inthepocket.android.beacons.ibeaconscanner.exceptions;

/**
 * Created by eliaslecomte on 03/10/2016.
 */

public class IllegalUUIDException extends IllegalArgumentException
{
    /**
     * Constructs an {@link IllegalUUIDException} with the specified
     * detail message.  A detail message is a String that describes
     * this particular exception.
     *
     * @param message the detail message.
     */
    public IllegalUUIDException(final String message)
    {
        super(message);
    }
}
