package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WishCategory(
    val id: String,
    val title: String,
    val shortName: String,
    val iconKey: String,
    val description: String
) {
    FRIENDS(
        id = "friends",
        title = "Friends & Best Friends",
        shortName = "Friends",
        iconKey = "friends",
        description = "Wishes for great friends, buddies & besties"
    ),
    LOVE(
        id = "love",
        title = "Love, Girlfriend & Boyfriend",
        shortName = "Love",
        iconKey = "love",
        description = "Romantic, sweet & passionate wishes"
    ),
    SPOUSE(
        id = "spouse",
        title = "Wife & Husband",
        shortName = "Spouse",
        iconKey = "spouse",
        description = "Heartwarming wishes for your life partner"
    ),
    PARENTS(
        id = "parents",
        title = "Mother & Father",
        shortName = "Parents",
        iconKey = "parents",
        description = "Respectful, emotional & loving messages"
    ),
    SIBLINGS(
        id = "siblings",
        title = "Brother & Sister",
        shortName = "Siblings",
        iconKey = "siblings",
        description = "Playful, caring & memorable messages"
    ),
    PROFESSIONAL(
        id = "professional",
        title = "Teacher, Boss & Colleagues",
        shortName = "Work & Mentors",
        iconKey = "work",
        description = "Respectful, elegant & professional greetings"
    ),
    KIDS(
        id = "kids",
        title = "Kids, Son & Daughter",
        shortName = "Kids",
        iconKey = "kids",
        description = "Cute, playful & cheerful wishes for little stars"
    ),
    ISLAMIC_GENERAL(
        id = "islamic_general",
        title = "Islamic, Relatives & General Wishes",
        shortName = "Islamic & Relatives",
        iconKey = "islamic",
        description = "Prayers, heartfelt duas, cousins & universal wishes"
    );

    companion object {
        fun fromId(id: String): WishCategory = entries.firstOrNull { it.id == id } ?: FRIENDS
    }
}

enum class WishLanguage(val id: String, val label: String) {
    ENGLISH("english", "English"),
    ROMAN_URDU("roman_urdu", "Roman Urdu")
}

enum class WishStyle(val id: String, val label: String) {
    ROMANTIC("romantic", "Romantic"),
    FUNNY("funny", "Funny"),
    EMOTIONAL("emotional", "Emotional"),
    SHORT("short", "Short"),
    FORMAL("formal", "Formal"),
    DUA("dua", "Dua / Prayer")
}

data class BirthdayWish(
    val id: Int,
    val category: WishCategory,
    val language: WishLanguage,
    val style: WishStyle,
    val text: String
) {
    /**
     * Replaces "{name}" placeholder with specified name, or cleans it naturally if no name is provided.
     */
    fun personalized(recipientName: String?): String {
        val clean = recipientName?.trim()
        return if (clean.isNullOrEmpty()) {
            cleanNaturalText()
        } else {
            text.replace("{name}", clean)
        }
    }

    /**
     * Returns a naturally worded message without raw placeholders or awkward punctuation.
     */
    fun cleanNaturalText(): String {
        return text
            .replace(", {name}!", "!")
            .replace(", {name}.", ".")
            .replace(", {name}?", "?")
            .replace(", {name}", "")
            .replace("{name}, ", "")
            .replace(" {name}!", "!")
            .replace(" {name}.", ".")
            .replace(" {name}?", "?")
            .replace(" {name}", "")
            .replace("{name}", "")
            .trim()
    }
}

@Entity(tableName = "favorite_wishes")
data class FavoriteWish(
    @PrimaryKey
    val wishId: Int,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recently_viewed_wishes")
data class RecentlyViewedWish(
    @PrimaryKey
    val wishId: Int,
    val viewedAt: Long = System.currentTimeMillis()
)
