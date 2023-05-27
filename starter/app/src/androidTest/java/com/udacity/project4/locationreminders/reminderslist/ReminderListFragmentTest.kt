package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.util.base.BaseUITest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : BaseUITest() {

    // test the navigation of the fragments.
    @Test
    fun clickAddButton_navigateToSaveReminderFragment() = runBlocking {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            it.view?.let { view ->
                Navigation.setViewNavController(view, navController)
            }
        }

        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
        scenario.close()
    }


    // test the displayed data on the UI.

    @Test
    fun openFragment_shouldDisplayRemindersIntoRecyclerView() = runTest {
        repository.saveReminder(fakeReminderDTO)
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)

        onView(withId(R.id.reminderssRecyclerView)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.reminderssRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )
        scenario.close()
    }

    //add testing for the error messages.
    @Test
    fun openFragment_EmptyReminderData_shouldDisplayNoData() = runTest {
        repository.deleteAllReminders()
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(isDisplayed()))
        scenario.close()
    }
}