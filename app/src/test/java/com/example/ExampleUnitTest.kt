package com.example

import com.example.data.wishes.WishesRepository
import com.example.model.Birthday
import com.example.model.WishCategory
import com.example.model.WishLanguage
import com.example.model.WishStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WishYaUnitTest {

    @Test
    fun wishes_totalCountAndCategoryRequirementsMet() {
        val allWishes = WishesRepository.allWishes
        assertTrue("Total wishes must be at least 600, found: ${allWishes.size}", allWishes.size >= 600)

        // Check each category minimum count
        val friendsCount = allWishes.count { it.category == WishCategory.FRIENDS }
        val loveCount = allWishes.count { it.category == WishCategory.LOVE }
        val spouseCount = allWishes.count { it.category == WishCategory.SPOUSE }
        val parentsCount = allWishes.count { it.category == WishCategory.PARENTS }
        val siblingsCount = allWishes.count { it.category == WishCategory.SIBLINGS }
        val profCount = allWishes.count { it.category == WishCategory.PROFESSIONAL }
        val kidsCount = allWishes.count { it.category == WishCategory.KIDS }
        val islamicGeneralCount = allWishes.count { it.category == WishCategory.ISLAMIC_GENERAL }

        assertTrue("Friends min 80, found $friendsCount", friendsCount >= 80)
        assertTrue("Love min 80, found $loveCount", loveCount >= 80)
        assertTrue("Spouse min 70, found $spouseCount", spouseCount >= 70)
        assertTrue("Parents min 70, found $parentsCount", parentsCount >= 70)
        assertTrue("Siblings min 60, found $siblingsCount", siblingsCount >= 60)
        assertTrue("Professional min 60, found $profCount", profCount >= 60)
        assertTrue("Kids min 50, found $kidsCount", kidsCount >= 50)
        assertTrue("Islamic & General min 130, found $islamicGeneralCount", islamicGeneralCount >= 130)

        // Verify all IDs are unique
        val uniqueIds = allWishes.map { it.id }.toSet()
        assertEquals("All wish IDs must be unique", allWishes.size, uniqueIds.size)

        // Verify all wish texts are unique
        val uniqueTexts = allWishes.map { it.text.trim().lowercase() }.toSet()
        assertEquals("All wish texts must be unique", allWishes.size, uniqueTexts.size)

        // Verify both English and Roman Urdu are included
        val englishCount = allWishes.count { it.language == WishLanguage.ENGLISH }
        val romanUrduCount = allWishes.count { it.language == WishLanguage.ROMAN_URDU }
        assertTrue("Must have rich English wishes: $englishCount", englishCount >= 200)
        assertTrue("Must have rich Roman Urdu wishes: $romanUrduCount", romanUrduCount >= 200)
    }

    @Test
    fun birthday_dateCalculations_todayAndNextYear() {
        val today = LocalDate.of(2026, 10, 25)

        // 1. Birthday is today
        val birthdayToday = Birthday(
            name = "Test Person",
            relationship = "Friend",
            birthDay = 25,
            birthMonth = 10,
            birthYear = 1996
        )
        assertEquals(0L, birthdayToday.daysRemaining(today))
        assertTrue(birthdayToday.isToday(today))
        assertEquals(30, birthdayToday.ageTurning(today))
        assertEquals(today, birthdayToday.nextBirthday(today))

        // 2. Birthday later this year
        val birthdayLater = Birthday(
            name = "Alice",
            relationship = "Best Friend",
            birthDay = 30,
            birthMonth = 10,
            birthYear = 2000
        )
        assertEquals(5L, birthdayLater.daysRemaining(today))
        assertFalse(birthdayLater.isToday(today))
        assertEquals(LocalDate.of(2026, 10, 30), birthdayLater.nextBirthday(today))
        assertEquals(26, birthdayLater.ageTurning(today))

        // 3. Birthday already passed this year (e.g. Jan 15) -> rolls over to next year
        val birthdayPassed = Birthday(
            name = "Bob",
            relationship = "Brother",
            birthDay = 15,
            birthMonth = 1,
            birthYear = 1995
        )
        val nextBday = birthdayPassed.nextBirthday(today)
        assertEquals(LocalDate.of(2027, 1, 15), nextBday)
        assertEquals(32, birthdayPassed.ageTurning(today))
    }

    @Test
    fun birthday_leapYearHandling() {
        // Feb 29 on non-leap year (2026 is non-leap year) -> resolves to Feb 28
        val leapBirthday = Birthday(
            name = "Leap Baby",
            relationship = "Cousin",
            birthDay = 29,
            birthMonth = 2,
            birthYear = 2000
        )
        val today = LocalDate.of(2026, 1, 1)
        val nextBday = leapBirthday.nextBirthday(today)
        assertEquals(LocalDate.of(2026, 2, 28), nextBday)
    }

    @Test
    fun wishes_personalizationReplacement() {
        val wish = WishesRepository.allWishes.first { it.text.contains("{name}") }
        val personalized = wish.personalized("Ahmad")
        assertFalse(personalized.contains("{name}"))
        assertTrue(personalized.contains("Ahmad"))

        val fallback = wish.personalized("")
        assertFalse(fallback.contains("{name}"))
        assertEquals(wish.cleanNaturalText(), fallback)
    }

    @Test
    fun wishes_searchAndFiltering() {
        val islamicResults = WishesRepository.searchAndFilter(category = WishCategory.ISLAMIC_GENERAL)
        assertTrue(islamicResults.isNotEmpty())
        assertTrue(islamicResults.all { it.category == WishCategory.ISLAMIC_GENERAL })

        val romanUrduResults = WishesRepository.searchAndFilter(language = WishLanguage.ROMAN_URDU)
        assertTrue(romanUrduResults.isNotEmpty())
        assertTrue(romanUrduResults.all { it.language == WishLanguage.ROMAN_URDU })

        val funnyResults = WishesRepository.searchAndFilter(style = WishStyle.FUNNY)
        assertTrue(funnyResults.isNotEmpty())
        assertTrue(funnyResults.all { it.style == WishStyle.FUNNY })

        val textSearchResults = WishesRepository.searchAndFilter(query = "salgirah")
        assertTrue(textSearchResults.isNotEmpty())
    }
}
