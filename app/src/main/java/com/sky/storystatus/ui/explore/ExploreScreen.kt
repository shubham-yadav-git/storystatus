package com.sky.storystatus.ui.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sky.storystatus.model.StatusTemplate
import com.sky.storystatus.ui.components.CategoryChip
import com.sky.storystatus.ui.components.EmptyState
import com.sky.storystatus.ui.components.SectionHeader
import com.sky.storystatus.ui.components.TemplateCard
import com.sky.storystatus.ui.theme.Dimens

@Composable
fun ExploreScreen(
    templates: List<StatusTemplate>,
    categories: List<String>,
    languages: List<String> = listOf("All", "Hindi", "Hinglish", "English"),
    tags: List<String> = emptyList(),
    selectedTag: String = "All",
    onTagSelected: (String) -> Unit = {},
    favorites: Set<String>,
    onTemplateSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    initialSearchQuery: String = "",
    focusSearch: Boolean = false
) {
    var searchQuery by remember { mutableStateOf(initialSearchQuery) }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedLanguage by remember { mutableStateOf("All") }
    var currentTag by remember { mutableStateOf(selectedTag) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusSearch) {
        if (focusSearch) {
            focusRequester.requestFocus()
        }
    }

    val allCategories = remember(categories) { listOf("All") + categories }
    val allTags = remember(tags, templates) {
        if (tags.isNotEmpty()) {
            if (tags.firstOrNull() == "All") tags else listOf("All") + tags
        } else {
            listOf("All") + templates.flatMap { it.tags }.distinct().sorted()
        }
    }
    val isFilterActive = searchQuery.isNotBlank() || selectedCategory != "All" || selectedLanguage != "All" || currentTag != "All"

    // Search and filter templates locally and efficiently
    val filteredTemplates = remember(searchQuery, selectedCategory, selectedLanguage, currentTag, templates) {
        val cleanQuery = searchQuery.trim()
        templates.filter { template ->
            val matchesCategory = selectedCategory == "All" ||
                    template.category.equals(selectedCategory, ignoreCase = true)

            val matchesLanguage = selectedLanguage == "All" ||
                    template.language.equals(selectedLanguage, ignoreCase = true)

            val matchesTag = currentTag == "All" ||
                    template.tags.any { it.equals(currentTag, ignoreCase = true) }

            val matchesQuery = cleanQuery.isEmpty() ||
                    template.title.contains(cleanQuery, ignoreCase = true) ||
                    template.category.contains(cleanQuery, ignoreCase = true) ||
                    template.language.contains(cleanQuery, ignoreCase = true) ||
                    template.tags.any { it.contains(cleanQuery, ignoreCase = true) } ||
                    template.textLayers.any { it.text.contains(cleanQuery, ignoreCase = true) }

            matchesCategory && matchesLanguage && matchesTag && matchesQuery
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(Dimens.GridColumns),
        horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
        contentPadding = PaddingValues(bottom = Dimens.SpaceXxl),
        modifier = Modifier.fillMaxSize()
    ) {
        // ── Header & Search Controls ──
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Dimens.SpaceLg)
                    .padding(top = Dimens.SpaceLg)
            ) {
                Text(
                    text = "Discover Status",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceXxs))
                Text(
                    text = "Search across 300+ Hindi, Hinglish & English designs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceMd))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { Text("Search templates, quotes, shayari, styles...") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Search icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { searchQuery = "" }
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Clear search query",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(Dimens.RadiusFull),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    )
                )

                Spacer(modifier = Modifier.height(Dimens.SpaceMd))

                // Category filter chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                ) {
                    items(allCategories) { category ->
                        CategoryChip(
                            label = category,
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceMd))

                // Style & Aesthetics filter chips
                Text(
                    text = "Style & Aesthetics",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                ) {
                    items(allTags) { tag ->
                        val isSelected = currentTag == tag
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                currentTag = tag
                                onTagSelected(tag)
                            },
                            label = {
                                Text(
                                    text = if (tag == "All") "All Styles" else tag,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(Dimens.RadiusFull),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceMd))

                // Language filter chips
                Text(
                    text = "Language",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                ) {
                    items(languages) { lang ->
                        val isSelected = selectedLanguage == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLanguage = lang },
                            label = {
                                Text(
                                    text = if (lang == "All") "All Languages" else lang,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(Dimens.RadiusFull),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Active filters banner with clear action
                AnimatedVisibility(
                    visible = isFilterActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.SpaceMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filters applied",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Dimens.RadiusFull))
                                .clickable {
                                    searchQuery = ""
                                    selectedCategory = "All"
                                    selectedLanguage = "All"
                                    currentTag = "All"
                                    onTagSelected("All")
                                }
                                .padding(horizontal = Dimens.SpaceSm, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterAltOff,
                                contentDescription = "Clear all filters",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear all filters",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ── Results count & section title ──
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(
                title = if (isFilterActive) "Filtered Results" else "All Templates",
                modifier = Modifier.padding(
                    start = Dimens.SpaceLg,
                    end = Dimens.SpaceLg,
                    top = Dimens.SpaceLg
                ),
                actionLabel = "${filteredTemplates.size} found"
            )
        }

        // ── Results or empty state ──
        if (filteredTemplates.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceXl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = "No templates found",
                        subtitle = if (searchQuery.isNotBlank()) {
                            "No results matching \"$searchQuery\" in $selectedCategory ($selectedLanguage)."
                        } else {
                            "No designs match the selected filters."
                        },
                        modifier = Modifier.height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceMd))
                    Button(
                        onClick = {
                            searchQuery = ""
                            selectedCategory = "All"
                            selectedLanguage = "All"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(Dimens.RadiusFull)
                    ) {
                        Text("Reset Filters")
                    }
                }
            }
        } else {
            itemsIndexed(
                items = filteredTemplates,
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
