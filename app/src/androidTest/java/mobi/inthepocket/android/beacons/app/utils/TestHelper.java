package mobi.inthepocket.android.beacons.app.utils;

/**
 * Created by eliaslecomte on 17/11/2016.
 */

public final class TestHelper
{
    private TestHelper()
    {
    }

    /**
     * Make the thread sleep for {@code millis}.
     *
     * @param millis how long the {@link Thread} will sleep
     */
    public static void sleep(final long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException("Cannot execute Thread.sleep()");
        }
    }
}
