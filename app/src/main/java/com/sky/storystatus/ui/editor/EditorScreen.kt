package com.sky.storystatus.ui.editor

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sky.storystatus.model.EditorState
import com.sky.storystatus.model.EditorTool
import com.sky.storystatus.model.StatusTemplate
import com.sky.storystatus.model.TemplateBackground
import com.sky.storystatus.model.TemplateTextLayer
import com.sky.storystatus.ui.theme.Dimens
import com.sky.storystatus.ui.theme.categoryColor
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    templateId: String,
    viewModel: EditorViewModel = viewModel(),
    onBack: () -> Unit
) {
    val editorState by viewModel.editorState.collectAsState()
    val template by viewModel.template.collectAsState()

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRendering by remember { mutableStateOf(true) }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Initialize template
    LaunchedEffect(templateId) {
        viewModel.initTemplate(templateId)
    }

    // Re-render preview bitmap whenever customization or template changes
    LaunchedEffect(template, editorState.customization) {
        if (template != null) {
            isRendering = true
            previewBitmap = viewModel.renderPreviewBitmap(1080, 1920)
            isRendering = false
        }
    }

    // Intercept system back press when there are unsaved changes
    BackHandler(enabled = editorState.hasUnsavedChanges) {
        showDiscardDialog = true
    }

    // Discard Confirmation Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?", fontWeight = FontWeight.Bold) },
            text = { Text("You have unsaved customizations. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onBack()
                }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to original?", fontWeight = FontWeight.Bold) },
            text = { Text("This will revert all text, fonts, colors, and background back to the template's original state.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    viewModel.reset()
                }) {
                    Text("Reset", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Top Toolbar ──
            EditorTopBar(
                canUndo = editorState.canUndo,
                canRedo = editorState.canRedo,
                canReset = editorState.hasUnsavedChanges,
                isExporting = editorState.isExporting,
                onBack = {
                    if (editorState.hasUnsavedChanges) {
                        showDiscardDialog = true
                    } else {
                        onBack()
                    }
                },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onReset = { showResetDialog = true },
                onShare = {
                    coroutineScope.launch {
                        val result = viewModel.shareStatus()
                        if (result.isFailure) {
                            snackbarHostState.showSnackbar("❌ Failed to share image.")
                        }
                    }
                },
                onSave = {
                    coroutineScope.launch {
                        val result = viewModel.saveToGallery()
                        snackbarHostState.showSnackbar(
                            if (result.isSuccess) "✅ Saved to gallery!" else "❌ Failed to save."
                        )
                    }
                }
            )

            // ── Active Layer Selector (if multiple layers) ──
            template?.let { tmpl ->
                if (tmpl.textLayers.size > 1) {
                    LayerSelectorRow(
                        layers = tmpl.textLayers,
                        selectedLayerId = editorState.selectedLayerId,
                        onSelectLayer = { viewModel.selectLayer(it) }
                    )
                }
            }

            // ── Canvas Preview Area (9:16 portrait) ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceXs),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(9f / 16f)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(Dimens.RadiusLg),
                            ambientColor = categoryColor(template?.category ?: "").copy(alpha = 0.35f),
                            spotColor = categoryColor(template?.category ?: "").copy(alpha = 0.25f)
                        )
                        .clip(RoundedCornerShape(Dimens.RadiusLg))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    previewBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Status Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    if (isRendering) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // ── Bottom Editing Controls Panel ──
            EditorControlsPanel(
                template = template,
                editorState = editorState,
                viewModel = viewModel
            )
        }
    }
}

/**
 * Top Toolbar with Back, Undo, Redo, Reset, Share, and Save buttons.
 */
