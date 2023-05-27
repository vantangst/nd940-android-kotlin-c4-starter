package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

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
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runTest {
        // Given
        database.reminderDao().saveReminder(fakeReminderDTO)

        // When
        val actual = database.reminderDao().getReminderById(fakeReminderDTO.id)

        // Then
        assertThat(actual as ReminderDTO, notNullValue())
        assertThat(actual.id, `is`(fakeReminderDTO.id))
        assertThat(actual.title, `is`(fakeReminderDTO.title))
        assertThat(actual.description, `is`(fakeReminderDTO.description))
        assertThat(actual.location, `is`(fakeReminderDTO.location))
        assertThat(actual.latitude, `is`(fakeReminderDTO.latitude))
        assertThat(actual.longitude, `is`(fakeReminderDTO.longitude))
    }

    @Test
    fun insertExistingReminder() = runTest {
        // Given
        database.reminderDao().saveReminder(fakeReminderDTO)
        val reminderSameId = fakeReminderDTO.copy(title = "Title  Existing")

        // When
        database.reminderDao().saveReminder(reminderSameId)
        val actual = database.reminderDao().getReminderById(reminderSameId.id)

        // Then
        assertThat(actual as ReminderDTO, notNullValue())
        assertThat(actual.id, `is`(reminderSameId.id))
        assertThat(actual.title, `is`(reminderSameId.title))
        assertThat(actual.description, `is`(reminderSameId.description))
        assertThat(actual.location, `is`(reminderSameId.location))
        assertThat(actual.latitude, `is`(reminderSameId.latitude))
        assertThat(actual.longitude, `is`(reminderSameId.longitude))
    }

    @Test
    fun deleteReminderById() = runTest {
        // Given
        database.reminderDao().saveReminder(fakeReminderDTO)

        // When
        val actual = database.reminderDao().deleteReminderById(fakeReminderDTO.id)

        // Then
        assert(actual > 0)
        val reminder = database.reminderDao().getReminderById(fakeReminderDTO.id)
        assertThat(reminder, nullValue())
    }

    @Test
    fun deleteAllReminder() = runTest {
        // Given
        database.reminderDao().saveReminder(fakeReminderDTO)

        // When
        database.reminderDao().deleteAllReminders()

        // Then
        val actual = database.reminderDao().getReminders()
        assertThat(actual, `is`(listOf()))
    }

    @Test
    fun getAllReminder() = runTest {
        // Given
        database.reminderDao().saveReminder(fakeReminderDTO)

        // When
        val actual = database.reminderDao().getReminders()

        // Then
        assert(actual.isNotEmpty())
    }

}