package com.lifeup.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.ui.theme.CategoryArt
import com.lifeup.app.ui.theme.CategoryLanguage
import com.lifeup.app.ui.theme.CategoryLife
import com.lifeup.app.ui.theme.CategoryMental
import com.lifeup.app.ui.theme.CategoryPhysical
import com.lifeup.app.ui.theme.CategorySocial
import com.lifeup.app.ui.theme.CategorySurvival

/**
 * Color resolver for skill categories.
 */
fun SkillCategory.color(): Color = when (this) {
    SkillCategory.LIVELIHOOD -> CategorySurvival
    SkillCategory.SOCIAL -> CategorySocial
    SkillCategory.LANGUAGE -> CategoryLanguage
    SkillCategory.LIFE -> CategoryLife
    SkillCategory.PHYSICAL -> CategoryPhysical
    SkillCategory.MENTAL -> CategoryMental
    SkillCategory.ART -> CategoryArt
}

/**
 * Custom-drawn icon for a skill category. The shape is intentionally
 * non-Material so the product has its own visual language.
 */
@Composable
fun CategoryIcon(
    category: SkillCategory,
    modifier: Modifier = Modifier,
    tint: Color = category.color(),
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        when (category) {
            SkillCategory.LIVELIHOOD -> {
                // Mountain (survival = 生存)
                val path = Path().apply {
                    moveTo(cx - w * 0.4f, cy + h * 0.3f)
                    lineTo(cx - w * 0.1f, cy - h * 0.35f)
                    lineTo(cx + w * 0.05f, cy - h * 0.1f)
                    lineTo(cx + w * 0.25f, cy - h * 0.35f)
                    lineTo(cx + w * 0.4f, cy + h * 0.3f)
                    close()
                }
                drawPath(path, tint)
                // Sun
                drawCircle(
                    color = tint.copy(alpha = 0.55f),
                    radius = w * 0.08f,
                    center = Offset(cx + w * 0.32f, cy - h * 0.28f)
                )
            }
            SkillCategory.SOCIAL -> {
                // Two overlapping speech bubbles
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(cx - w * 0.38f, cy - h * 0.30f),
                    size = Size(w * 0.48f, h * 0.36f),
                    cornerRadius = CornerRadius(w * 0.08f)
                )
                drawRoundRect(
                    color = tint.copy(alpha = 0.55f),
                    topLeft = Offset(cx - w * 0.10f, cy - h * 0.05f),
                    size = Size(w * 0.48f, h * 0.36f),
                    cornerRadius = CornerRadius(w * 0.08f)
                )
                // Dots
                drawCircle(Color.White, w * 0.05f, Offset(cx - w * 0.22f, cy - h * 0.12f))
                drawCircle(Color.White, w * 0.05f, Offset(cx - w * 0.05f, cy - h * 0.12f))
                drawCircle(Color.White, w * 0.05f, Offset(cx + w * 0.12f, cy - h * 0.12f))
            }
            SkillCategory.LANGUAGE -> {
                // Letter A with crossbar
                val path = Path().apply {
                    moveTo(cx - w * 0.32f, cy + h * 0.32f)
                    lineTo(cx, cy - h * 0.38f)
                    lineTo(cx + w * 0.32f, cy + h * 0.32f)
                    moveTo(cx - w * 0.18f, cy + h * 0.04f)
                    lineTo(cx + w * 0.18f, cy + h * 0.04f)
                }
                drawPath(
                    path = path,
                    color = tint,
                    style = Stroke(width = w * 0.10f)
                )
            }
            SkillCategory.LIFE -> {
                // House
                val path = Path().apply {
                    moveTo(cx - w * 0.35f, cy - h * 0.05f)
                    lineTo(cx, cy - h * 0.35f)
                    lineTo(cx + w * 0.35f, cy - h * 0.05f)
                    lineTo(cx + w * 0.35f, cy + h * 0.32f)
                    lineTo(cx - w * 0.35f, cy + h * 0.32f)
                    close()
                }
                drawPath(path, tint)
                // Door
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.7f),
                    topLeft = Offset(cx - w * 0.08f, cy + h * 0.05f),
                    size = Size(w * 0.16f, h * 0.27f),
                    cornerRadius = CornerRadius(w * 0.04f)
                )
            }
            SkillCategory.PHYSICAL -> {
                // Dumbbell
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(cx - w * 0.42f, cy - h * 0.22f),
                    size = Size(w * 0.12f, h * 0.44f),
                    cornerRadius = CornerRadius(w * 0.04f)
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(cx + w * 0.30f, cy - h * 0.22f),
                    size = Size(w * 0.12f, h * 0.44f),
                    cornerRadius = CornerRadius(w * 0.04f)
                )
                drawRoundRect(
                    color = tint.copy(alpha = 0.75f),
                    topLeft = Offset(cx - w * 0.20f, cy - h * 0.10f),
                    size = Size(w * 0.40f, h * 0.20f),
                    cornerRadius = CornerRadius(w * 0.05f)
                )
            }
            SkillCategory.MENTAL -> {
                // Brain-ish: two interlocking circles + center node
                drawCircle(
                    color = tint,
                    radius = w * 0.30f,
                    center = Offset(cx - w * 0.08f, cy)
                )
                drawCircle(
                    color = tint.copy(alpha = 0.75f),
                    radius = w * 0.30f,
                    center = Offset(cx + w * 0.08f, cy)
                )
                drawCircle(
                    color = Color.White,
                    radius = w * 0.08f,
                    center = Offset(cx, cy)
                )
            }
            SkillCategory.ART -> {
                // Palette: 4 dots inside a soft shape
                val path = Path().apply {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            offset = Offset(cx - w * 0.40f, cy - h * 0.30f),
                            size = Size(w * 0.80f, h * 0.70f)
                        )
                    )
                }
                drawPath(path, tint)
                // Thumb hole
                drawCircle(
                    color = Color.White,
                    radius = w * 0.08f,
                    center = Offset(cx + w * 0.22f, cy + h * 0.08f)
                )
                // Color dots
                drawCircle(tint.copy(alpha = 0.55f), w * 0.08f, Offset(cx - w * 0.20f, cy - h * 0.10f))
                drawCircle(tint.copy(alpha = 0.75f), w * 0.08f, Offset(cx - w * 0.05f, cy - h * 0.18f))
                drawCircle(tint.copy(alpha = 0.55f), w * 0.07f, Offset(cx - w * 0.18f, cy + h * 0.12f))
            }
        }
    }
}
