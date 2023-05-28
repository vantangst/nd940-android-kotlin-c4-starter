package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    val reminders = mutableListOf<ReminderDTO>()
    var isError = false
    val errorMsg = "Reminder Error"

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (isError) Result.Error(errorMsg) else Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun deleteReminder(id: String): Boolean {
        return reminders.removeIf { it.id == id }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return try {
            return Result.Success(reminders.first { it.id == id })
        } catch (e: Exception) {
            Result.Error("Unexpected exception: ${e.localizedMessage}")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

}