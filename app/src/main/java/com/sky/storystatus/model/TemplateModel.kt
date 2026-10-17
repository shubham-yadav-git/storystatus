package com.sky.storystatus.model

data class StatusTemplate(
    val id: String,
    val title: String,
    val category: String,
    val language: String,
    val background: TemplateBackground,
    val textLayers: List<TemplateTextLayer>,
    val tags: List<String> = emptyList(),
    val decorations: List<TemplateDecoration> = emptyList(),
    val description: String? = null,
    val version: Int = 2
)

sealed class TemplateBackground {
    data class Color(val colorValue: Long) : TemplateBackground()
    data class Gradient(val colors: List<Long>, val type: String = "linear") : TemplateBackground()
    data class Pattern(
        val patternType: String, // "grid", "dots", "split", "stars", "paper", "newspaper", "film", "arch", "sunburst", "aura"
        val baseColor: Long = 0xFF121212,
        val accentColor: Long = 0xFFFFFFFF,
        val secondaryColor: Long = 0x00000000,
        val scale: Float = 1f
    ) : TemplateBackground()
    data class ImageResource(val resourceName: String) : TemplateBackground()
}

/**
 * Procedural vector decorations supported by the graphics renderer.
 * Enables luxury borders, corner brackets, frosted cards, accent dividers,
 * polaroids, badges, barcodes, tape, and typographic seals without runtime image downloads.
 */
data class TemplateDecoration(
    val type: String, // "border", "corner_brackets", "card_panel", "divider", "symbol", "polaroid", "badge", "barcode", "tape", "circle_rings"
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val x2: Float = 0f,
    val y2: Float = 0f,
    val color: Long = 0xFFFFFFFF,
    val strokeColor: Long? = null,
    val strokeWidth: Float = 2f,
    val cornerRadius: Float = 0f,
    val style: String = "solid", // "solid", "double", "dashed"
    val symbolType: String = "", // "quote", "sparkle", "star", "heart", "seal"
    val label: String = "" // Optional badge or stamp label e.g. "OFFICIAL", "EDITION 01"
)

data class TemplateTextLayer(
    val id: String,
    val text: String,
    val fontFamily: String,
    val fontSize: Float,
    val color: Long,
    val alignment: String,
    val x: Float,
    val y: Float,
    val maxWidth: Float,
    val letterSpacing: Float = 0f,
    val lineSpacingMultiplier: Float = 1.2f,
    val shadowColor: Long? = null,
    val backgroundColor: Long? = null
)

data class TemplateCustomization(
    val textModifications: Map<String, String> = emptyMap(),
    val colorModifications: Map<String, Long> = emptyMap(),
    val fontModifications: Map<String, String> = emptyMap(),
    val fontSizeModifications: Map<String, Float> = emptyMap(),
    val alignmentModifications: Map<String, String> = emptyMap(),
    val positionModifications: Map<String, Float> = emptyMap(),
    val backgroundModification: TemplateBackground? = null
)

enum class EditorTool(val label: String) {
    TEXT("Text"),
    FONT("Font"),
    SIZE("Size"),
    COLOR("Color"),
    BACKGROUND("Background"),
    ALIGNMENT("Alignment"),
    POSITION("Position")
}

data class EditorState(
    val templateId: String,
    val customization: TemplateCustomization = TemplateCustomization(),
    val selectedLayerId: String? = null,
    val activeTool: EditorTool = EditorTool.TEXT,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isExporting: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)
