package com.example.data

import com.example.model.Birthday
import com.example.model.FavoriteWish
import com.example.model.RecentlyViewedWish
import kotlinx.coroutines.flow.Flow

class BirthdayRepository(
    private val birthdayDao: BirthdayDao,
    private val wishDao: WishDao
) {
    val allBirthdays: Flow<List<Birthday>> = birthdayDao.getAllBirthdays()

    suspend fun getBirthdayById(id: Long): Birthday? = birthdayDao.getBirthdayById(id)

    suspend fun insertBirthday(birthday: Birthday): Long = birthdayDao.insertBirthday(birthday)

    suspend fun updateBirthday(birthday: Birthday) = birthdayDao.updateBirthday(birthday)

    suspend fun deleteBirthday(birthday: Birthday) = birthdayDao.deleteBirthday(birthday)

    suspend fun deleteAllBirthdays() = birthdayDao.deleteAllBirthdays()

    val favoriteWishIds: Flow<List<Int>> = wishDao.getFavoriteWishIds()

    suspend fun toggleFavorite(wishId: Int) {
        if (wishDao.isFavorite(wishId)) {
            wishDao.deleteFavorite(wishId)
        } else {
            wishDao.insertFavorite(FavoriteWish(wishId = wishId))
        }
    }

    suspend fun isFavorite(wishId: Int): Boolean = wishDao.isFavorite(wishId)

    val recentlyViewedWishIds: Flow<List<Int>> = wishDao.getRecentlyViewedWishIds(10)

    suspend fun recordRecentlyViewed(wishId: Int) {
        wishDao.insertRecentlyViewed(RecentlyViewedWish(wishId = wishId))
    }

    suspend fun clearFavorites() = wishDao.clearFavorites()
}
