package com.sky.storystatus

import com.sky.storystatus.data.CatalogParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CatalogParserTest {

    @Test
    fun testParseCatalog() {
        val json = """
            [
              {
                "id": "test_1",
                "title": "A Test Template",
                "category": "Motivation",
                "language": "English",
                "background": {
                  "colorValue": 4294967295
                },
                "textLayers": [
                  {
                    "id": "l1",
                    "text": "Hello World",
                    "fontFamily": "sans-serif",
                    "fontSize": 24.0,
                    "color": 4278190080,
                    "alignment": "center",
                    "x": 540.0,
                    "y": 960.0,
                    "maxWidth": 900.0
                  }
                ],
                "version": 1
              }
            ]
        """.trimIndent()

        val parser = CatalogParser()
        val templates = parser.parseCatalog(json)

        assertNotNull(templates)
        assertEquals(1, templates.size)
        assertEquals("test_1", templates[0].id)
        assertEquals("A Test Template", templates[0].title)
        assertEquals(1, templates[0].textLayers.size)
        assertEquals("Hello World", templates[0].textLayers[0].text)
    }

    @Test
    fun testBundledCatalogAsset() {
        val file = java.io.File("src/main/assets/templates.json")
        org.junit.Assert.assertTrue("Asset file should exist", file.exists())
        val json = file.readText()
        val parser = CatalogParser()
        val templates = parser.parseCatalog(json)

        assertEquals(304, templates.size)
        val categories = templates.map { it.category }.toSet()
        val expectedCategories = setOf(
            "Attitude", "Hindi Shayari", "Love", "Motivation",
            "Friendship", "Sad", "Birthday", "Festivals"
        )
        assertEquals(expectedCategories, categories)

        // Validate strictly unique template IDs
        val ids = templates.map { it.id }
        assertEquals("All template IDs must be unique", ids.toSet().size, ids.size)

        // Validate each template has required properties
        templates.forEach { template ->
            org.junit.Assert.assertTrue(template.id.isNotBlank())
            org.junit.Assert.assertTrue(template.title.isNotBlank())
            org.junit.Assert.assertTrue(template.category.isNotBlank())
            org.junit.Assert.assertTrue(template.language.isNotBlank())
            org.junit.Assert.assertTrue(template.textLayers.isNotEmpty())
            template.textLayers.forEach { layer ->
                org.junit.Assert.assertTrue(layer.text.isNotBlank())
            }
        }

        // Validate that templates feature tags and decorations
        val decoratedTemplates = templates.filter { it.decorations.isNotEmpty() }
        org.junit.Assert.assertTrue("Should contain templates with rich vector decorations", decoratedTemplates.size >= 180)

        val taggedTemplates = templates.filter { it.tags.isNotEmpty() }
        org.junit.Assert.assertTrue("Should contain templates with discovery tags", taggedTemplates.size >= 200)
    }

    @Test
    fun testMalformedJsonHandling() {
        val parser = CatalogParser()
        val emptyResult = parser.parseCatalog("INVALID JSON {{{")
        assertEquals(0, emptyResult.size)

        val nullResult = parser.parseCatalog("")
        assertEquals(0, nullResult.size)
    }
}
