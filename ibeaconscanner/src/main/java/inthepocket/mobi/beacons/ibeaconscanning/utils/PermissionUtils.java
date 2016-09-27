package inthepocket.mobi.beacons.ibeaconscanning.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.security.InvalidParameterException;

/**
 * PermissionUtils to help determine if a particular permission is granted
 * <p/>
 * Created by eliaslecomte on 04/04/16.
 */

public final class PermissionUtils
{

    private PermissionUtils()
    {
    }

    /**
     * Determine whether the provided context has been granted any location permission (ACCESS_COARSE_LOCATION or
     * ACCESS_FINE_LOCATION). Below Android M, will always return true.
     *
     * @param context    where from you determine if a permission is granted
     * @return true if you have any location permission or the sdk is below Android M.
     */
    public static boolean isLocationGranted(final Context context)
    {
        if (context == null)
        {
            throw new InvalidParameterException("context is null");
        }

        return (!isMarshmallow()
                || (isPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION) || isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)));
    }

    /**
     * @return true if the current sdk is above or equal to Android M
     */
    private static boolean isMarshmallow()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Determine whether the provided context has been granted a particular permission.
     *
     * @param context    where from you determine if a permission is granted
     * @param permission you want to determinie if it is granted
     * @return true if you have the permission, or false if not
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static boolean isPermissionGranted(final Context context, final String permission)
    {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
}
