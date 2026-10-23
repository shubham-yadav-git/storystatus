package com.sky.storystatus

import com.sky.storystatus.data.CatalogParser
import com.sky.storystatus.data.DefaultTemplateRepository
import com.sky.storystatus.export.ThumbnailCache
import com.sky.storystatus.model.StatusTemplate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class TemplateRepositoryTest {

    private lateinit var allTemplates: List<StatusTemplate>

    @Before
    fun setup() {
        val file = File("src/main/assets/templates.json")
        assertTrue("Asset file must exist", file.exists())
        val parser = CatalogParser()
        allTemplates = parser.parseCatalog(file.readText())
    }

    @Test
    fun testCatalogTotalCountAndUniqueIds() {
        assertEquals("Catalog should contain exactly 361 templates", 361, allTemplates.size)

        val ids = allTemplates.map { it.id }
        val uniqueIds = ids.toSet()
        assertEquals("Every template must have a unique ID", uniqueIds.size, ids.size)

        allTemplates.forEach { template ->
            assertTrue("ID must not be blank", template.id.isNotBlank())
            assertTrue("Title must not be blank", template.title.isNotBlank())
            assertTrue("Category must not be blank", template.category.isNotBlank())
            assertTrue("Language must not be blank", template.language.isNotBlank())
            assertTrue("Template must have at least one text layer", template.textLayers.isNotEmpty())
        }
    }

    @Test
    fun testCategoryDistribution() {
        val counts = allTemplates.groupingBy { it.category }.eachCount()

        assertEquals(54, counts["Attitude"])
        assertEquals(50, counts["Hindi Shayari"])
        assertEquals(45, counts["Love"])
        assertEquals(45, counts["Motivation"])
        assertEquals(42, counts["Friendship"])
        assertEquals(40, counts["Sad"])
        assertEquals(40, counts["Birthday"])
        assertEquals(45, counts["Festivals"])
    }

    @Test
    fun testCategoryFiltering() {
        val attitude = allTemplates.filter { it.category.equals("Attitude", ignoreCase = true) }
        assertEquals(54, attitude.size)

        val shayari = allTemplates.filter { it.category.equals("Hindi Shayari", ignoreCase = true) }
        assertEquals(50, shayari.size)

        val love = allTemplates.filter { it.category.equals("Love", ignoreCase = true) }
        assertEquals(45, love.size)

        val motivation = allTemplates.filter { it.category.equals("Motivation", ignoreCase = true) }
        assertEquals(45, motivation.size)

        val festivals = allTemplates.filter { it.category.equals("Festivals", ignoreCase = true) }
        assertEquals(45, festivals.size)
    }

    @Test
    fun testCaseInsensitiveSearch() {
        fun search(query: String): List<StatusTemplate> {
            val q = query.trim()
            return allTemplates.filter { t ->
                t.title.contains(q, ignoreCase = true) ||
                        t.category.contains(q, ignoreCase = true) ||
                        t.language.contains(q, ignoreCase = true) ||
                        t.tags.any { it.contains(q, ignoreCase = true) } ||
                        t.textLayers.any { it.text.contains(q, ignoreCase = true) }
            }
        }

        val lowerResults = search("attitude")
        val upperResults = search("ATTITUDE")
        val mixedResults = search("AttiTude")

        assertTrue(lowerResults.isNotEmpty())
        assertEquals(lowerResults.size, upperResults.size)
        assertEquals(lowerResults.size, mixedResults.size)

        val sigmaResults = search("sigma")
        assertTrue("Should find sigma template", sigmaResults.any { it.id == "att_3" })
    }

    @Test
    fun testUnicodeAndHindiSearch() {
        fun search(query: String): List<StatusTemplate> {
            val q = query.trim()
            return allTemplates.filter { t ->
                t.title.contains(q, ignoreCase = true) ||
                        t.category.contains(q, ignoreCase = true) ||
                        t.language.contains(q, ignoreCase = true) ||
                        t.tags.any { it.contains(q, ignoreCase = true) } ||
                        t.textLayers.any { it.text.contains(q, ignoreCase = true) }
            }
        }

        // Test Ghalib search in Hindi
        val ghalibResults = search("ग़ालिब")
        assertTrue("Should find Mirza Ghalib template with Hindi keyword", ghalibResults.isNotEmpty())
        assertTrue(ghalibResults.any { it.id == "shayari_1" })

        // Test Deepavali festival search in Hindi
        val diwaliResults = search("दीपावली")
        assertTrue("Should find Diwali templates with Devanagari query", diwaliResults.isNotEmpty())

        // Test Hausla / Motivation quote in Hindi
        val hauslaResults = search("हौसलों")
        assertTrue("Should find motivation quote with Devanagari query", hauslaResults.isNotEmpty())

        // Test Aukaat / Attitude quote in Hindi
        val aukaatResults = search("औकात")
        assertTrue("Should find attitude quote with Devanagari query", aukaatResults.isNotEmpty())
    }

    @Test
    fun testLanguageFiltering() {
        val hindiTemplates = allTemplates.filter { it.language.equals("Hindi", ignoreCase = true) }
        val hinglishTemplates = allTemplates.filter { it.language.equals("Hinglish", ignoreCase = true) }
        val englishTemplates = allTemplates.filter { it.language.equals("English", ignoreCase = true) }

        assertTrue("Should contain Hindi templates", hindiTemplates.isNotEmpty())
        assertTrue("Should contain Hinglish templates", hinglishTemplates.isNotEmpty())
        assertTrue("Should contain English templates", englishTemplates.isNotEmpty())
        assertEquals(361, hindiTemplates.size + hinglishTemplates.size + englishTemplates.size)
    }

    @Test
    fun testTagFilteringAndAesthetics() {
        val luxuryGold = allTemplates.filter { it.tags.any { tag -> tag.equals("Luxury Gold", ignoreCase = true) } }
        assertTrue("Should contain Luxury Gold templates", luxuryGold.isNotEmpty())

        val minimal = allTemplates.filter { it.tags.any { tag -> tag.equals("Minimal", ignoreCase = true) } }
        assertTrue("Should contain Minimal templates", minimal.isNotEmpty())

        val neonCyber = allTemplates.filter { it.tags.any { tag -> tag.equals("Neon Cyber", ignoreCase = true) } }
        assertTrue("Should contain Neon Cyber templates", neonCyber.isNotEmpty())

        val editorial = allTemplates.filter { it.tags.any { tag -> tag.equals("Editorial", ignoreCase = true) } }
        assertTrue("Should contain Editorial templates", editorial.isNotEmpty())
    }

    @Test
    fun testPlaceholderSyntax() {
        val birthdayTemplatesWithPlaceholders = allTemplates.filter { template ->
            template.textLayers.any { it.text.contains("{{name}}") || it.text.contains("{{age}}") }
        }
        assertTrue("Should contain birthday templates with editable placeholders", birthdayTemplatesWithPlaceholders.isNotEmpty())
    }

    @Test
    fun testEmptySearchResults() {
        fun search(query: String): List<StatusTemplate> {
            val q = query.trim()
            return allTemplates.filter { t ->
                t.title.contains(q, ignoreCase = true) ||
                        t.category.contains(q, ignoreCase = true) ||
                        t.language.contains(q, ignoreCase = true) ||
                        t.tags.any { it.contains(q, ignoreCase = true) } ||
                        t.textLayers.any { it.text.contains(q, ignoreCase = true) }
            }
        }

        val emptyResults = search("unmatched_random_keyword_xyz_999")
        assertTrue("Unmatched query should return empty list gracefully", emptyResults.isEmpty())
    }

    @Test
    fun testDeterministicCuratedFeaturedTemplates() {
        val curatedStandoutIds = listOf(
            "att_13", "shayari_13", "love_13", "mot_13",
            "fest_13", "friend_13", "sad_13", "bday_13",
            "att_17", "shayari_17", "love_17", "mot_17",
            "fest_17", "friend_17", "sad_17", "bday_17",
            "att_1", "shayari_1", "love_1", "mot_1"
        )
        val featured = curatedStandoutIds.mapNotNull { id -> allTemplates.find { it.id == id } }

        assertEquals(20, featured.size)
        // Verify 20 strictly unique IDs
        assertEquals(20, featured.map { it.id }.toSet().size)
    }

    @Test
    fun testThumbnailCacheBasicOperations() {
        ThumbnailCache.clear()
        assertEquals(0, ThumbnailCache.size())
        assertNull(ThumbnailCache.get("nonexistent_key"))
    }
}
