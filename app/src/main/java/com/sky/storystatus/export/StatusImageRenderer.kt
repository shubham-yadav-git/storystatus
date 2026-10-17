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
            is TemplateBackground.Pattern -> {
                drawPatternBackground(canvas, background, width, height)
            }
            is TemplateBackground.ImageResource -> {
                canvas.drawColor(Color.LTGRAY)
            }
        }
    }

    private fun drawPatternBackground(
        canvas: Canvas,
        pattern: TemplateBackground.Pattern,
        width: Int,
        height: Int
    ) {
        val baseColor = pattern.baseColor.toInt()
        val accentColor = pattern.accentColor.toInt()
        val secondaryColor = if (pattern.secondaryColor != 0L) pattern.secondaryColor.toInt() else accentColor

        canvas.drawColor(baseColor)
        val scale = (width / 1080f).coerceAtLeast(0.2f)

        when (pattern.patternType.lowercase()) {
            "grid" -> {
                val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    strokeWidth = 1.2f * scale
                    alpha = (Color.alpha(accentColor) * 0.35f).toInt()
                }
                val step = 72f * scale
                var x = 0f
                while (x <= width) {
                    canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
                    x += step
                }
                var y = 0f
                while (y <= height) {
                    canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
                    y += step
                }
                val crossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    strokeWidth = 2.5f * scale
                    alpha = (Color.alpha(accentColor) * 0.7f).toInt()
                }
                val crossLen = 6f * scale
                x = step
                while (x < width) {
                    y = step
                    while (y < height) {
                        canvas.drawLine(x - crossLen, y, x + crossLen, y, crossPaint)
                        canvas.drawLine(x, y - crossLen, x, y + crossLen, crossPaint)
                        y += step * 3
                    }
                    x += step * 3
                }
            }

            "dots" -> {
                val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = accentColor
                    alpha = (Color.alpha(accentColor) * 0.28f).toInt()
                }
                val step = 48f * scale
                val radius = 2.8f * scale
                var x = step / 2f
                while (x <= width) {
                    var y = step / 2f
                    while (y <= height) {
                        canvas.drawCircle(x, y, radius, dotPaint)
                        y += step
                    }
                    x += step
                }
            }

            "split" -> {
                val splitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = accentColor
                }
                val path = android.graphics.Path().apply {
                    moveTo(0f, height * 0.52f)
                    lineTo(width.toFloat(), height * 0.44f)
                    lineTo(width.toFloat(), height.toFloat())
                    lineTo(0f, height.toFloat())
                    close()
                }
                canvas.drawPath(path, splitPaint)

                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = secondaryColor
                    strokeWidth = 4f * scale
                }
                canvas.drawLine(0f, height * 0.52f, width.toFloat(), height * 0.44f, linePaint)
            }

            "stars" -> {
                val nebulaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = android.graphics.RadialGradient(
                        width * 0.5f, height * 0.35f,
                        width * 0.7f,
                        intArrayOf(accentColor, Color.TRANSPARENT),
                        floatArrayOf(0f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), nebulaPaint)

                val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = Color.WHITE
                }
                val random = java.util.Random(1337L)
                for (i in 0..70) {
                    val sx = random.nextFloat() * width
                    val sy = random.nextFloat() * height
                    val sRadius = (random.nextFloat() * 2.5f + 1f) * scale
                    starPaint.alpha = random.nextInt(180) + 75
                    canvas.drawCircle(sx, sy, sRadius, starPaint)
                }
                val sparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    strokeWidth = 1.8f * scale
                    alpha = 220
                }
                val sparkleCoords = listOf(
                    Pair(width * 0.2f, height * 0.25f),
                    Pair(width * 0.8f, height * 0.28f),
                    Pair(width * 0.15f, height * 0.75f),
                    Pair(width * 0.85f, height * 0.78f)
                )
                sparkleCoords.forEach { (cx, cy) ->
                    val len = 12f * scale
                    canvas.drawLine(cx - len, cy, cx + len, cy, sparklePaint)
                    canvas.drawLine(cx, cy - len, cx, cy + len, sparklePaint)
                }
            }

            "paper" -> {
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f * scale
                    color = accentColor
                    alpha = 180
                }
                val inset = 36f * scale
                canvas.drawRect(inset, inset, width - inset, height - inset, borderPaint)

                val fiberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    strokeWidth = 1f * scale
                    alpha = 35
                }
                var ly = 120f * scale
                while (ly < height - (120f * scale)) {
                    canvas.drawLine(inset + (20f * scale), ly, width - inset - (20f * scale), ly, fiberPaint)
                    ly += 42f * scale
                }
            }

            "newspaper" -> {
                val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    strokeWidth = 3f * scale
                }
                val thinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    strokeWidth = 1.2f * scale
                }
                val topMargin = 160f * scale
                canvas.drawLine(40f * scale, topMargin, width - (40f * scale), topMargin, rulePaint)
                canvas.drawLine(40f * scale, topMargin + (10f * scale), width - (40f * scale), topMargin + (10f * scale), thinPaint)

                val datePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    textSize = 18f * scale
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create("serif", Typeface.BOLD)
                    alpha = 180
                }
                canvas.drawText("SPECIAL EDITION • STORYSTATUS GAZETTE • ESTD. 2026", width / 2f, topMargin - (16f * scale), datePaint)

                val bottomMargin = height - (160f * scale)
                canvas.drawLine(40f * scale, bottomMargin - (10f * scale), width - (40f * scale), bottomMargin - (10f * scale), thinPaint)
                canvas.drawLine(40f * scale, bottomMargin, width - (40f * scale), bottomMargin, rulePaint)
            }

            "film" -> {
                val stripWidth = 56f * scale
                val borderFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = Color.BLACK
                }
                canvas.drawRect(0f, 0f, stripWidth, height.toFloat(), borderFill)
                canvas.drawRect(width - stripWidth, 0f, width.toFloat(), height.toFloat(), borderFill)

                val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = baseColor
                }
                val holeW = 28f * scale
                val holeH = 40f * scale
                val holeRadius = 6f * scale
                val spacing = 72f * scale
                var hy = 40f * scale
                while (hy < height - (40f * scale)) {
                    val leftRect = RectF((stripWidth - holeW) / 2f, hy, (stripWidth + holeW) / 2f, hy + holeH)
                    canvas.drawRoundRect(leftRect, holeRadius, holeRadius, holePaint)
                    val rightX = width - stripWidth + ((stripWidth - holeW) / 2f)
                    val rightRect = RectF(rightX, hy, rightX + holeW, hy + holeH)
                    canvas.drawRoundRect(rightRect, holeRadius, holeRadius, holePaint)
                    hy += spacing
                }
                val markPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(160, 255, 180, 0)
                    textSize = 14f * scale
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.MONOSPACE
                }
                canvas.drawText("▶ 24A", stripWidth / 2f, height * 0.48f, markPaint)
                canvas.drawText("400TX", width - (stripWidth / 2f), height * 0.48f, markPaint)
            }

            "arch" -> {
                val archLeft = 80f * scale
                val archTop = 180f * scale
                val archRight = width - (80f * scale)
                val archBottom = height - (140f * scale)
                val archRadius = (archRight - archLeft) / 2f

                val archPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = accentColor
                }
                val path = android.graphics.Path().apply {
                    moveTo(archLeft, archTop + archRadius)
                    arcTo(RectF(archLeft, archTop, archRight, archTop + (archRadius * 2f)), 180f, 180f)
                    lineTo(archRight, archBottom)
                    lineTo(archLeft, archBottom)
                    close()
                }
                canvas.drawPath(path, archPaint)

                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 3f * scale
                    color = secondaryColor
                }
                canvas.drawPath(path, strokePaint)
            }

            "aura" -> {
                val auraPaint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = android.graphics.RadialGradient(
                        width * 0.25f, height * 0.25f,
                        width * 0.65f,
                        intArrayOf(accentColor, Color.TRANSPARENT),
                        null,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), auraPaint1)

                val auraPaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = android.graphics.RadialGradient(
                        width * 0.75f, height * 0.75f,
                        width * 0.7f,
                        intArrayOf(secondaryColor, Color.TRANSPARENT),
                        null,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), auraPaint2)
            }

            else -> {
                canvas.drawColor(baseColor)
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

                    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = dec.color.toInt()
                    }
                    val radius = dec.cornerRadius * scale
                    canvas.drawRoundRect(rect, radius, radius, fillPaint)

                    if (dec.strokeColor != null && dec.strokeWidth > 0) {
                        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = dec.strokeWidth * scale
                            color = dec.strokeColor.toInt()
                        }
                        canvas.drawRoundRect(rect, radius, radius, strokePaint)
                    }
                }

                "polaroid" -> {
                    val left = dec.x * scale
                    val top = dec.y * scale
                    val w = dec.width * scale
                    val h = dec.height * scale
                    val cardRect = RectF(left, top, left + w, top + h)

                    val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = Color.WHITE
                    }
                    canvas.drawRoundRect(cardRect, 14f * scale, 14f * scale, whitePaint)

                    val photoPad = 24f * scale
                    val photoBottomPad = 130f * scale
                    val photoRect = RectF(left + photoPad, top + photoPad, left + w - photoPad, top + h - photoBottomPad)
                    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = dec.color.toInt()
                    }
                    canvas.drawRoundRect(photoRect, 8f * scale, 8f * scale, innerPaint)

                    // Washi tape sticker at top
                    val tapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = Color.argb(210, 245, 210, 130)
                    }
                    canvas.save()
                    canvas.rotate(-4f, left + (w / 2f), top)
                    canvas.drawRect(left + (w / 2f) - (65f * scale), top - (16f * scale), left + (w / 2f) + (65f * scale), top + (20f * scale), tapePaint)
                    canvas.restore()
                }

                "badge" -> {
                    val cx = dec.x * scale
                    val cy = dec.y * scale
                    val r = (dec.width * scale) / 2f
                    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = dec.strokeWidth * scale
                        color = dec.color.toInt()
                    }
                    canvas.drawCircle(cx, cy, r, badgePaint)
                    val innerBadgePaint = Paint(badgePaint).apply {
                        strokeWidth = maxOf(1f, dec.strokeWidth * 0.6f * scale)
                        pathEffect = DashPathEffect(floatArrayOf(8f * scale, 6f * scale), 0f)
                    }
                    canvas.drawCircle(cx, cy, r - (10f * scale), innerBadgePaint)

                    if (dec.label.isNotBlank()) {
                        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = dec.color.toInt()
                            textSize = (dec.width * 0.16f) * scale
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                letterSpacing = 0.12f
                            }
                        }
                        canvas.drawText(dec.label.uppercase(), cx, cy + (labelPaint.textSize / 3f), labelPaint)
                    }
                }

                "barcode" -> {
                    val startX = dec.x * scale
                    val startY = dec.y * scale
                    val w = dec.width * scale
                    val h = dec.height * scale
                    val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = dec.color.toInt()
                    }
                    var curX = startX
                    val rand = java.util.Random(99L)
                    while (curX < startX + w) {
                        val barW = (rand.nextInt(3) + 1) * 2f * scale
                        canvas.drawRect(curX, startY, curX + barW, startY + h, barPaint)
                        curX += barW + ((rand.nextInt(3) + 1) * 2f * scale)
                    }
                    val codePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = dec.color.toInt()
                        textSize = 14f * scale
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.MONOSPACE
                    }
                    canvas.drawText("STORYSTATUS • 890124", startX + (w / 2f), startY + h + (18f * scale), codePaint)
                }

                "circle_rings" -> {
                    val cx = dec.x * scale
                    val cy = dec.y * scale
                    val r = (dec.width * scale) / 2f
                    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = dec.strokeWidth * scale
                        color = dec.color.toInt()
                    }
                    canvas.drawCircle(cx, cy, r, ringPaint)
                    canvas.drawCircle(cx, cy, r * 0.68f, ringPaint.apply { alpha = 130 })
                    canvas.drawCircle(cx, cy, r * 0.36f, ringPaint.apply { alpha = 70 })
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
                    val endX = if (dec.x2 > 0) dec.x2 * scale else (dec.x + dec.width) * scale
                    val endY = if (dec.y2 > 0) dec.y2 * scale else dec.y * scale
                    canvas.drawLine(dec.x * scale, dec.y * scale, endX, endY, paint)
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
                        "seal" -> "❖"
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
