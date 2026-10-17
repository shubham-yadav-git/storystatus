package com.sky.storystatus.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sky.storystatus.export.DefaultStatusImageRenderer
import com.sky.storystatus.export.ThumbnailCache
import com.sky.storystatus.model.StatusTemplate
import com.sky.storystatus.model.TemplateCustomization
import com.sky.storystatus.ui.theme.Dimens
import com.sky.storystatus.ui.theme.categoryColor
import com.sky.storystatus.ui.theme.categoryEmoji
import com.sky.storystatus.ui.theme.categoryGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Premium template card with rendered preview, category badge,
 * title overlay, and press animation.
 */
@Composable
fun TemplateCard(
    template: StatusTemplate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: ((String) -> Unit)? = null
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(Dimens.RadiusLg)
            )
            .clip(RoundedCornerShape(Dimens.RadiusLg))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClickLabel = "Open ${template.title}"
            ) { onClick() }
    ) {
        // Rendered template thumbnail
        TemplateThumbnail(
            template = template,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom gradient overlay for title readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                    )
                )
        )

        // Category badge
        Box(
            modifier = Modifier
                .padding(Dimens.SpaceSm)
                .align(Alignment.TopStart)
                .background(
                    brush = Brush.horizontalGradient(categoryGradient(template.category)),
                    shape = RoundedCornerShape(Dimens.RadiusFull)
                )
                .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceXs)
        ) {
            Text(
                text = "${categoryEmoji(template.category)} ${template.category}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Favorite button
        if (onToggleFavorite != null) {
            IconButton(
                onClick = { onToggleFavorite(template.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens.SpaceXs)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "${template.title} is in favorites, click to remove" else "Add ${template.title} to favorites",
                    tint = if (isFavorite) Color(0xFFFF4081) else Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Title & language label at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(Dimens.SpaceMd)
        ) {
            Text(
                text = template.title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = template.language,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Renders a template at thumbnail resolution with high-speed memory-aware LRU caching.
 * Performs background rendering on Dispatchers.IO and avoids heavy spinner animations
 * during active grid scrolling.
 */
@Composable
fun TemplateThumbnail(template: StatusTemplate, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val cacheKey = "${template.id}_v${template.version}"
    var bitmap by remember(cacheKey) { mutableStateOf(ThumbnailCache.get(cacheKey)) }

    LaunchedEffect(cacheKey) {
        if (bitmap == null) {
            val cached = ThumbnailCache.get(cacheKey)
            if (cached != null) {
                bitmap = cached
            } else {
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val renderer = DefaultStatusImageRenderer(context)
                        // Render lightweight 270x480 thumbnail off the UI thread
                        val rendered = renderer.render(template, TemplateCustomization(), 270, 480)
                        ThumbnailCache.put(cacheKey, rendered)
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            bitmap = rendered
                        }
                    } catch (e: Exception) {
                        // ignore and keep fallback background
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(categoryGradient(template.category))
        ),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * Category chip with gradient background when selected.
 */
@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) categoryColor(label) else Color.Transparent,
        label = "chipColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chipTextColor"
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (label != "All") {
                    Text(
                        text = categoryEmoji(label),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = label,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        modifier = modifier.height(Dimens.ChipHeight),
        shape = RoundedCornerShape(Dimens.RadiusFull),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            selectedContainerColor = containerColor,
            selectedLabelColor = contentColor,
            labelColor = contentColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderColor = Color.Transparent,
            enabled = true,
            selected = selected
        )
    )
}

/**
 * Section header with title and optional action.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onAction() }
            )
        } else if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Empty state with icon, title, and subtitle.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimens.IconSizeXxl),
            tint = iconTint
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceLg))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceSm))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Color swatch circle for color picker.
 */
@Composable
fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val isWhite = color == Color.White || color.alpha > 0.98f && color.red > 0.98f && color.green > 0.98f && color.blue > 0.98f

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isWhite || selected) {
                    Modifier.border(
                        width = if (selected) 3.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
                        shape = CircleShape
                    )
                } else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (color.luminance() > 0.5f) Color.Black else Color.White
                    )
            )
        }
    }
}

/** Approximate luminance check */
private fun Color.luminance(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue
