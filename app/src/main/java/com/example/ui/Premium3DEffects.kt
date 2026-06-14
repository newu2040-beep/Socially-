package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- BRAND COLOR METRIC FOR SOCIALY ---
object SociallyTheme {
    // Elegant light palette coordinates extracted from the Sleek Interface specs
    val PageBg = Color(0xFFFDFCFB)       // #fdfcfb (warm cozy ivory/linen background)
    val TextMain = Color(0xFF1F1B16)     // #1f1b16 (deep rich loam charcoal text)
    val TextMuted = Color(0xFF4E4539)    // #4e4539 (secondary tone warm loam text)
    val BrandBrown = Color(0xFF6F5B40)   // #6f5b40 (sleek main gold/bronze action brand color)
    val CardBeige = Color(0xFFF3EFEA)    // #f3efea (beautiful light secondary slot background)
    val BorderBeige = Color(0xFFE8E0D5)  // #e8e0d5 (light neat outline borders)
    val BorderTan = Color(0xFFD3C4B4)    // #d3c4b4 (medium decorative border outlines)
    val BadgeCream = Color(0xFFEFE0CF)   // #efe0cf (golden light badge indicator and secondary action buttons)
    
    // Maintain highly immersive retro film noir / negative dark styles for specific preview chassis
    val FilmNegativeBg = Color(0xFF2D2924) // #2d2924 (high-contrast camera mockup card backing)

    // Legacy variables for clean backwards compatibility & seamless compilation
    val GoldBrass = BrandBrown
    val WarmCream = PageBg
    val MatteCharcoal = PageBg            // Forces Scaffold canvas to adopt light theme immediately!
    val CardSlate = CardBeige
    val Terracotta = Color(0xFFD3C4B4)   // Soft tan/beige
    val TealCyan = BrandBrown
}

// --- FUJI CLASSIC FILM COLOR MATRIX REGISTRY ---
object FujiFilmMatrix {
    val Identity = ColorMatrix()

    // Warm, desaturated, classic high-contrast chrome
    val ClassicChrome = ColorMatrix(
        floatArrayOf(
            0.95f, 0.05f, 0.05f, 0f, -5f,
            0.03f, 0.88f, 0.03f, 0f, -2f,
            0.02f, 0.02f, 0.82f, 0f, 15f,
            0f,    0f,    0f,    1f, 0f
        )
    )

    // Vibrant green/blue hues, nostalgic warmth
    val SuperiaExtra = ColorMatrix(
        floatArrayOf(
            1.05f, -0.05f, 0f, 0f, 8f,
            0f,    1.12f,  0f, 0f, -12f,
            -0.05f,0.08f,  0.98f, 0f, 12f,
            0f,    0f,     0f,   1f, 0f
        )
    )

    // Ultra-vivid landscape depth, rich colors & contrast
    val Velvia = ColorMatrix(
        floatArrayOf(
            1.28f, -0.02f, -0.02f, 0f, -20f,
            -0.02f, 1.28f, -0.02f, 0f, -20f,
            -0.02f, -0.02f, 1.38f, 0f, -15f,
            0f,     0f,     0f,    1f, 0f
        )
    )

    // Pastel portrait feel, gentle shadows and lovely skin warmth
    val Astia = ColorMatrix(
        floatArrayOf(
            1.06f, 0.02f, 0.02f, 0f, 2f,
            0.02f, 1.04f, 0.02f, 0f, 3f,
            0.01f, 0.01f, 1.08f, 0f, -2f,
            0f,    0f,    0f,    1f, 0f
        )
    )

    // Saturated natural slide, beautiful blue skies
    val Provia = ColorMatrix(
        floatArrayOf(
            1.12f, 0.01f, 0.01f, 0f, -8f,
            0.01f, 1.15f, 0.01f, 0f, -8f,
            0.01f, 0.01f, 1.20f, 0f, -2f,
            0f,    0f,    0f,    1f, 0f
        )
    )

    // Dynamic, deep monochrome black and white with high-contrast grains
    val AcrosBW = ColorMatrix(
        floatArrayOf(
            0.299f, 0.587f, 0.114f, 0f, 12f,
            0.299f, 0.587f, 0.114f, 0f, 12f,
            0.299f, 0.587f, 0.114f, 0f, 12f,
            0f,     0f,     0f,     1f, 0f
        )
    )

    fun getMatrix(filterName: String): ColorMatrix {
        return when (filterName) {
            "Classic Chrome" -> ClassicChrome
            "Superia X-TRA" -> SuperiaExtra
            "Velvia" -> Velvia
            "Astia" -> Astia
            "Provia" -> Provia
            "Acros" -> AcrosBW
            else -> Identity
        }
    }

    fun getColorFilter(filterName: String): ColorFilter? {
        if (filterName == "Normal") return null
        return ColorFilter.colorMatrix(getMatrix(filterName))
    }
}

