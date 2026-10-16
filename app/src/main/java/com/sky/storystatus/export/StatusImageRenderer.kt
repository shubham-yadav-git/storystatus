package com.sky.storystatus.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.sky.storystatus.model.StatusTemplate
import com.sky.storystatus.model.TemplateBackground
import com.sky.storystatus.model.TemplateCustomization
import com.sky.storystatus.model.TemplateDecoration
import com.sky.storystatus.model.TemplateTextLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface StatusImageRenderer {
    suspend fun render(
        template: StatusTemplate,
        customization: TemplateCustomization,
        width: Int = 1080,
        height: Int = 1920
    ): Bitmap
}

class DefaultStatusImageRenderer(private val context: Context) : StatusImageRenderer {

    override suspend fun render(
        template: StatusTemplate,
        customization: TemplateCustomization,
        width: Int,
        height: Int
    ): Bitmap = withContext(Dispatchers.Default) {
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // 1. Draw Background
        val background = customization.backgroundModification ?: template.background
        drawBackground(canvas, background, width, height)

        val referenceWidth = 1080f
        val referenceHeight = 1920f
        val scaleX = width / referenceWidth
        val scaleY = height / referenceHeight
        val scale = minOf(scaleX, scaleY)

        // 2. Draw Decorative Elements (Borders, Panels, Brackets, Dividers, Symbols)
        drawDecorations(canvas, template.decorations, scale, width, height)

        // 3. Draw Text Layers
        template.textLayers.forEach { layer ->
            val customText = customization.textModifications[layer.id] ?: layer.text
            val customColor = customization.colorModifications[layer.id] ?: layer.color
            val customFont = customization.fontModifications[layer.id] ?: layer.fontFamily
            val customFontSize = customization.fontSizeModifications[layer.id] ?: layer.fontSize
            val customAlignment = customization.alignmentModifications[layer.id] ?: layer.alignment
            val customY = customization.positionModifications[layer.id] ?: layer.y
            
            drawTextLayer(
                canvas = canvas,
                layer = layer.copy(
                    text = resolvePlaceholders(customText),
                    color = customColor,
                    fontFamily = customFont,
                    fontSize = customFontSize,
                    alignment = customAlignment,
                    y = customY
                ),
                scale = scale,
                canvasWidth = width
            )
        }

        bitmap
    }

    private fun resolvePlaceholders(text: String): String {
        return text
            .replace("{{name}}", "Priya")
            .replace("{{age}}", "25")
            .replace("{{message}}", "Best Wishes")
    }

