package com.example.data.wishes

import com.example.model.BirthdayWish
import com.example.model.WishCategory
import com.example.model.WishLanguage
import com.example.model.WishStyle

object WishesRepository {

    val allWishes: List<BirthdayWish> by lazy {
        wishesFriends +
            wishesLove +
            wishesSpouse +
            wishesParents +
            wishesSiblings +
            wishesProfessional +
            wishesKids +
            wishesIslamicGeneral
    }

    private val wishesById: Map<Int, BirthdayWish> by lazy {
        allWishes.associateBy { it.id }
    }

    fun getWishById(id: Int): BirthdayWish? = wishesById[id]

    fun getWishesByCategory(category: WishCategory): List<BirthdayWish> {
        return allWishes.filter { it.category == category }
    }

    fun countByCategory(category: WishCategory): Int {
        return allWishes.count { it.category == category }
    }

    fun totalCount(): Int = allWishes.size

    fun searchAndFilter(
        query: String = "",
        category: WishCategory? = null,
        language: WishLanguage? = null,
        style: WishStyle? = null
    ): List<BirthdayWish> {
        val trimmedQuery = query.trim().lowercase()

        return allWishes.filter { wish ->
            val matchesCategory = category == null || wish.category == category
            val matchesLanguage = language == null || wish.language == language
            val matchesStyle = style == null || wish.style == style
            val matchesQuery = if (trimmedQuery.isEmpty()) {
                true
            } else {
                wish.text.lowercase().contains(trimmedQuery) ||
                    wish.category.title.lowercase().contains(trimmedQuery) ||
                    wish.style.label.lowercase().contains(trimmedQuery) ||
                    wish.language.label.lowercase().contains(trimmedQuery)
            }
            matchesCategory && matchesLanguage && matchesStyle && matchesQuery
        }
    }
}
