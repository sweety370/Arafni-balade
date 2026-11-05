package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoritePlaceEntity(
    @PrimaryKey
    val placeId: String,
    val nameAr: String,
    val governorateAr: String,
    val categoryName: String,
    val addedAtTimestamp: Long = System.currentTimeMillis(),
    val userNote: String = ""
)
