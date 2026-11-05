package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WikiCacheDao {
    @Query("SELECT * FROM wiki_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun getCachedArticle(key: String): CachedWikiArticle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedArticle(article: CachedWikiArticle)

    @Query("DELETE FROM wiki_cache WHERE timestamp < :cutoffTime")
    suspend fun clearOldCache(cutoffTime: Long)
}
