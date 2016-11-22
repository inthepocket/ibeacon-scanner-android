package mobi.inthepocket.android.beacons.app.utils;

import android.support.annotation.IdRes;
import android.support.test.espresso.ViewInteraction;
import android.widget.EditText;
import android.widget.TextView;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Created by eliaslecomte on 29/10/2016.
 */

public final class TextInputLayoutTestUtils
{
    private TextInputLayoutTestUtils()
    {
    }

    /**
     * @return The TextInputLayout's error view.
     */
    public static ViewInteraction onErrorViewWithinTilWithId(@IdRes final int textInputLayoutId)
    {
        return onView(allOf(isDescendantOfA(withId(textInputLayoutId)), not(isAssignableFrom(EditText.class)), isAssignableFrom(TextView.class)));
    }
}
