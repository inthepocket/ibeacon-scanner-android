package mobi.inthepocket.android.beacons.app.utils;

import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import mobi.inthepocket.android.beacons.ibeaconscanner.utils.PermissionUtils;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Validates if you have a runtime permission. If it still has to be approved, will click the dialog.
 * From https://gist.github.com/rocboronat/65b1187a9fca9eabfebb5121d818a3c4.
 */

public final class PermissionTestHelper
{
    private static final int PERMISSIONS_DIALOG_DELAY = 1000;
    private static final int GRANT_BUTTON_INDEX = 1;

    private PermissionTestHelper()
    {
    }

    /**
     * Allows location permission if the runtime permission dialog is shown.
     */
    public static void allowLocationPermissionWhenAsked()
    {
        try
        {
            if (!PermissionUtils.isLocationGranted(InstrumentationRegistry.getTargetContext()))
            {
                TestHelper.sleep(PERMISSIONS_DIALOG_DELAY);

                final UiDevice device = UiDevice.getInstance(getInstrumentation());
                final UiObject allowPermissions = device.findObject(new UiSelector()
                                                                            .clickable(true)
                                                                            .checkable(false)
                                                                            .index(GRANT_BUTTON_INDEX));
                if (allowPermissions.exists())
                {
                    allowPermissions.click();
                }
            }
        }
        catch (final UiObjectNotFoundException e)
        {
            Log.e(PermissionTestHelper.class.getSimpleName(), "There is no permissions dialog to interact with.");
        }
    }
}
