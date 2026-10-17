package com.sky.storystatus.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sky.storystatus.model.StatusTemplate
import com.sky.storystatus.ui.components.CategoryChip
import com.sky.storystatus.ui.components.EmptyState
import com.sky.storystatus.ui.components.SectionHeader
import com.sky.storystatus.ui.components.TemplateCard
import com.sky.storystatus.ui.components.TemplateThumbnail
import com.sky.storystatus.ui.theme.Dimens
import com.sky.storystatus.ui.theme.StatusWalaColors
import com.sky.storystatus.ui.theme.categoryColor
import com.sky.storystatus.ui.theme.categoryGradient
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    templates: List<StatusTemplate>,
    featuredTemplates: List<StatusTemplate> = emptyList(),
    categories: List<String>,
    favorites: Set<String>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onTemplateSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onRetry: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val allCategories = remember(categories) { listOf("All") + categories }

    // Deterministic featured selection: use provided featuredTemplates or fallback to first items
    val effectiveFeatured = remember(featuredTemplates, templates) {
        if (featuredTemplates.isNotEmpty()) featuredTemplates else templates.take(4)
    }

    // Grid templates filtered by selected category
    val displayTemplates = remember(selectedCategory, templates) {
        if (selectedCategory == "All") templates else templates.filter { it.category == selectedCategory }
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); isVisible = true }

    if (isLoading && templates.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    if (errorMessage != null && templates.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.SpaceXl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmptyState(
                icon = Icons.Rounded.SentimentDissatisfied,
                title = "Unable to load templates",
                subtitle = errorMessage
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceLg))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                Text("Retry")
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(Dimens.GridColumns),
        horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
        contentPadding = PaddingValues(bottom = Dimens.SpaceXxl),
        modifier = Modifier.fillMaxSize()
    ) {
        // ── Compact Brand Header ──
        item(span = { GridItemSpan(maxLineSpan) }) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { -it / 2 }
            ) {
                CompactHeroHeader(
                    favoritesCount = favorites.size,
                    onSearchClick = onSearchClick,
                    onFavoritesClick = onFavoritesClick
                )
            }
        }

        // ── Featured Showcase (Curated, Deterministic) ──
        if (effectiveFeatured.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically { it / 3 }
                ) {
                    Column(modifier = Modifier.padding(top = Dimens.SpaceLg)) {
                        SectionHeader(
                            title = "Featured",
                            modifier = Modifier.padding(horizontal = Dimens.SpaceLg),
                            actionLabel = "See All",
                            onAction = onSearchClick
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                            contentPadding = PaddingValues(horizontal = Dimens.SpaceLg)
                        ) {
                            items(effectiveFeatured, key = { "featured_${it.id}" }) { featured ->
                                FeaturedItemCard(
                                    template = featured,
                                    isFavorite = favorites.contains(featured.id),
                                    onToggleFavorite = { onToggleFavorite(featured.id) },
                                    onClick = { onTemplateSelected(featured.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Category Filter Section ──
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.padding(top = Dimens.SpaceLg)) {
                SectionHeader(
                    title = "Explore Categories",
                    modifier = Modifier.padding(horizontal = Dimens.SpaceLg)
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceLg)
                ) {
                    items(allCategories) { category ->
                        CategoryChip(
                            label = category,
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }
        }

        // ── Trending / Popular Section Header ──
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(
                title = if (selectedCategory == "All") "Popular" else selectedCategory,
                modifier = Modifier.padding(
                    start = Dimens.SpaceLg,
                    end = Dimens.SpaceLg,
                    top = Dimens.SpaceLg
                ),
                actionLabel = if (displayTemplates.size > 0) "${displayTemplates.size} designs" else null
            )
        }

        // ── Empty State inside grid if no templates for selected category ──
        if (displayTemplates.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    icon = Icons.Rounded.SentimentDissatisfied,
                    title = "No templates in $selectedCategory",
                    subtitle = "Select another category to view designs.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }
        } else {
            // ── 2-Column Responsive Grid ──
            itemsIndexed(
                items = displayTemplates,
                key = { _, template -> template.id }
            ) { index, template ->
                TemplateCard(
                    template = template,
                    onClick = { onTemplateSelected(template.id) },
                    modifier = Modifier
                        .padding(
                            start = if (index % 2 == 0) Dimens.SpaceLg else 0.dp,
                            end = if (index % 2 == 1) Dimens.SpaceLg else 0.dp
                        )
                        .aspectRatio(Dimens.TemplateCardAspectRatio),
                    isFavorite = favorites.contains(template.id),
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}

@Composable
private fun CompactHeroHeader(
    favoritesCount: Int,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        // Thin primary accent stripe — brand anchor, not a decoration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(MaterialTheme.colorScheme.primary)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceLg)
                .padding(top = Dimens.SpaceMd, bottom = Dimens.SpaceMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "StoryStatus",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.RadiusSm))
                        .background(
                            if (favoritesCount > 0)
                                Color(0xFFFF4081).copy(alpha = 0.10f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable(onClickLabel = "View saved favorites", onClick = onFavoritesClick)
                        .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorites",
                            tint = if (favoritesCount > 0) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        if (favoritesCount > 0) {
                            Text(
                                text = "$favoritesCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF4081)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceMd))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.RadiusMd))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .clickable(onClickLabel = "Open search and explore", onClick = onSearchClick)
                    .padding(horizontal = Dimens.SpaceMd, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.IconSizeMd)
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                Text(
                    text = "Search quotes, shayari, attitude...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Curated Featured Card showing a 9:16 preview thumbnail, category tag,
 * title, quote snippet, favorite toggle, and a clear Customize action.
 */
@Composable
private fun FeaturedItemCard(
    template: StatusTemplate,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(290.dp)
            .clip(RoundedCornerShape(Dimens.RadiusXl))
            .clickable(onClickLabel = "Open featured template ${template.title}") { onClick() },
        shape = RoundedCornerShape(Dimens.RadiusXl),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 9:16 Portrait thumbnail
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .aspectRatio(Dimens.TemplateCardAspectRatio)
                    .clip(RoundedCornerShape(Dimens.RadiusMd))
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(Dimens.RadiusMd))
            ) {
                TemplateThumbnail(template = template, modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.width(Dimens.SpaceMd))

            // Details and actions
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(categoryGradient(template.category)),
                                shape = RoundedCornerShape(Dimens.RadiusFull)
                            )
                            .padding(horizontal = Dimens.SpaceSm, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FEATURED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.Favorite,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = template.textLayers.firstOrNull()?.text ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onClick,
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceMd, vertical = 0.dp),
                    shape = RoundedCornerShape(Dimens.RadiusFull),
                    colors = ButtonDefaults.buttonColors(containerColor = categoryColor(template.category))
                ) {
                    Text(
                        text = "Customize",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