// Helper to construct combined ColorMatrix including edits (Brightness, Contrast, Saturation)
fun createCombinedMatrix(
    filterName: String,
    brightness: Float, // -1f to 1f, default 0f (maps directly to offset translation)
    contrast: Float,   // 0.5f to 1.5f, default 1f (maps to diagonal multipliers)
    saturation: Float, // 0f to 2f, default 1f (maps to desaturation weighting)
    warmth: Float      // -0.5f to 0.5f (adds gold/brass tint)
): ColorFilter {
    // 1. Start with the fuji color filter core matrix
    val base = getMatrixCopy(filterName)

    // 2. Apply Custom Brightness Translation
    // In color matrix, offsets are column 5 (indexes 4, 9, 14 representing R,G,B offsets in 0-255 scale)
    val brightOffset = brightness * 100f
    base[4] = base[4] + brightOffset
    base[9] = base[9] + brightOffset
    base[14] = base[14] + brightOffset

    // 3. Apply Warmth (warm gold yellowish tones: increase red and decrease blue slightly)
    val warmthFactor = warmth * 30f
    base[4] = base[4] + warmthFactor
    base[14] = base[14] - (warmthFactor * 0.8f)

    // 4. Apply Contrast (scale diagonal elements around midtones)
    val c = contrast
    base[0] = base[0] * c
    base[5] = base[5] * c
    base[10] = base[10] * c

    // 5. Apply Saturation (linear blending with luminance weights 0.3, 0.59, 0.11)
    val s = saturation
    if (s != 1f) {
        val rWeight = 0.299f
        val gWeight = 0.587f
        val bWeight = 0.114f

        val rInv = (1f - s) * rWeight
        val gInv = (1f - s) * gWeight
        val bInv = (1f - s) * bWeight

        // Blend base row multipliers with saturation weights
        base[0] = base[0] * s + rInv
        base[1] = base[1] * s + gInv
        base[2] = base[2] * s + bInv

        // G Row
        base[5] = base[5] * s + rInv
        base[6] = base[6] * s + gInv
        base[7] = base[7] * s + bInv

        // B Row
        base[10] = base[10] * s + rInv
        base[11] = base[11] * s + gInv
        base[12] = base[12] * s + bInv
    }

    return ColorFilter.colorMatrix(ColorMatrix(base))
}

private fun getMatrixCopy(filterName: String): FloatArray {
    val src = FujiFilmMatrix.getMatrix(filterName).values
    val copy = FloatArray(20)
    System.arraycopy(src, 0, copy, 0, 20)
    return copy
}

// --- 3D TACTILE BUTTONS AND PLY CARD LAYER DESIGNS ---

@Composable
fun TactileClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "",
    content: @Composable BoxScope.(isPressed: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth spring animation for physical retraction depth
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "clickScale"
    )

    val shadowElevation by animateFloatAsState(
        targetValue = if (isPressed && enabled) 1f else 6f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "clickShadow"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = shadowElevation.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.8f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom physical compression handles the ripple feel
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content(isPressed)
    }
}

// --- TILT 3D PARALLAX DEPTH CARD ---
@Composable
fun Interactive3DTiltCard(
    modifier: Modifier = Modifier,
    testTag: String = "interactive_fuji_card",
    content: @Composable BoxScope.(tiltX: Float, tiltY: Float) -> Unit
) {
    var rawDragX by remember { mutableStateOf(0f) }
    var rawDragY by remember { mutableStateOf(0f) }

    // Smooth interpolation back to center (using gentle damping Spec)
    val smoothDragX by animateFloatAsState(
        targetValue = rawDragX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltX"
    )
    val smoothDragY by animateFloatAsState(
        targetValue = rawDragY,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltY"
    )

    // Double underlying shadow offset layers to represent real floating physical depth!
    val layeredShadowOffset by animateOffsetAsState(
        targetValue = Offset(
            x = -smoothDragX * 0.15f,
            y = -smoothDragY * 0.15f
        ),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "shadowParallax"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(380.dp)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Constrain maximum tilt angle (-22f to 22f degrees)
                        rawDragX = (rawDragX + dragAmount.x * 0.08f).coerceIn(-22f, 22f)
                        rawDragY = (rawDragY + dragAmount.y * 0.08f).coerceIn(-22f, 22f)
                    },
                    onDragEnd = {
                        rawDragX = 0f
                        rawDragY = 0f
                    },
                    onDragCancel = {
                        rawDragX = 0f
                        rawDragY = 0f
                    }
                )
            }
    ) {
        // LAYER 1: Deep physical dark parallax shadow backdrop (asymmetrical background offset)
        Box(
            modifier = Modifier
                .offset(x = layeredShadowOffset.x.dp + 6.dp, y = layeredShadowOffset.y.dp + 12.dp)
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = smoothDragX
                    rotationX = -smoothDragY
                    cameraDistance = 16f
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.75f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        // LAYER 2: Back golden camera rim plate (creates bevelled metallic frame overlay)
        Box(
            modifier = Modifier
                .offset(x = (-smoothDragX * 0.05f).dp, y = (-smoothDragY * 0.05f).dp)
                .fillMaxSize()
                .padding(2.dp)
                .graphicsLayer {
                    rotationY = smoothDragX
                    rotationX = -smoothDragY
                    cameraDistance = 16f
                }
                .background(
                    Brush.linearGradient(
                        colors = listOf(SociallyTheme.GoldBrass, SociallyTheme.GoldBrass.copy(alpha = 0.6f)),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        // LAYER 3: Main image frame container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .graphicsLayer {
                    // Apply dynamic rotations
                    rotationY = smoothDragX
                    rotationX = -smoothDragY
                    cameraDistance = 16f
                }
                .background(SociallyTheme.FilmNegativeBg, shape = RoundedCornerShape(16.dp))
                .border(2.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Give contents the raw tracking vectors so inner children (like highlights, decals)
            // can offset themselves dynamically in the opposite direction (Parallax Holographic depth)!
            content(smoothDragX, smoothDragY)

            // Dynamic diagonal glass sheen glare overlaying on top!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Gloss angle shift is relative to raw drag horizontal displacement!
                        val startX = size.width * (0.1f + (smoothDragX / 50f))
                        val endX = size.width * (0.5f + (smoothDragX / 50f))
                        val brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0f),
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.16f),
                                Color.White.copy(alpha = 0f)
                            ),
                            start = Offset(startX, 0f),
                            end = Offset(endX, size.height)
                        )
                        drawRect(brush = brush)
                    }
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    }
}
