package com.sky.storystatus.ui.editor

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sky.storystatus.model.StatusTemplate
import com.sky.storystatus.ui.theme.Dimens
import com.sky.storystatus.ui.theme.categoryColor
import com.sky.storystatus.ui.theme.categoryEmoji

@Composable
fun PreviewScreen(
    template: StatusTemplate?,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    renderPreview: suspend (String) -> Bitmap?
) {
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(template?.id) {
        if (template != null) {
            isLoading = true
            previewBitmap = renderPreview(template.id)
            isLoading = false
        }
    }

    if (template == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Template not found", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Minimal Top Bar (Title only) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceMd),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // ── Preview Canvas ──
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = Dimens.Space3xl),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(Dimens.RadiusLg),
                        ambientColor = categoryColor(template.category).copy(alpha = 0.3f),
                        spotColor = categoryColor(template.category).copy(alpha = 0.2f)
                    )
                    .clip(RoundedCornerShape(Dimens.RadiusLg))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                previewBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Template Preview: ${template.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // ── Navigation & Heart below template, followed by Info & Actions ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = Dimens.RadiusXxl, topEnd = Dimens.RadiusXxl))
                .background(MaterialTheme.colorScheme.surface)
                .padding(Dimens.SpaceXxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row with Back (Navigation) and Heart (Favorite) below the template
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.SpaceMd),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(Dimens.RadiusFull)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    Text("Back")
                }

                IconButton(
                    onClick = { onToggleFavorite(template.id) },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(Dimens.RadiusFull)
                        )
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Category badge
            Box(
                modifier = Modifier
                    .background(
                        color = categoryColor(template.category).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(Dimens.RadiusFull)
                    )
                    .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceXs)
            ) {
                Text(
                    text = "${categoryEmoji(template.category)} ${template.category}",
                    style = MaterialTheme.typography.labelMedium,
                    color = categoryColor(template.category),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceSm))

            Text(
                text = template.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = template.language,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceXl))

            // Action buttons (Save, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
            ) {
                FilledTonalButton(
                    onClick = onSave,
                    modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
                    shape = RoundedCornerShape(Dimens.RadiusMd),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
                FilledTonalButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
                    shape = RoundedCornerShape(Dimens.RadiusMd),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    Text("Share", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceMd))

            // Edit button — primary CTA
            Button(
                onClick = { onEdit(template.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight),
                shape = RoundedCornerShape(Dimens.RadiusMd),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isLoading
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                Text("Customize This Template", fontWeight = FontWeight.Bold)
            }
        }
    }
}
