package mobi.inthepocket.android.beacons.app.utils;


import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;

import mobi.inthepocket.android.beacons.app.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
/**
 * Created by eliaslecomte on 29/10/2016.
 */

public class MainActivityTestHelper
{
    private final ActivityTestRule activityTestRule;

    public MainActivityTestHelper(final ActivityTestRule activityTestRule)
    {
        this.activityTestRule = activityTestRule;
    }

    public void inputUUID(final String value)
    {
        onView(ViewMatchers.withId(R.id.edittext_uuid)).perform(replaceText(value)).perform(closeSoftKeyboard());
    }

    public void inputMajor(final String value)
    {
        onView(ViewMatchers.withId(R.id.edittext_major)).perform(replaceText(value)).perform(closeSoftKeyboard());
    }

    public void inputMinor(final String value)
    {
        onView(ViewMatchers.withId(R.id.edittext_minor)).perform(replaceText(value)).perform(closeSoftKeyboard());
    }

    public void assertUUIDHasNoErrorMessage()
    {
        TextInputLayoutTestUtils.onErrorViewWithinTilWithId(R.id.textinputlayout_uuid).check(doesNotExist());
    }

    public void assertUUIDHasErrorMessage()
    {
        final String expectedError = this.activityTestRule.getActivity().getString(R.string.uuid_error);
        TextInputLayoutTestUtils.onErrorViewWithinTilWithId(R.id.textinputlayout_uuid).check(matches(withText(expectedError)));
    }

    public void assertMajorHasErrorMessage()
    {
        final String expectedError = this.activityTestRule.getActivity().getString(R.string.major_error);
        TextInputLayoutTestUtils.onErrorViewWithinTilWithId(R.id.textinputlayout_major).check(matches(withText(expectedError)));
    }

    public void assertMajorHasNoErrorMessage()
    {
        TextInputLayoutTestUtils.onErrorViewWithinTilWithId(R.id.textinputlayout_major).check(doesNotExist());
    }

    public void assertMinorHasErrorMessage()
    {
        final String expectedError = this.activityTestRule.getActivity().getString(R.string.minor_error);
        TextInputLayoutTestUtils.onErrorViewWithinTilWithId(R.id.textinputlayout_minor).check(matches(withText(expectedError)));
    }

    public void assertMinorHasNoErrorMessage()
    {
        TextInputLayoutTestUtils.onErrorViewWithinTilWithId(R.id.textinputlayout_minor).check(doesNotExist());
    }

    public void clickOnButton(final int viewId)
    {
        onView(withId(viewId)).perform(click());
    }
}
