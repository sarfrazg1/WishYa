package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Entity(tableName = "birthdays")
data class Birthday(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val relationship: String,
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int? = null,
    val notes: String = "",
    val remindOnDay: Boolean = true,
    val remindOneDayBefore: Boolean = true,
    val remindHour: Int = 9,
    val remindMinute: Int = 0
) {
    /**
     * Calculates the next date this birthday occurs on or after [today].
     * Handles leap year (Feb 29 -> Feb 28 on non-leap years).
     */
    fun nextBirthday(today: LocalDate = LocalDate.now()): LocalDate {
        val currentYear = today.year
        val thisYearTarget = resolveDate(currentYear, birthMonth, birthDay)
        return if (!thisYearTarget.isBefore(today)) {
            thisYearTarget
        } else {
            resolveDate(currentYear + 1, birthMonth, birthDay)
        }
    }

    /**
     * Number of days until next birthday. 0 means today!
     */
    fun daysRemaining(today: LocalDate = LocalDate.now()): Long {
        val nextDate = nextBirthday(today)
        return ChronoUnit.DAYS.between(today, nextDate)
    }

    /**
     * True if the birthday is today.
     */
    fun isToday(today: LocalDate = LocalDate.now()): Boolean {
        return daysRemaining(today) == 0L
    }

    /**
     * Calculates age turning on the next birthday.
     * If birthYear is null, returns null.
     */
    fun ageTurning(today: LocalDate = LocalDate.now()): Int? {
        val year = birthYear ?: return null
        val nextDate = nextBirthday(today)
        return nextDate.year - year
    }

    /**
     * Formats birthday for display (e.g. "October 28" or "October 28, 1995")
     */
    fun formattedBirthDate(): String {
        val monthName = Month.of(birthMonth).name.lowercase().replaceFirstChar { it.uppercase() }
        return if (birthYear != null) {
            "$monthName $birthDay, $birthYear"
        } else {
            "$monthName $birthDay"
        }
    }

    /**
     * Returns the astrological Zodiac sign for the birthday.
     */
    fun zodiacSign(): String {
        return when (birthMonth) {
            1 -> if (birthDay < 20) "Capricorn ♑" else "Aquarius ♒"
            2 -> if (birthDay < 19) "Aquarius ♒" else "Pisces ♓"
            3 -> if (birthDay < 21) "Pisces ♓" else "Aries ♈"
            4 -> if (birthDay < 20) "Aries ♈" else "Taurus ♉"
            5 -> if (birthDay < 21) "Taurus ♉" else "Gemini ♊"
            6 -> if (birthDay < 21) "Gemini ♊" else "Cancer ♋"
            7 -> if (birthDay < 23) "Cancer ♋" else "Leo ♌"
            8 -> if (birthDay < 23) "Leo ♌" else "Virgo ♍"
            9 -> if (birthDay < 23) "Virgo ♍" else "Libra ♎"
            10 -> if (birthDay < 23) "Libra ♎" else "Scorpio ♏"
            11 -> if (birthDay < 22) "Scorpio ♏" else "Sagittarius ♐"
            12 -> if (birthDay < 22) "Sagittarius ♐" else "Capricorn ♑"
            else -> "🎂"
        }
    }

    private fun resolveDate(year: Int, month: Int, day: Int): LocalDate {
        return if (month == 2 && day == 29) {
            if (java.time.Year.isLeap(year.toLong())) {
                LocalDate.of(year, 2, 29)
            } else {
                LocalDate.of(year, 2, 28)
            }
        } else {
            LocalDate.of(year, month, day)
        }
    }
}
