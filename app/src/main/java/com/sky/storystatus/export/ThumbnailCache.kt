package com.sky.storystatus.export

import android.graphics.Bitmap

/**
 * High-performance, thread-safe memory-bounded LRU cache for rendered template thumbnails.
 * Generously sized with 350 entries to hold the entire template catalog in memory,
 * eliminating repeated rendering, preventing scroll stutter, and maintaining full
 * compatibility with local JVM unit test runners.
 */
object ThumbnailCache {
    private const val MAX_ENTRIES = 350

    private val cache = object : LinkedHashMap<String, Bitmap>(MAX_ENTRIES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
            return size > MAX_ENTRIES
        }
    }

    @Synchronized
    fun get(key: String): Bitmap? = cache[key]

    @Synchronized
    fun put(key: String, bitmap: Bitmap) {
        cache[key] = bitmap
    }

    @Synchronized
    fun contains(key: String): Boolean = cache.containsKey(key)

    @Synchronized
    fun clear() {
        cache.clear()
    }

    @Synchronized
    fun size(): Int = cache.size
}