    private fun drawBackground(canvas: Canvas, background: TemplateBackground, width: Int, height: Int) {
        val paint = Paint()
        when (background) {
            is TemplateBackground.Color -> {
                canvas.drawColor(background.colorValue.toInt())
            }
            is TemplateBackground.Gradient -> {
                if (background.colors.isNotEmpty()) {
                    val colors = background.colors.map { it.toInt() }.toIntArray()
                    paint.shader = when (background.type.lowercase()) {
                        "diagonal" -> LinearGradient(
                            0f, 0f, width.toFloat(), height.toFloat(),
                            colors,
                            null,
                            Shader.TileMode.CLAMP
                        )
                        "radial" -> android.graphics.RadialGradient(
                            width / 2f, height / 2f,
                            maxOf(width, height) * 0.75f,
                            colors,
                            null,
                            Shader.TileMode.CLAMP
                        )
                        else -> LinearGradient(
                            0f, 0f, 0f, height.toFloat(),
                            colors,
                            null,
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                } else {
                    canvas.drawColor(Color.DKGRAY)
                }
            }
            is TemplateBackground.ImageResource -> {
                canvas.drawColor(Color.LTGRAY)
            }
        }
    }

    private fun drawDecorations(
        canvas: Canvas,
        decorations: List<TemplateDecoration>,
        scale: Float,
        width: Int,
        height: Int
    ) {
        if (decorations.isEmpty()) return

        decorations.forEach { dec ->
            when (dec.type.lowercase()) {
                "border" -> {
                    val inset = if (dec.x > 0) dec.x * scale else 40f * scale
                    val strokeW = dec.strokeWidth * scale
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = strokeW
                        color = dec.color.toInt()
                        if (dec.style.lowercase() == "dashed") {
                            pathEffect = DashPathEffect(floatArrayOf(16f * scale, 12f * scale), 0f)
                        }
                    }

                    if (dec.cornerRadius > 0) {
                        val rect = RectF(inset, inset, width - inset, height - inset)
                        canvas.drawRoundRect(rect, dec.cornerRadius * scale, dec.cornerRadius * scale, paint)
                    } else {
                        canvas.drawRect(inset, inset, width - inset, height - inset, paint)
                    }

                    // Double border accent for luxury/editorial designs
                    if (dec.style.lowercase() == "double") {
                        val innerInset = inset + (14f * scale)
                        val innerPaint = Paint(paint).apply {
                            strokeWidth = maxOf(1f, strokeW * 0.7f)
                        }
                        if (dec.cornerRadius > 0) {
                            val innerRect = RectF(innerInset, innerInset, width - innerInset, height - innerInset)
                            canvas.drawRoundRect(innerRect, (dec.cornerRadius - 6f).coerceAtLeast(0f) * scale, (dec.cornerRadius - 6f).coerceAtLeast(0f) * scale, innerPaint)
                        } else {
                            canvas.drawRect(innerInset, innerInset, width - innerInset, height - innerInset, innerPaint)
                        }
                    }
                }

                "corner_brackets" -> {
                    val inset = if (dec.x > 0) dec.x * scale else 48f * scale
                    val length = if (dec.width > 0) dec.width * scale else 60f * scale
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = dec.strokeWidth * scale
                        color = dec.color.toInt()
                    }

                    // Top-left
                    canvas.drawLine(inset, inset, inset + length, inset, paint)
                    canvas.drawLine(inset, inset, inset, inset + length, paint)

                    // Top-right
                    canvas.drawLine(width - inset, inset, width - inset - length, inset, paint)
                    canvas.drawLine(width - inset, inset, width - inset, inset + length, paint)

                    // Bottom-left
                    canvas.drawLine(inset, height - inset, inset + length, height - inset, paint)
                    canvas.drawLine(inset, height - inset, inset, height - inset - length, paint)

                    // Bottom-right
                    canvas.drawLine(width - inset, height - inset, width - inset - length, height - inset, paint)
                    canvas.drawLine(width - inset, height - inset, width - inset, height - inset - length, paint)
                }

                "card_panel" -> {
                    val left = dec.x * scale
                    val top = dec.y * scale
                    val right = (dec.x + dec.width) * scale
                    val bottom = (dec.y + dec.height) * scale
                    val rect = RectF(left, top, right, bottom)

                    // Fill
                    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = dec.color.toInt()
                    }
                    val radius = dec.cornerRadius * scale
                    canvas.drawRoundRect(rect, radius, radius, fillPaint)

                    // Optional stroke
                    if (dec.strokeColor != null && dec.strokeWidth > 0) {
                        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = dec.strokeWidth * scale
                            color = dec.strokeColor.toInt()
                        }
                        canvas.drawRoundRect(rect, radius, radius, strokePaint)
                    }
                }

                "divider" -> {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = dec.strokeWidth * scale
                        color = dec.color.toInt()
                        if (dec.style.lowercase() == "dashed") {
                            pathEffect = DashPathEffect(floatArrayOf(10f * scale, 8f * scale), 0f)
                        }
                    }
                    canvas.drawLine(dec.x * scale, dec.y * scale, dec.x2 * scale, dec.y2 * scale, paint)
                }

                "symbol" -> {
                    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = dec.color.toInt()
                        textSize = (if (dec.width > 0) dec.width else 60f) * scale
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.create("serif", Typeface.BOLD)
                    }

