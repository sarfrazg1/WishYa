package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.FavoriteWish
import com.example.model.RecentlyViewedWish
import kotlinx.coroutines.flow.Flow

@Dao
interface WishDao {
    @Query("SELECT wishId FROM favorite_wishes ORDER BY savedAt DESC")
    fun getFavoriteWishIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favoriteWish: FavoriteWish)

    @Query("DELETE FROM favorite_wishes WHERE wishId = :wishId")
    suspend fun deleteFavorite(wishId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_wishes WHERE wishId = :wishId)")
    suspend fun isFavorite(wishId: Int): Boolean

    @Query("SELECT wishId FROM recently_viewed_wishes ORDER BY viewedAt DESC LIMIT :limit")
    fun getRecentlyViewedWishIds(limit: Int = 10): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentlyViewed(recentlyViewed: RecentlyViewedWish)

    @Query("DELETE FROM favorite_wishes")
    suspend fun clearFavorites()
}
