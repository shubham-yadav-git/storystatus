package com.sky.storystatus.ui.editor

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sky.storystatus.data.TemplateRepository
import com.sky.storystatus.export.DefaultStatusImageRenderer
import com.sky.storystatus.export.ShareManager
import com.sky.storystatus.model.EditorState
import com.sky.storystatus.model.EditorTool
import com.sky.storystatus.model.StatusTemplate
import com.sky.storystatus.model.TemplateBackground
import com.sky.storystatus.model.TemplateCustomization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel managing editing operations, undo/redo history stacks, and image export.
 * Note: Never holds Android Context, View, Activity, or Bitmap instances in EditorState.
 */
class EditorViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = TemplateRepository(application)
    private val renderer = DefaultStatusImageRenderer(application)
    private val shareManager = ShareManager(application)

    private val _editorState = MutableStateFlow(EditorState(templateId = ""))
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    private val _template = MutableStateFlow<StatusTemplate?>(null)
    val template: StateFlow<StatusTemplate?> = _template.asStateFlow()

    // Bounded undo and redo stacks (max 30 snapshots)
    private val undoStack = mutableListOf<TemplateCustomization>()
    private val redoStack = mutableListOf<TemplateCustomization>()
    private val maxHistorySize = 30

    // Debounce job for text commits to prevent per-character undo entries
    private var textCommitJob: Job? = null
    private var preTextCustomization: TemplateCustomization? = null

    fun initTemplate(templateId: String) {
        if (_editorState.value.templateId == templateId && _template.value != null) {
            return
        }
        viewModelScope.launch {
            val templates = repository.loadTemplates()
            val found = templates.find { it.id == templateId }
            _template.value = found
            val initialLayerId = found?.textLayers?.firstOrNull()?.id
            undoStack.clear()
            redoStack.clear()
            preTextCustomization = null
            _editorState.value = EditorState(
                templateId = templateId,
                customization = TemplateCustomization(),
                selectedLayerId = initialLayerId,
                activeTool = EditorTool.TEXT,
                canUndo = false,
                canRedo = false,
                isExporting = false,
                hasUnsavedChanges = false
            )
        }
    }

    private fun applyCustomizationChange(newCustomization: TemplateCustomization) {
        val current = _editorState.value.customization
        if (newCustomization == current) return

        // Push current state to undo stack
        undoStack.add(current)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
        redoStack.clear()

        _editorState.update { state ->
            state.copy(
                customization = newCustomization,
                canUndo = undoStack.isNotEmpty(),
                canRedo = false,
                hasUnsavedChanges = newCustomization != TemplateCustomization()
            )
        }
    }

    /**
     * Updates text with debounced history commit so typing does not flood the undo stack.
     */
    fun updateText(layerId: String, newText: String) {
        val current = _editorState.value.customization
        val currentLayerText = current.textModifications[layerId]
            ?: _template.value?.textLayers?.find { it.id == layerId }?.text
            ?: ""
        if (newText == currentLayerText) return

        if (preTextCustomization == null) {
            preTextCustomization = current
        }

        val updatedMap = current.textModifications.toMutableMap()
        updatedMap[layerId] = newText
        val newCustomization = current.copy(textModifications = updatedMap)

        // Live update in state for instant UI preview response
        _editorState.update { state ->
            state.copy(
                customization = newCustomization,
                hasUnsavedChanges = true
            )
        }

        textCommitJob?.cancel()
        textCommitJob = viewModelScope.launch {
            delay(400)
            commitTextHistory()
        }
    }

    private fun commitTextHistory() {
        val baseline = preTextCustomization ?: return
        val current = _editorState.value.customization
        if (current != baseline) {
            undoStack.add(baseline)
            if (undoStack.size > maxHistorySize) {
                undoStack.removeAt(0)
            }
            redoStack.clear()
            _editorState.update { state ->
                state.copy(
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = false,
                    hasUnsavedChanges = current != TemplateCustomization()
                )
            }
        }
        preTextCustomization = null
    }

    fun updateFont(layerId: String, fontFamily: String) {
        commitTextHistory()
        val current = _editorState.value.customization
        val updatedMap = current.fontModifications.toMutableMap()
        updatedMap[layerId] = fontFamily
        applyCustomizationChange(current.copy(fontModifications = updatedMap))
    }

    fun updateFontSize(layerId: String, fontSize: Float) {
        commitTextHistory()
        val current = _editorState.value.customization
        val updatedMap = current.fontSizeModifications.toMutableMap()
        updatedMap[layerId] = fontSize
        applyCustomizationChange(current.copy(fontSizeModifications = updatedMap))
    }

    fun updateColor(layerId: String, color: Long) {
        commitTextHistory()
        val current = _editorState.value.customization
        val updatedMap = current.colorModifications.toMutableMap()
        updatedMap[layerId] = color
        applyCustomizationChange(current.copy(colorModifications = updatedMap))
    }

    fun updateAlignment(layerId: String, alignment: String) {
        commitTextHistory()
        val current = _editorState.value.customization
        val updatedMap = current.alignmentModifications.toMutableMap()
        updatedMap[layerId] = alignment
        applyCustomizationChange(current.copy(alignmentModifications = updatedMap))
    }

    fun updatePosition(layerId: String, y: Float) {
        commitTextHistory()
        val current = _editorState.value.customization
        val updatedMap = current.positionModifications.toMutableMap()
        updatedMap[layerId] = y
        applyCustomizationChange(current.copy(positionModifications = updatedMap))
    }

    fun updateBackground(background: TemplateBackground) {
        commitTextHistory()
        val current = _editorState.value.customization
        applyCustomizationChange(current.copy(backgroundModification = background))
    }

    fun selectLayer(layerId: String) {
        commitTextHistory()
        _editorState.update { it.copy(selectedLayerId = layerId) }
    }

    fun selectTool(tool: EditorTool) {
        commitTextHistory()
        _editorState.update { it.copy(activeTool = tool) }
    }

    fun undo() {
        commitTextHistory()
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeAt(undoStack.lastIndex)
        val current = _editorState.value.customization
        redoStack.add(current)
        _editorState.update { state ->
            state.copy(
                customization = previous,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true,
                hasUnsavedChanges = previous != TemplateCustomization()
            )
        }
    }

    fun redo() {
        commitTextHistory()
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        val current = _editorState.value.customization
        undoStack.add(current)
        _editorState.update { state ->
            state.copy(
                customization = next,
                canUndo = true,
                canRedo = redoStack.isNotEmpty(),
                hasUnsavedChanges = next != TemplateCustomization()
            )
        }
    }

    fun reset() {
        commitTextHistory()
        val current = _editorState.value.customization
        if (current == TemplateCustomization()) return
        undoStack.add(current)
        redoStack.clear()
        _editorState.update { state ->
            state.copy(
                customization = TemplateCustomization(),
                canUndo = true,
                canRedo = false,
                hasUnsavedChanges = false
            )
        }
    }

    suspend fun renderPreviewBitmap(width: Int = 1080, height: Int = 1920): Bitmap? = withContext(Dispatchers.Default) {
        val tmpl = _template.value ?: return@withContext null
        renderer.render(tmpl, _editorState.value.customization, width, height)
    }

    suspend fun saveToGallery(): Result<Uri> = withContext(Dispatchers.Default) {
        val tmpl = _template.value ?: return@withContext Result.failure(IllegalStateException("Template not loaded"))
        _editorState.update { it.copy(isExporting = true) }
        try {
            val bitmap = renderer.render(tmpl, _editorState.value.customization, 1080, 1920)
            shareManager.saveToMediaStore(bitmap, tmpl.title)
        } finally {
            _editorState.update { it.copy(isExporting = false) }
        }
    }

    suspend fun shareStatus(): Result<Unit> = withContext(Dispatchers.Default) {
        val tmpl = _template.value ?: return@withContext Result.failure(IllegalStateException("Template not loaded"))
        _editorState.update { it.copy(isExporting = true) }
        try {
            val bitmap = renderer.render(tmpl, _editorState.value.customization, 1080, 1920)
            shareManager.shareImage(bitmap)
        } finally {
            _editorState.update { it.copy(isExporting = false) }
        }
    }
}
