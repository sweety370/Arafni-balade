package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAtTimestamp DESC")
    fun getAllFavorites(): Flow<List<FavoritePlaceEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE placeId = :placeId)")
    fun isFavorite(placeId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoritePlaceEntity)

    @Query("DELETE FROM favorites WHERE placeId = :placeId")
    suspend fun deleteFavoriteById(placeId: String)

    @Query("UPDATE favorites SET userNote = :note WHERE placeId = :placeId")
    suspend fun updateNote(placeId: String, note: String)
}