                    val symbolChar = when (dec.symbolType.lowercase()) {
                        "quote" -> "“"
                        "sparkle" -> "✦"
                        "star" -> "★"
                        "heart" -> "♥"
                        else -> "✦"
                    }

                    if (dec.symbolType.lowercase() == "circle_badge") {
                        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = dec.strokeWidth * scale
                            color = dec.color.toInt()
                        }
                        canvas.drawCircle(dec.x * scale, dec.y * scale, (dec.width * scale) / 2f, circlePaint)
                    } else {
                        canvas.drawText(symbolChar, dec.x * scale, dec.y * scale, paint)
                    }
                }
            }
        }
    }

    private fun drawTextLayer(
        canvas: Canvas,
        layer: TemplateTextLayer,
        scale: Float,
        canvasWidth: Int
    ) {
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = layer.color.toInt()
            textSize = layer.fontSize * scale
            typeface = TypefaceCache.get(layer.fontFamily)

            // Tracking / letter spacing support
            if (layer.letterSpacing != 0f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                letterSpacing = layer.letterSpacing
            }

            // Custom shadow / glow or crisp default drop shadow
            if (layer.shadowColor != null) {
                setShadowLayer(10f * scale, 0f, 4f * scale, layer.shadowColor.toInt())
            } else {
                setShadowLayer(8f * scale, 0f, 3f * scale, Color.argb(160, 0, 0, 0))
            }
        }

        val scaledMaxWidth = (layer.maxWidth * scale).toInt()
        val textAlignment = when (layer.alignment.lowercase()) {
            "center" -> Layout.Alignment.ALIGN_CENTER
            "right" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_NORMAL
        }

        val lineSpacing = if (layer.lineSpacingMultiplier > 0f) layer.lineSpacingMultiplier else 1.2f

        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(layer.text, 0, layer.text.length, textPaint, scaledMaxWidth)
                .setAlignment(textAlignment)
                .setLineSpacing(0f, lineSpacing)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                layer.text, textPaint, scaledMaxWidth,
                textAlignment, lineSpacing, 0f, false
            )
        }

        canvas.save()
        
        val scaledX = layer.x * scale
        val scaledY = layer.y * scale
        val drawX = if (layer.alignment.lowercase() == "center") {
             scaledX - (scaledMaxWidth / 2f)
        } else {
             scaledX
        }
        val drawY = scaledY - (staticLayout.height / 2f)

        // Optional background highlight pill/box behind text
        if (layer.backgroundColor != null) {
            val padX = 24f * scale
            val padY = 12f * scale
            val bgRect = RectF(
                drawX - padX,
                drawY - padY,
                drawX + scaledMaxWidth + padX,
                drawY + staticLayout.height + padY
            )
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = layer.backgroundColor.toInt()
            }
            canvas.drawRoundRect(bgRect, 16f * scale, 16f * scale, bgPaint)
        }

        canvas.translate(drawX, drawY)
        staticLayout.draw(canvas)
        canvas.restore()
    }
}

/**
 * Thread-safe cache for parsed Typeface instances to avoid native re-allocations
 * during fast scroll rendering.
 */
object TypefaceCache {
    private val cache = java.util.concurrent.ConcurrentHashMap<String, Typeface>()

    fun get(fontFamily: String): Typeface {
        return cache.computeIfAbsent(fontFamily) { family ->
            when (family) {
                "sans-serif-medium" -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
                "sans-serif-bold" -> Typeface.create("sans-serif", Typeface.BOLD)
                "sans-serif-black" -> Typeface.create("sans-serif-black", Typeface.NORMAL)
                "sans-serif-light" -> Typeface.create("sans-serif-light", Typeface.NORMAL)
                "serif" -> Typeface.create("serif", Typeface.NORMAL)
                "serif-bold" -> Typeface.create("serif", Typeface.BOLD)
                "monospace" -> Typeface.MONOSPACE
                "casual" -> Typeface.create("casual", Typeface.NORMAL)
                else -> Typeface.DEFAULT
            }
        }
    }
}