@Composable
private fun EditorTopBar(
    canUndo: Boolean,
    canRedo: Boolean,
    canReset: Boolean,
    isExporting: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onReset: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(onClick = onUndo, enabled = canUndo) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }

        IconButton(onClick = onRedo, enabled = canRedo) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }

        IconButton(onClick = onReset, enabled = canReset) {
            Icon(
                imageVector = Icons.Filled.RestartAlt,
                contentDescription = "Reset",
                tint = if (canReset) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onShare, enabled = !isExporting) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Button(
            onClick = onSave,
            enabled = !isExporting,
            shape = RoundedCornerShape(Dimens.RadiusFull),
            contentPadding = PaddingValues(horizontal = Dimens.SpaceMd, vertical = 0.dp),
            modifier = Modifier.height(36.dp)
        ) {
            if (isExporting) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Layer selector chip row for templates with multiple text layers.
 */
@Composable
private fun LayerSelectorRow(
    layers: List<TemplateTextLayer>,
    selectedLayerId: String?,
    onSelectLayer: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Dimens.SpaceLg, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm, Alignment.CenterHorizontally)
    ) {
        layers.forEachIndexed { index, layer ->
            val isSelected = layer.id == selectedLayerId
            val label = when {
                index == 0 -> "❝ Quote"
                layer.text.startsWith("★") || layer.text.startsWith("🔥") || layer.text.startsWith("👑") || layer.text.startsWith("✨") || layer.text.startsWith("📜") || layer.text.startsWith("🔱") -> "★ Tag"
                layer.text.startsWith("~") -> "~ Author"
                else -> "Layer ${index + 1}"
            }
            FilterChip(
                selected = isSelected,
                onClick = { onSelectLayer(layer.id) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(Dimens.RadiusFull),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

/**
 * Bottom Controls Container with active contextual tool area and tool navigation tabs.
 */
@Composable
private fun EditorControlsPanel(
    template: StatusTemplate?,
    editorState: EditorState,
    viewModel: EditorViewModel
) {
    val activeLayer = template?.textLayers?.find { it.id == editorState.selectedLayerId }
        ?: template?.textLayers?.firstOrNull()

    val currentText = activeLayer?.let { layer ->
        editorState.customization.textModifications[layer.id] ?: layer.text
    } ?: ""

    val currentFont = activeLayer?.let { layer ->
        editorState.customization.fontModifications[layer.id] ?: layer.fontFamily
    } ?: "sans-serif"

    val currentFontSize = activeLayer?.let { layer ->
        editorState.customization.fontSizeModifications[layer.id] ?: layer.fontSize
    } ?: 64.0f

    val currentColor = activeLayer?.let { layer ->
        editorState.customization.colorModifications[layer.id] ?: layer.color
    } ?: 0xFFFFFFFF

    val currentAlignment = activeLayer?.let { layer ->
        editorState.customization.alignmentModifications[layer.id] ?: layer.alignment
    } ?: "center"

    val currentY = activeLayer?.let { layer ->
        editorState.customization.positionModifications[layer.id] ?: layer.y
    } ?: 960.0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = Dimens.RadiusXxl, topEnd = Dimens.RadiusXxl))
            .background(MaterialTheme.colorScheme.surface)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = Dimens.RadiusXxl, topEnd = Dimens.RadiusXxl)
            )
    ) {
        // Contextual Tool Control Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceSm),
            contentAlignment = Alignment.Center
        ) {
            when (editorState.activeTool) {
                EditorTool.TEXT -> TextTool(
                    text = currentText,
                    onTextChange = { newText ->
                        activeLayer?.let { viewModel.updateText(it.id, newText) }
                    }
                )

                EditorTool.FONT -> FontTool(
                    selectedFont = currentFont,
                    onFontSelected = { font ->
                        activeLayer?.let { viewModel.updateFont(it.id, font) }
                    }
                )

                EditorTool.SIZE -> SizeTool(
                    fontSize = currentFontSize,
                    onSizeChange = { newSize ->
                        activeLayer?.let { viewModel.updateFontSize(it.id, newSize) }
                    }
                )

                EditorTool.COLOR -> ColorTool(
                    selectedColor = currentColor,
                    onColorSelected = { colorVal ->
                        activeLayer?.let { viewModel.updateColor(it.id, colorVal) }
                    }
                )

                EditorTool.BACKGROUND -> BackgroundTool(
                    currentBackground = editorState.customization.backgroundModification ?: template?.background,
                    onBackgroundSelected = { bg ->
                        viewModel.updateBackground(bg)
                    }
                )

                EditorTool.ALIGNMENT -> AlignmentTool(
                    alignment = currentAlignment,
                    onAlignmentChange = { align ->
                        activeLayer?.let { viewModel.updateAlignment(it.id, align) }
                    }
                )

                EditorTool.POSITION -> PositionTool(
                    yPosition = currentY,
                    onPositionChange = { newY ->
                        activeLayer?.let { viewModel.updatePosition(it.id, newY) }
                    }
                )
            }
        }

        // Bottom Navigation Bar for Tools
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceXs),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)
        ) {
            EditorTool.entries.forEach { tool ->
                val isSelected = editorState.activeTool == tool
                val icon = when (tool) {
                    EditorTool.TEXT -> Icons.Filled.TextFields
                    EditorTool.FONT -> Icons.Filled.FontDownload
                    EditorTool.SIZE -> Icons.Filled.FormatSize
                    EditorTool.COLOR -> Icons.Filled.Palette
                    EditorTool.BACKGROUND -> Icons.Filled.Wallpaper
                    EditorTool.ALIGNMENT -> Icons.Filled.FormatAlignCenter
                    EditorTool.POSITION -> Icons.Filled.SwapVert
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.RadiusMd))
                        .clickable { viewModel.selectTool(tool) }
                        .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceXs)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tool.label,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tool.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOOL SUB-COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TextTool(
    text: String,
    onTextChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit Text",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (text.isNotEmpty()) {
                TextButton(
                    onClick = { onTextChange("") },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your status quote...") },
            shape = RoundedCornerShape(Dimens.RadiusMd),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}

private data class FontOption(
    val name: String,
    val familyId: String,
    val fontFamily: FontFamily,
    val fontWeight: FontWeight = FontWeight.Normal
)

@Composable
private fun FontTool(
    selectedFont: String,
    onFontSelected: (String) -> Unit
) {
    val fonts = listOf(
        FontOption("Classic", "sans-serif", FontFamily.Default, FontWeight.Normal),
        FontOption("Modern", "sans-serif-medium", FontFamily.SansSerif, FontWeight.Medium),
        FontOption("Impact", "sans-serif-bold", FontFamily.SansSerif, FontWeight.Bold),
        FontOption("Heavy", "sans-serif-black", FontFamily.SansSerif, FontWeight.Black),
        FontOption("Editorial", "serif", FontFamily.Serif, FontWeight.Normal),
        FontOption("Minimal", "sans-serif-light", FontFamily.SansSerif, FontWeight.Light)
    )

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(
            text = "Typography Style",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Dimens.SpaceSm)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
        ) {
            fonts.forEach { fontOption ->
                val isSelected = fontOption.familyId == selectedFont
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onFontSelected(fontOption.familyId) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 44.dp)
                            .clip(RoundedCornerShape(Dimens.RadiusSm))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(Dimens.RadiusSm)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aa",
                            fontFamily = fontOption.fontFamily,
                            fontWeight = fontOption.fontWeight,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = fontOption.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SizeTool(
    fontSize: Float,
    onSizeChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Font Size",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${fontSize.toInt()} sp",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onSizeChange((fontSize - 4f).coerceAtLeast(24f)) },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Slider(
                value = fontSize,
                onValueChange = { onSizeChange(it) },
                valueRange = 24f..96f,
                steps = 18,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.SpaceMd),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedButton(
                onClick = { onSizeChange((fontSize + 4f).coerceAtMost(96f)) },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun ColorTool(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit
) {
    val presetColors = listOf(
        0xFFFFFFFF to "White",
        0xFFFFD700 to "Gold",
        0xFFFFC107 to "Amber",
        0xFFFF5252 to "Red",
        0xFFFF4081 to "Pink",
        0xFF00E5FF to "Cyan",
        0xFF00E676 to "Emerald",
        0xFFB388FF to "Violet",
        0xFFFFAB91 to "Peach",
        0xFF80D8FF to "Sky",
        0xFF1A1A1A to "Charcoal"
    )

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(
            text = "Text Color",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Dimens.SpaceSm)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
        ) {
            presetColors.forEach { (colorVal, name) ->
                val isSelected = colorVal == selectedColor
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .then(
                                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else if (colorVal == 0xFFFFFFFF) Modifier.border(1.dp, Color.LightGray, CircleShape)
                                else Modifier
                            )
                            .clickable { onColorSelected(colorVal) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BackgroundTool(
    currentBackground: TemplateBackground?,
    onBackgroundSelected: (TemplateBackground) -> Unit
) {
    val presets = listOf(
        TemplateBackground.Gradient(listOf(0xFF0D0D11, 0xFF1A1A2E, 0xFF16213E), "diagonal") to "Obsidian",
        TemplateBackground.Gradient(listOf(0xFF1B0000, 0xFF4A0000, 0xFF8B0000), "diagonal") to "Crimson",
        TemplateBackground.Gradient(listOf(0xFF001F1D, 0xFF004D40, 0xFF00796B), "diagonal") to "Teal",
        TemplateBackground.Gradient(listOf(0xFF120024, 0xFF2A0845, 0xFF6441A5), "diagonal") to "Violet",
        TemplateBackground.Gradient(listOf(0xFF2B0938, 0xFF5F134B, 0xFF9E1B32), "linear") to "Blaze",
        TemplateBackground.Gradient(listOf(0xFF0F0C08, 0xFF241E15, 0xFF3D3222), "diagonal") to "Vintage",
        TemplateBackground.Gradient(listOf(0xFF0A1917, 0xFF153B34, 0xFF26594E), "diagonal") to "Forest",
        TemplateBackground.Gradient(listOf(0xFF280518, 0xFF5C0E35, 0xFF9C1A5B), "diagonal") to "Ruby",
        TemplateBackground.Gradient(listOf(0xFF051026, 0xFF0D2554, 0xFF1B498C), "diagonal") to "Navy",
        TemplateBackground.Gradient(listOf(0xFF331400, 0xFF662900, 0xFFCC5200), "linear") to "Sunrise",
        TemplateBackground.Color(0xFF0A0A0A) to "Noir",
        TemplateBackground.Color(0xFFFCF8F4) to "Cream"
    )

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(
            text = "Background Style",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Dimens.SpaceSm)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
        ) {
            presets.forEach { (bg, name) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(Dimens.RadiusSm))
                            .then(
                                when (bg) {
                                    is TemplateBackground.Gradient -> Modifier.background(
                                        Brush.verticalGradient(bg.colors.map { Color(it) })
                                    )
                                    is TemplateBackground.Color -> Modifier.background(Color(bg.colorValue))
                                    else -> Modifier.background(Color.Gray)
                                }
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Dimens.RadiusSm))
                            .clickable { onBackgroundSelected(bg) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AlignmentTool(
    alignment: String,
    onAlignmentChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(
            text = "Text Alignment",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Dimens.SpaceSm)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd, Alignment.CenterHorizontally)
        ) {
            listOf(
                Triple("left", Icons.Filled.FormatAlignLeft, "Left"),
                Triple("center", Icons.Filled.FormatAlignCenter, "Center"),
                Triple("right", Icons.Filled.FormatAlignRight, "Right")
            ).forEach { (alignVal, icon, label) ->
                val isSelected = alignment.lowercase() == alignVal
                OutlinedButton(
                    onClick = { onAlignmentChange(alignVal) },
                    shape = RoundedCornerShape(Dimens.RadiusMd),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = if (isSelected) 2.dp else 1.dp
                    )
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun PositionTool(
    yPosition: Float,
    onPositionChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vertical Position",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = { onPositionChange(960.0f) },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Center Vertically", style = MaterialTheme.typography.labelSmall)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onPositionChange((yPosition - 30f).coerceAtLeast(400f)) },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Text("▲", fontSize = 12.sp)
            }

            Slider(
                value = yPosition,
                onValueChange = { onPositionChange(it) },
                valueRange = 400f..1500f,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.SpaceMd),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedButton(
                onClick = { onPositionChange((yPosition + 30f).coerceAtMost(1500f)) },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Text("▼", fontSize = 12.sp)
            }
        }
    }
}
