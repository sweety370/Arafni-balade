package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wiki_cache")
data class CachedWikiArticle(
    @PrimaryKey
    val cacheKey: String,
    val title: String,
    val summary: String,
    val fullText: String,
    val imageUrl: String?,
    val articleUrl: String,
    val sectionsJson: String, // JSON array of Section(title, content)
    val language: String,     // "ar" or "en"
    val timestamp: Long = System.currentTimeMillis()
)
