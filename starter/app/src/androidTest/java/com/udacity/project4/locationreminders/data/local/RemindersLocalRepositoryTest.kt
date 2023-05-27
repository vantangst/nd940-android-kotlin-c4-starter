package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var reminderDataSource: ReminderDataSource
    private lateinit var database: RemindersDatabase

    private val fakeReminderDTO: ReminderDTO
        get() = ReminderDTO(
            title = "Title",
            description = "description",
            location = "location name",
            latitude = 11.0,
            longitude = 10.0,
            id = "reminderId"
        )

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
        .build()

        reminderDataSource = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_getReminder() = runBlocking {
        // Given
        reminderDataSource.saveReminder(fakeReminderDTO)

        // When
        val result = reminderDataSource.getReminder(fakeReminderDTO.id)

        // Then
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.id, `is`(fakeReminderDTO.id))
        assertThat(result.data.title, `is`(fakeReminderDTO.title))
        assertThat(result.data.description, `is`(fakeReminderDTO.description))
        assertThat(result.data.location, `is`(fakeReminderDTO.location))
        assertThat(result.data.latitude, `is`(fakeReminderDTO.latitude))
        assertThat(result.data.longitude, `is`(fakeReminderDTO.longitude))
    }

    @Test
    fun getReminderById_notExistingId_returnNotFound() = runBlocking {
        // Given
        reminderDataSource.deleteAllReminders()

        // When
        val result = reminderDataSource.getReminder(fakeReminderDTO.id)

        // Then
        assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun deleteReminderById_existingId_returnTrue() = runBlocking {
        // Given
        reminderDataSource.saveReminder(fakeReminderDTO)

        // When
        val result = reminderDataSource.deleteReminder(fakeReminderDTO.id)

        // Then
        assertThat(result, `is`(true))
    }

    @Test
    fun deleteReminderById_notExistingId_returnFalse() = runBlocking {
        // Given
        reminderDataSource.deleteAllReminders()

        // When
        val result = reminderDataSource.deleteReminder(fakeReminderDTO.id)

        // Then
        assertThat(result, `is`(false))
    }

    @Test
    fun getAllReminder_notExistingAnyItem_returnSuccessEmptyList() = runBlocking {
        // Given
        reminderDataSource.deleteAllReminders()

        // When
        val result = reminderDataSource.getReminders()

        // Then
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.isEmpty(), `is`(true))
    }

    @Test
    fun getAllReminder_existingItem_returnSuccessDataList() = runBlocking {
        // Given
        reminderDataSource.saveReminder(fakeReminderDTO)
        reminderDataSource.saveReminder(fakeReminderDTO.copy(id = "id_2"))

        // When
        val result = reminderDataSource.getReminders()

        // Then
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.size, `is`(2))
    }

    @Test
    fun deleteAllReminder() = runBlocking {
        // Given
        reminderDataSource.saveReminder(fakeReminderDTO)
        reminderDataSource.saveReminder(fakeReminderDTO.copy(id = "id_2"))

        // When
        reminderDataSource.deleteAllReminders()

        val resultCheck = reminderDataSource.getReminders()

        // Then
        assertThat(resultCheck is Result.Success, `is`(true))
        resultCheck as Result.Success
        assertThat(resultCheck.data.isEmpty(), `is`(true))
    }

}