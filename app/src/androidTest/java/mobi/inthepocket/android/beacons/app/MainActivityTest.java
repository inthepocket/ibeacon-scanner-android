package mobi.inthepocket.android.beacons.app;

import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import mobi.inthepocket.android.beacons.app.utils.MainActivityTestHelper;
import mobi.inthepocket.android.beacons.app.utils.PermissionTestHelper;

/**
 * Created by eliaslecomte on 29/10/2016.
 */
public class MainActivityTest
{
    @Rule
    public final ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    private MainActivityTestHelper mainActivityTestHelper;

    @Before
    public void setup()
    {
        this.mainActivityTestHelper = new MainActivityTestHelper(this.mainActivityActivityTestRule);
    }

    @Before
    public void acceptLocationPermission()
    {
        PermissionTestHelper.allowLocationPermissionWhenAsked();
    }

    //region Input tests

    @Test
    public void onCorrectUUIDInputTest()
    {
        this.mainActivityTestHelper.inputUUID("5419c45e-27ba-48c9-8fe4-5f577fedeb33");

        this.mainActivityTestHelper.clickOnButton(R.id.button_start);

        this.mainActivityTestHelper.assertUUIDHasNoErrorMessage();
    }

    @Test
    public void onWrongUUIDInputTest()
    {
        this.mainActivityTestHelper.inputUUID("invalid uuid");

        this.mainActivityTestHelper.clickOnButton(R.id.button_start);

        this.mainActivityTestHelper.assertUUIDHasErrorMessage();
    }

    @Test
    public void onWrongMajorTest()
    {
        this.mainActivityTestHelper.inputMajor("invalid major");

        this.mainActivityTestHelper.clickOnButton(R.id.button_start);

        this.mainActivityTestHelper.assertMajorHasErrorMessage();
    }

    @Test
    public void onWrongMinorTest()
    {
        this.mainActivityTestHelper.inputMinor("invalid minor");

        this.mainActivityTestHelper.clickOnButton(R.id.button_start);

        this.mainActivityTestHelper.assertMinorHasErrorMessage();
    }

    @Test
    public void onCorrectMajorTest()
    {
        this.mainActivityTestHelper.inputMajor("15");

        this.mainActivityTestHelper.clickOnButton(R.id.button_start);

        this.mainActivityTestHelper.assertMajorHasNoErrorMessage();
    }

    @Test
    public void onCorrectMinorTest()
    {
        this.mainActivityTestHelper.inputMinor("15");

        this.mainActivityTestHelper.clickOnButton(R.id.button_start);

        this.mainActivityTestHelper.assertMinorHasNoErrorMessage();
    }

    //endregion
}
