package com.udacity.project4

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.base.BaseUITest
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest : BaseUITest() {

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    // Note: We are assuming 2 points
    // 1. You have logged in
    // 2. That device location is ready for use (Location is On and permission allowed)
    @Test
    fun createNewReminder_giveValidInput_shouldAddedAndDisplayOnRecycleView() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.google_map)).perform(longClick())

        onView(withId(R.id.btn_save)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(
            clearText(),
            typeText(fakeReminderDTO.title),
            closeSoftKeyboard()
        )

        onView(withId(R.id.reminderDescription)).perform(
            clearText(),
            typeText(fakeReminderDTO.description),
            closeSoftKeyboard()
        )

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withId(R.id.reminderssRecyclerView)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        activityScenario.close()
    }

    @Test
    fun createNewReminder_selectionLocation_giveLocationEmpty_shouldErrorToast() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.selectLocation)).perform(click())

        activityScenario.onActivity {
            it?.onBackPressedDispatcher?.onBackPressed()
        }

        onView(withId(R.id.reminderTitle)).perform(
            clearText(),
            typeText(fakeReminderDTO.title),
            closeSoftKeyboard()
        )

        onView(withId(R.id.reminderDescription)).perform(
            clearText(),
            typeText(fakeReminderDTO.description),
            closeSoftKeyboard()
        )

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText(R.string.err_select_location)).check(matches(isDisplayed()))

        activityScenario.close()
    }

}
