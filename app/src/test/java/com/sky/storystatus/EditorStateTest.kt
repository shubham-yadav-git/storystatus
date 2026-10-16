package com.sky.storystatus

import com.sky.storystatus.model.EditorState
import com.sky.storystatus.model.EditorTool
import com.sky.storystatus.model.TemplateBackground
import com.sky.storystatus.model.TemplateCustomization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorStateTest {

    @Test
    fun testTextModification() {
        var state = TemplateCustomization()
        val currentTextMap = state.textModifications.toMutableMap()
        currentTextMap["layer_1"] = "New Custom Text"
        state = state.copy(textModifications = currentTextMap)

        assertEquals("New Custom Text", state.textModifications["layer_1"])
    }

    @Test
    fun testColorModification() {
        var state = TemplateCustomization()
        val currentColorMap = state.colorModifications.toMutableMap()
        currentColorMap["layer_1"] = 0xFFFF0000 // Red
        state = state.copy(colorModifications = currentColorMap)

        assertEquals(0xFFFF0000, state.colorModifications["layer_1"])
    }

    @Test
    fun testBackgroundModification() {
        var state = TemplateCustomization()
        val newBg = TemplateBackground.Color(0xFF00FF00) // Green
        state = state.copy(backgroundModification = newBg)

        assertNotNull(state.backgroundModification)
        assertEquals(newBg, state.backgroundModification)
    }

    @Test
    fun testFontModification() {
        var state = TemplateCustomization()
        val currentFontMap = state.fontModifications.toMutableMap()
        currentFontMap["layer_1"] = "serif"
        state = state.copy(fontModifications = currentFontMap)

        assertEquals("serif", state.fontModifications["layer_1"])
    }

    @Test
    fun testFontSizeModification() {
        var state = TemplateCustomization()
        val sizeMap = state.fontSizeModifications.toMutableMap()
        sizeMap["l1"] = 72f
        state = state.copy(fontSizeModifications = sizeMap)

        assertEquals(72f, state.fontSizeModifications["l1"])
    }

    @Test
    fun testAlignmentModification() {
        var state = TemplateCustomization()
        val alignMap = state.alignmentModifications.toMutableMap()
        alignMap["l1"] = "right"
        state = state.copy(alignmentModifications = alignMap)

        assertEquals("right", state.alignmentModifications["l1"])
    }

    @Test
    fun testPositionModification() {
        var state = TemplateCustomization()
        val posMap = state.positionModifications.toMutableMap()
        posMap["l1"] = 850f
        state = state.copy(positionModifications = posMap)

        assertEquals(850f, state.positionModifications["l1"])
    }

    @Test
    fun testEditorStateDefaultValues() {
        val editorState = EditorState(templateId = "t1")

        assertEquals("t1", editorState.templateId)
        assertEquals(TemplateCustomization(), editorState.customization)
        assertEquals(EditorTool.TEXT, editorState.activeTool)
        assertFalse(editorState.canUndo)
        assertFalse(editorState.canRedo)
        assertFalse(editorState.isExporting)
        assertFalse(editorState.hasUnsavedChanges)
    }

    @Test
    fun testUndoRedoStackLogic() {
        val undoStack = mutableListOf<TemplateCustomization>()
        val redoStack = mutableListOf<TemplateCustomization>()
        var current = TemplateCustomization()

        // 1. First edit: text change
        undoStack.add(current)
        current = current.copy(textModifications = mapOf("l1" to "Hello"))
        redoStack.clear()

        assertTrue(undoStack.isNotEmpty())
        assertTrue(redoStack.isEmpty())
        assertEquals("Hello", current.textModifications["l1"])

        // 2. Second edit: color change
        undoStack.add(current)
        current = current.copy(colorModifications = mapOf("l1" to 0xFFFFD700))
        redoStack.clear()

        assertEquals(2, undoStack.size)

        // 3. Undo color change
        val prev1 = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(current)
        current = prev1

        assertEquals("Hello", current.textModifications["l1"])
        assertTrue(current.colorModifications.isEmpty())
        assertEquals(1, undoStack.size)
        assertEquals(1, redoStack.size)

        // 4. Undo text change
        val prev2 = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(current)
        current = prev2

        assertEquals(TemplateCustomization(), current)
        assertTrue(undoStack.isEmpty())
        assertEquals(2, redoStack.size)

        // 5. Redo text change
        val next1 = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(current)
        current = next1

        assertEquals("Hello", current.textModifications["l1"])
        assertEquals(1, undoStack.size)
        assertEquals(1, redoStack.size)

        // 6. New edit after undo/redo must clear redo stack
        undoStack.add(current)
        current = current.copy(fontModifications = mapOf("l1" to "serif"))
        redoStack.clear()

        assertTrue(redoStack.isEmpty())
        assertEquals(2, undoStack.size)
    }

    @Test
    fun testUndoStackBounding() {
        val undoStack = mutableListOf<TemplateCustomization>()
        val maxSize = 30

        for (i in 1..40) {
            undoStack.add(TemplateCustomization(textModifications = mapOf("l1" to "Version $i")))
            if (undoStack.size > maxSize) {
                undoStack.removeAt(0)
            }
        }

        assertEquals(30, undoStack.size)
        assertEquals("Version 11", undoStack.first().textModifications["l1"])
        assertEquals("Version 40", undoStack.last().textModifications["l1"])
    }

    @Test
    fun testResetClearsCustomization() {
        var customization = TemplateCustomization(
            textModifications = mapOf("l1" to "Custom"),
            colorModifications = mapOf("l1" to 0xFFFF0000),
            fontSizeModifications = mapOf("l1" to 80f),
            backgroundModification = TemplateBackground.Color(0xFF000000)
        )
        assertTrue(customization != TemplateCustomization())

        // Reset
        customization = TemplateCustomization()
        assertEquals(TemplateCustomization(), customization)
        assertTrue(customization.textModifications.isEmpty())
        assertTrue(customization.colorModifications.isEmpty())
        assertTrue(customization.fontSizeModifications.isEmpty())
        assertTrue(customization.backgroundModification == null)
    }
}
