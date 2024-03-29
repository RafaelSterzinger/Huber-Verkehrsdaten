package com.example.huber;


import android.content.SharedPreferences;
import android.os.Build;
import android.os.RemoteException;
import android.view.View;

import androidx.preference.PreferenceManager;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.example.huber.MainActivity.CAMERA_LAT;
import static com.example.huber.MainActivity.CAMERA_LON;
import static com.example.huber.MainActivity.CAMERA_ZOOM;
import static com.example.huber.MainActivity.CURRENT_SELECTION;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.isOneOf;

@RunWith(AndroidJUnit4.class)
public class ExampleHuberMainActivityTest {

    @Rule
    public final ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    private static ViewAction typeSearchViewText() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(MaterialSearchBar.class));
            }

            @Override
            public String getDescription() {
                return "Change searchBar query";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((MaterialSearchBar) view).setText("Karlsplatz");
            }
        };
    }

    @Before
    public void setUp() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activityTestRule.getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(CAMERA_LAT);
        editor.remove(CAMERA_LON);
        editor.remove(CAMERA_ZOOM);
        editor.remove(CURRENT_SELECTION);
        editor.apply();

        if (Build.VERSION.SDK_INT >= 23) {
            UiObject allowPermissions = UiDevice.getInstance(getInstrumentation()).findObject(new UiSelector().text("ALLOW"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    System.out.println("No permission dialog found.");
                }
            }
        }

        if (Math.random() > 0.5) {
            try {
                UiDevice.getInstance(getInstrumentation()).setOrientationLeft();
                Thread.sleep(1000);
            } catch (RemoteException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    //Test 1
    public void whenClickOnFavoritesThenDisplayCurrentFavorites() throws InterruptedException {
        //GIVEN
        ((SlidingUpPanelLayout) activityTestRule.getActivity().findViewById(R.id.sliding_up_panel)).setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

        // Await slide up panel animation
        Thread.sleep(3000);

        //WHEN
        onView(withId(R.id.favorites)).perform(click());

        // Await content switch
        Thread.sleep(3000);

        //THEN
        onView(withId(R.id.scrollView)).check(matches(hasChildCount(1)));
        onView(withText("Karlsplatz")).check(matches(isDisplayed()));
    }

    @Test
    //Test 2
    public void whenClickingAndEnteringTextInSearchbarThenResultsAreShownAndSlidePanelIsHiddenCollapsed() throws InterruptedException {
        //GIVEN
        ((SlidingUpPanelLayout) activityTestRule.getActivity().findViewById(R.id.sliding_up_panel)).setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

        // Await slide up panel animation
        Thread.sleep(3000);

        //WHEN
        onView(withId(R.id.searchBar)).perform(click());
        onView(withId(R.id.searchBar)).perform(typeSearchViewText());

        //THEN
        assertThat(((SlidingUpPanelLayout) activityTestRule.getActivity().findViewById(R.id.sliding_up_panel)).getPanelState(), isOneOf(SlidingUpPanelLayout.PanelState.COLLAPSED, SlidingUpPanelLayout.PanelState.HIDDEN));
        onView(allOf(withText("Bösendorferstraße/Karlsplatz U"), hasSibling(withText("Oper/Karlsplatz U")), hasSibling(withText("Karlsplatz")))).check(matches(isDisplayed()));
        onView(allOf(withText("Karlsplatz"), hasSibling(withText("Oper/Karlsplatz U")), hasSibling(withText("Bösendorferstraße/Karlsplatz U")))).check(matches(isDisplayed()));
        onView(allOf(withText("Oper/Karlsplatz U"), hasSibling(withText("Bösendorferstraße/Karlsplatz U")), hasSibling(withText("Karlsplatz")))).check(matches(isDisplayed()));
    }
}


