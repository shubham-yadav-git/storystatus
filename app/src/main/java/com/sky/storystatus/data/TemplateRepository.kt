package com.sky.storystatus.data

import android.content.Context
import android.util.Log
import com.sky.storystatus.model.StatusTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Repository interface for managing the StoryStatus template catalog.
 * Follows an offline-first architecture with thread-safe in-memory caching.
 */
interface TemplateRepository {
    suspend fun getAllTemplates(): List<StatusTemplate>
    suspend fun getTemplateById(id: String): StatusTemplate?
    suspend fun getFeaturedTemplates(): List<StatusTemplate>
    suspend fun getTemplatesByCategory(categoryId: String): List<StatusTemplate>
    suspend fun searchTemplates(query: String): List<StatusTemplate>
    suspend fun searchTemplates(
        query: String,
        categoryId: String?,
        language: String?
    ): List<StatusTemplate>
    suspend fun searchTemplates(
        query: String,
        categoryId: String?,
        language: String?,
        tag: String?
    ): List<StatusTemplate>
    fun getCategories(): List<String>
    fun getLanguages(): List<String>
    fun getTags(): List<String>

    @Deprecated("Use getAllTemplates instead", ReplaceWith("getAllTemplates()"))
    suspend fun loadTemplates(): List<StatusTemplate> = getAllTemplates()
}

class DefaultTemplateRepository(
    private val context: Context,
    private val parser: CatalogParser = CatalogParser()
) : TemplateRepository {

    private var cachedTemplates: List<StatusTemplate>? = null
    private val mutex = Mutex()

    override suspend fun getAllTemplates(): List<StatusTemplate> = withContext(Dispatchers.IO) {
        cachedTemplates?.let { return@withContext it }
        mutex.withLock {
            cachedTemplates?.let { return@withContext it }
            val loaded = try {
                val jsonString = context.assets.open("templates.json").bufferedReader().use { it.readText() }
                val parsed = parser.parseCatalog(jsonString)
                parsed.filter { template ->
                    template.id.isNotBlank() &&
                            template.title.isNotBlank() &&
                            template.category.isNotBlank() &&
                            template.textLayers.isNotEmpty()
                }
            } catch (e: Exception) {
                Log.e("TemplateRepository", "Failed to load bundled templates from assets", e)
                emptyList()
            }
            cachedTemplates = loaded
            loaded
        }
    }

    override suspend fun getTemplateById(id: String): StatusTemplate? {
        if (id.isBlank()) return null
        return getAllTemplates().find { it.id == id }
    }

    override suspend fun getFeaturedTemplates(): List<StatusTemplate> {
        val all = getAllTemplates()
        // 20 Curated standout launch templates representing distinct design styles across categories
        val featuredIds = listOf(
            "att_42", "att_46", "att_50", "att_13",
            "shayari_42", "shayari_44", "shayari_47", "shayari_16",
            "love_40", "love_42", "love_44",
            "mot_40", "mot_42", "mot_44",
            "friend_39", "friend_41",
            "sad_36", "sad_39",
            "bday_36", "bday_37",
            "fest_38", "fest_39", "fest_41"
        )
        val curated = featuredIds.mapNotNull { fId -> all.find { it.id == fId } }
        return if (curated.size >= 10) curated else all.take(20)
    }

    override suspend fun getTemplatesByCategory(categoryId: String): List<StatusTemplate> {
        val all = getAllTemplates()
        if (categoryId.isBlank() || categoryId.equals("All", ignoreCase = true)) {
            return all
        }
        return all.filter { it.category.equals(categoryId, ignoreCase = true) }
    }

    override suspend fun searchTemplates(query: String): List<StatusTemplate> {
        return searchTemplates(query, categoryId = null, language = null, tag = null)
    }

    override suspend fun searchTemplates(
        query: String,
        categoryId: String?,
        language: String?
    ): List<StatusTemplate> {
        return searchTemplates(query, categoryId = categoryId, language = language, tag = null)
    }

    override suspend fun searchTemplates(
        query: String,
        categoryId: String?,
        language: String?,
        tag: String?
    ): List<StatusTemplate> {
        val all = getAllTemplates()
        val cleanQuery = query.trim()

        return all.filter { template ->
            val matchesCategory = categoryId.isNullOrBlank() ||
                    categoryId.equals("All", ignoreCase = true) ||
                    template.category.equals(categoryId, ignoreCase = true)

            val matchesLanguage = language.isNullOrBlank() ||
                    language.equals("All", ignoreCase = true) ||
                    template.language.equals(language, ignoreCase = true)

            val matchesTag = tag.isNullOrBlank() ||
                    tag.equals("All", ignoreCase = true) ||
                    template.tags.any { it.equals(tag, ignoreCase = true) }

            val matchesQuery = cleanQuery.isEmpty() ||
                    template.title.contains(cleanQuery, ignoreCase = true) ||
                    template.category.contains(cleanQuery, ignoreCase = true) ||
                    template.language.contains(cleanQuery, ignoreCase = true) ||
                    template.tags.any { it.contains(cleanQuery, ignoreCase = true) } ||
                    template.textLayers.any { it.text.contains(cleanQuery, ignoreCase = true) }

            matchesCategory && matchesLanguage && matchesTag && matchesQuery
        }
    }

    override fun getCategories(): List<String> {
        return listOf(
            "Attitude",
            "Hindi Shayari",
            "Love",
            "Motivation",
            "Friendship",
            "Sad",
            "Birthday",
            "Festivals"
        )
    }

    override fun getLanguages(): List<String> {
        return listOf("All", "Hindi", "Hinglish", "English")
    }

    override fun getTags(): List<String> {
        return listOf(
            "All",
            "Minimal",
            "Luxury Gold",
            "Neon Cyber",
            "Editorial",
            "Vintage",
            "Typography",
            "Card Frame",
            "Poetic",
            "Romantic"
        )
    }
}

fun TemplateRepository(
    context: Context,
    parser: CatalogParser = CatalogParser()
): TemplateRepository {
    return DefaultTemplateRepository(context, parser)
}
