package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.mp.KoinPlatform

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var reminderDataSource: FakeDataSource

    private lateinit var viewModel: RemindersListViewModel

    private val fakeReminderDTO: ReminderDTO
        get() = ReminderDTO(
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
        KoinPlatform.stopKoin()
        reminderDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            reminderDataSource
        )
    }

    @Test
    fun clearAllReminder_reminderDataSourceShouldEmpty() = runTest {
        // Given
        reminderDataSource.saveReminder(fakeReminderDTO)

        // When
        viewModel.clearAllReminder()

        // Then
        Assert.assertTrue(reminderDataSource.reminders.isEmpty())
    }

    @Test
    fun loadReminders_errorDataSource_showErrorMsg() = runTest {
        // Given
        reminderDataSource.isError = true

        // When
        viewModel.loadReminders()

        // Then
        Assert.assertEquals(
            reminderDataSource.errorMsg,
            viewModel.showSnackBar.getOrAwaitValue()
        )
    }

    @Test
    fun loadReminders_displayLoadingCorrectly() = runTest {
        // Given

        // When
        // Pause dispatcher so we can verify initial values
        // Main dispatcher will not run coroutines eagerly for this test
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel.loadReminders()

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
    }

    @Test
    fun loadReminders_withInvalidateShowNoDataIsTrueDataSourceError_displayLoadingCorrectlyAndShowNoDataAndErrorMsg() = runTest {
        // Given
        reminderDataSource.isError = true

        // When
        // Pause dispatcher so we can verify initial values
        // Main dispatcher will not run coroutines eagerly for this test
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel.loadReminders()

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
        Assert.assertEquals(
            true,
            viewModel.showNoData.getOrAwaitValue()
        )
        Assert.assertEquals(
            null,
            viewModel.remindersList.value
        )
        Assert.assertEquals(
            reminderDataSource.errorMsg,
            viewModel.showSnackBar.getOrAwaitValue()
        )
    }

    @Test
    fun loadReminders_withInvalidateShowNoDataIsTrueAndEmptyDataSource_displayLoadingCorrectlyAndShowNoData() = runTest {
        // Given
        reminderDataSource.deleteAllReminders()

        // When
        // Pause dispatcher so we can verify initial values
        // Main dispatcher will not run coroutines eagerly for this test
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel.loadReminders()

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
        Assert.assertEquals(
            true,
            viewModel.showNoData.getOrAwaitValue()
        )
        Assert.assertEquals(
            0,
            viewModel.remindersList.value?.size
        )
        Assert.assertEquals(
            null,
            viewModel.showSnackBar.value
        )

    }

    @Test
    fun loadReminders_withInvalidateShowNoDataIsFalse_displayLoadingCorrectlyAndShowListData() = runTest {
        // Given
        reminderDataSource.saveReminder(fakeReminderDTO)

        // When
        // Pause dispatcher so we can verify initial values
        // Main dispatcher will not run coroutines eagerly for this test
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel.loadReminders()

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
        Assert.assertEquals(
            false,
            viewModel.showNoData.getOrAwaitValue()
        )
        Assert.assertEquals(
            1,
            viewModel.remindersList.getOrAwaitValue().size
        )
        Assert.assertEquals(
            null,
            viewModel.showSnackBar.value
        )
    }

}