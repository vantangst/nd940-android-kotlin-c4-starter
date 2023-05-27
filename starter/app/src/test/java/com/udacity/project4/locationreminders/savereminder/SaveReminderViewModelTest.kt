package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.utils.getOrAwaitValue

import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.mp.KoinPlatform.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var reminderDataSource: FakeDataSource

    private lateinit var viewModel: SaveReminderViewModel

    private val fakeReminderItem: ReminderDataItem
        get() = ReminderDataItem(
            title = "Title",
            description = "description",
            location = "location name",
            latitude = 11.0,
            longitude = 10.0,
            id = "reminderId"
        )

    @Before
    fun setup() {
        //Stop Current Koin instance
        stopKoin()
        reminderDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            reminderDataSource
        )
    }

    @Test
    fun onClear_allReminderPropertyGotEmpty() {
        //Given
        viewModel.apply {
            reminderTitle.value = "title"
            reminderDescription.value = "description"
            reminderSelectedLocationStr.value = "name location"
            selectedPOI.value = PointOfInterest(LatLng(11.0, 10.0), "placeId", "name")
            latitude.value = 11.0
            longitude.value = 10.0
        }

        // When
        viewModel.onClear()

        //Then
        viewModel.run {
            Assert.assertTrue(reminderTitle.value.isNullOrEmpty())
            Assert.assertTrue(reminderDescription.value.isNullOrEmpty())
            Assert.assertTrue(reminderSelectedLocationStr.value.isNullOrEmpty())
            Assert.assertTrue(selectedPOI.value == null)
            Assert.assertTrue(latitude.value == null)
            Assert.assertTrue(longitude.value == null)
        }
    }

    @Test
    fun validateAndSaveReminder_emptyTitle_returnFalse() {
        // Given
        val reminderDataItem = fakeReminderItem.copy(title = "")

        // When
        val actual = viewModel.validateAndSaveReminder(reminderDataItem)

        // Then
        Assert.assertFalse(actual)
        Assert.assertEquals(R.string.err_enter_title, viewModel.showSnackBarInt.getOrAwaitValue())
    }

    @Test
    fun validateAndSaveReminder_emptyLocation_returnFalse() {
        // Given
        val reminderDataItem = fakeReminderItem.copy(location = "")

        // When
        val actual = viewModel.validateAndSaveReminder(reminderDataItem)

        // Then
        Assert.assertFalse(actual)
        Assert.assertEquals(
            R.string.err_select_location,
            viewModel.showSnackBarInt.getOrAwaitValue()
        )
    }

    @Test
    fun validateAndSaveReminder_validData_returnTrueAndSuccessful() = runTest {
        // Given
        val reminderDataItem = fakeReminderItem

        // When
        // Pause dispatcher so we can verify initial values
        // Main dispatcher will not run coroutines eagerly for this test
        Dispatchers.setMain(StandardTestDispatcher())

        val actual = viewModel.validateAndSaveReminder(reminderDataItem)


        // Then
        Assert.assertEquals(
            true,
            viewModel.showLoading.getOrAwaitValue()
        )
        // Execute pending coroutine actions
        // Wait until coroutine in viewModel.validateAndSaveReminder() complete
        advanceUntilIdle()

        Assert.assertEquals(
            false,
            viewModel.showLoading.getOrAwaitValue()
        )
        Assert.assertTrue(actual)
        Assert.assertTrue(reminderDataSource.reminders.size > 0)
    }

    @Test
    fun removeReminder_withReminderId_removedThatReminderItem() = runTest {
        // Given
        val reminderDataItem = fakeReminderItem
        viewModel.validateAndSaveReminder(reminderDataItem)
        Assert.assertTrue(reminderDataSource.getReminder(reminderDataItem.id) is Result.Success)

        // When
        viewModel.removeReminder(reminderDataItem.id)

        // Then
        Assert.assertTrue(reminderDataSource.getReminder(reminderDataItem.id) is Result.Error)
    }

}