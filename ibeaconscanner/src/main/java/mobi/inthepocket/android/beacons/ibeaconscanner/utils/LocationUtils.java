package mobi.inthepocket.android.beacons.ibeaconscanner.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import java.security.InvalidParameterException;

/**
 * Created by eliaslecomte on 29/09/2016.
 */

public final class LocationUtils
{
    private LocationUtils()
    {
    }

    /**
     * Determine wether Location is turned on. Below Android M, will always return true.
     *
     * @param context to determine with if location is on
     * @return true if location is turned on or the sdk is below Android M.
     * @throws InvalidParameterException if {@code context} is null
     */
    public static boolean isLocationOn(final Context context)
    {
        if (context == null)
        {
            throw new InvalidParameterException("context is null");
        }

        return !isMarshmallowOrLater() || isLocationTurnedOn(context);
    }

    /**
     * @return true if the current sdk is above or equal to Android M
     */
    private static boolean isMarshmallowOrLater()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Determine if location is turned on.
     *
     * @param context where from you determine if location is turned on
     * @return true if location is turned on
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static boolean isLocationTurnedOn(final Context context)
    {
        final ContentResolver contentResolver = context.getContentResolver();
        final int mode = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);

        return (mode != Settings.Secure.LOCATION_MODE_OFF);
    }
}
