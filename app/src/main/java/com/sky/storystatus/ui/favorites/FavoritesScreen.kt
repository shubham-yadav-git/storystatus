package com.sky.storystatus.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sky.storystatus.model.StatusTemplate
import com.sky.storystatus.ui.components.EmptyState
import com.sky.storystatus.ui.components.SectionHeader
import com.sky.storystatus.ui.components.TemplateCard
import com.sky.storystatus.ui.theme.Dimens

@Composable
fun FavoritesScreen(
    templates: List<StatusTemplate>,
    favoriteIds: Set<String>,
    onTemplateSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val favoriteTemplates = templates.filter { favoriteIds.contains(it.id) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(Dimens.GridColumns),
        horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
        contentPadding = PaddingValues(bottom = Dimens.SpaceXxl),
        modifier = Modifier.fillMaxSize()
    ) {
        // ── Header ──
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(
                modifier = Modifier
                    .statusBarsPadding()
                    .height(Dimens.SpaceXxl)
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Favorites ❤️",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = Dimens.SpaceLg)
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Your saved templates for quick access",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceXs)
            )
        }

        if (favoriteTemplates.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    icon = Icons.Filled.FavoriteBorder,
                    title = "No favorites yet",
                    subtitle = "Tap the ❤️ icon on any template to save it here.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            }
        } else {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    title = "Your Collection",
                    modifier = Modifier.padding(
                        start = Dimens.SpaceLg,
                        end = Dimens.SpaceLg,
                        top = Dimens.SpaceLg
                    ),
                    actionLabel = "${favoriteTemplates.size} saved"
                )
            }

            itemsIndexed(
                items = favoriteTemplates,
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
                    isFavorite = true,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}
