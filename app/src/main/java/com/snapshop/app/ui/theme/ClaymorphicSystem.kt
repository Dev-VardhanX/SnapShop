package com.snapshop.app.ui.theme

import android.os.Build
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

// =====================================================================
//  CLAY ENGINE
//  Real claymorphism = 4 layers:
//   1. soft dark drop shadow  (bottom-right, tinted, large blur)
//   2. soft white light shadow (top-left, large blur)  -> "puffy" look
//   3. gradient body (light top-left -> slightly shaded bottom-right)
//   4. inner highlight (top-left) + inner shade (bottom-right)
//  `inset = true` flips it into a pressed-in clay dent (search fields, selected chips).
//  Needs API 28+ for blurred shadow layers; below that it falls back to a plain elevation shadow.
// =====================================================================

private fun Outline.toClayPath(): Path = when (this) {
    is Outline.Rectangle -> Path().apply { addRect(rect) }
    is Outline.Rounded -> Path().apply { addRoundRect(roundRect) }
    is Outline.Generic -> path
}

private fun clayPaint(argb: Int) =
    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { this.color = argb }

fun Modifier.clayDepth(
    shape: Shape,
    color: Color,
    elevation: Dp = 6.dp,
    pressed: Float = 0f,
    inset: Boolean = false,
    dropShadow: Color = ClayDropShadow,
    highlight: Color = Color.White.copy(alpha = 0.9f),
    shade: Color = lerp(color, ClayShadeBase, 0.45f)
): Modifier = this
    .then(
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P && !inset) {
            Modifier.shadow(elevation, shape, ambientColor = dropShadow, spotColor = dropShadow)
        } else {
            Modifier
        }
    )
    .drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val path = outline.toClayPath()
        val nativePath = path.asAndroidPath()

        // ring = big rect minus the shape. Its shadow falls INSIDE the shape = inner shadow.
        val pad = 400f
        val ring = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(-pad, -pad, size.width + pad, size.height + pad))
            addPath(path)
        }
        val nativeRing = ring.asAndroidPath()

        val e = elevation.toPx()
        val ie = min(e, 8.dp.toPx()).coerceAtLeast(2.dp.toPx())
        val hasBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        val outer = if (inset) 0f else 1f - 0.6f * pressed
        val sunk = if (inset) 1f else pressed * 0.5f

        val opaque = color.copy(alpha = 1f).toArgb()
        val darkPaint = clayPaint(opaque)
        val lightPaint = clayPaint(opaque)
        val innerLight = clayPaint(opaque)
        val innerDark = clayPaint(opaque)

        val top = if (inset) lerp(color, shade, 0.07f) else lerp(color, Color.White, 0.2f)
        val bottom = if (inset) color else lerp(color, shade, 0.05f)
        val body = Brush.linearGradient(
            colors = listOf(top, bottom),
            start = Offset.Zero,
            end = Offset(size.width, size.height)
        )

        onDrawBehind {
            if (hasBlur && outer > 0f && e > 0f) {
                drawIntoCanvas { canvas ->
                    val nc = canvas.nativeCanvas
                    darkPaint.setShadowLayer(
                        e * 1.5f, e * 0.45f, e * 0.75f,
                        dropShadow.copy(alpha = dropShadow.alpha * outer).toArgb()
                    )
                    nc.drawPath(nativePath, darkPaint)
                    lightPaint.setShadowLayer(
                        e * 1.1f, -e * 0.45f, -e * 0.45f,
                        Color.White.copy(alpha = 0.8f * outer).toArgb()
                    )
                    nc.drawPath(nativePath, lightPaint)
                }
            }

            drawPath(path, brush = body)

            if (hasBlur) {
                clipPath(path) {
                    drawIntoCanvas { canvas ->
                        val nc = canvas.nativeCanvas
                        val dir = if (inset) -1f else 1f
                        val blur = ie * (0.6f + 0.6f * sunk)
                        val off = ie * 0.4f * (1f + sunk * 0.5f)
                        innerLight.setShadowLayer(blur, dir * off, dir * off, highlight.toArgb())
                        nc.drawPath(nativeRing, innerLight)
                        val darkAlpha = if (inset) 0.3f else 0.12f + 0.15f * sunk
                        innerDark.setShadowLayer(
                            blur, -dir * off, -dir * off,
                            shade.copy(alpha = darkAlpha).toArgb()
                        )
                        nc.drawPath(nativeRing, innerDark)
                    }
                }
            }
        }
    }

/** Backwards-compatible wrapper so existing `claySurface` callers keep working. */
fun Modifier.claySurface(
    shape: Shape = RoundedCornerShape(22.dp),
    color: Color = SurfaceLight,
    elevation: Dp = 6.dp,
    pressedElevation: Dp = 2.dp,
    highlightColor: Color = Color.White.copy(alpha = 0.9f),
    shadowColor: Color = ClayDropShadow,
    isPressed: Boolean = false,
    highlightStrokeWidth: Dp = 1.2.dp
): Modifier = this
    .clayDepth(
        shape = shape,
        color = color,
        elevation = elevation,
        pressed = if (isPressed) 1f else 0f,
        dropShadow = shadowColor,
        highlight = highlightColor
    )
    .clip(shape)

/**
 * Interactive Clay Card. `inset = true` makes a pressed-in dent (great for search fields).
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    color: Color = SurfaceLight,
    elevation: Dp = 5.dp,
    pressedElevation: Dp = 2.dp,
    shadowColor: Color = ClayDropShadow,
    highlightColor: Color = Color.White.copy(alpha = 0.9f),
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    inset: Boolean = false,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressActive = isPressed && enabled && onClick != null

    val press by animateFloatAsState(
        targetValue = if (pressActive) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "clayPress"
    )
    val scale by animateFloatAsState(
        targetValue = if (pressActive) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "clayScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clayDepth(
                shape = shape,
                color = color,
                elevation = elevation,
                pressed = press,
                inset = inset,
                dropShadow = shadowColor,
                highlight = highlightColor
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(contentPadding)
    ) {
        content()
    }
}

enum class ClayButtonVariant {
    Primary,
    Secondary,
    Accent,
    Surface,
    Destructive
}

/**
 * Tactile Clay Button that physically depresses when pressed.
 */
@Composable
fun ClayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ClayButtonVariant = ClayButtonVariant.Primary,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(20.dp),
    icon: ImageVector? = null,
    text: String? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    content: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor: Color
    val textColor: Color
    val highlight: Color
    val glow: Color
    when (variant) {
        ClayButtonVariant.Primary -> {
            bgColor = PrimaryIndigo; textColor = OnPrimaryIndigo
            highlight = Color.White.copy(alpha = 0.5f); glow = PrimaryIndigo.copy(alpha = 0.55f)
        }
        ClayButtonVariant.Secondary -> {
            bgColor = SecondaryLavenderLight; textColor = PrimaryIndigo
            highlight = Color.White.copy(alpha = 0.95f); glow = ClayDropShadow
        }
        ClayButtonVariant.Accent -> {
            bgColor = AccentMint; textColor = OnAccentMint
            highlight = Color.White.copy(alpha = 0.5f); glow = AccentMint.copy(alpha = 0.5f)
        }
        ClayButtonVariant.Surface -> {
            bgColor = SurfaceLight; textColor = TextPrimaryLight
            highlight = Color.White.copy(alpha = 0.95f); glow = ClayDropShadow
        }
        ClayButtonVariant.Destructive -> {
            bgColor = SoftRed; textColor = RedHeart
            highlight = Color.White.copy(alpha = 0.95f); glow = RedHeart.copy(alpha = 0.3f)
        }
    }

    val press by animateFloatAsState(
        targetValue = if (isPressed && enabled) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnPress"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )

    val effectiveText = if (enabled) textColor else textColor.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .scale(scale)
            .clayDepth(
                shape = shape,
                color = if (enabled) bgColor else bgColor.copy(alpha = 0.5f),
                elevation = if (enabled) 6.dp else 0.dp,
                pressed = press,
                dropShadow = glow,
                highlight = highlight
            )
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides effectiveText) {
            if (content != null) {
                content()
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        if (!text.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    if (!text.isNullOrBlank()) {
                        Text(
                            text = text,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tactile circular or squircle clay icon button (wishlist, back, clear, settings).
 */
@Composable
fun ClayIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp,
    shape: Shape = CircleShape,
    containerColor: Color = SurfaceLight,
    contentColor: Color = TextPrimaryLight,
    elevation: Dp = 5.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val press by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconBtnPress"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconBtnScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clayDepth(
                shape = shape,
                color = containerColor,
                elevation = elevation,
                pressed = press
            )
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Clay chip. Unselected = puffy raised pill, selected = pressed-in indigo dent.
 */
@Composable
fun ClayChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingText: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bg = if (selected) PrimaryIndigoContainer else SurfaceLight
    val textColor = if (selected) PrimaryIndigo else TextPrimaryLight
    val shape = RoundedCornerShape(16.dp)

    val press by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipPress"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clayDepth(
                shape = shape,
                color = bg,
                elevation = 4.dp,
                pressed = press,
                inset = selected,
                dropShadow = if (selected) PrimaryIndigo.copy(alpha = 0.25f) else ClayDropShadow
            )
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
            if (!trailingText.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trailingText,
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Molded clay badge for stores, ratings, price drops, categories.
 */
@Composable
fun ClayBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceVariantLight,
    textColor: Color = TextSecondaryLight,
    icon: ImageVector? = null,
    iconColor: Color = textColor
) {
    val shape = RoundedCornerShape(9.dp)
    Box(
        modifier = modifier
            .clayDepth(shape = shape, color = backgroundColor, elevation = 1.5.dp)
            .clip(shape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * Soft pulsing clay skeleton placeholder for loading states.
 */
@Composable
fun ClaySkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp = 220.dp,
    shape: Shape = RoundedCornerShape(24.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clayDepth(shape = shape, color = SurfaceLight, elevation = 5.dp)
            .clip(shape)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clayDepth(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceVariantLight.copy(alpha = alpha),
                        elevation = 3.dp,
                        inset = true
                    )
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clayDepth(RoundedCornerShape(7.dp), SurfaceVariantLight.copy(alpha = alpha), 1.dp)
                    .clip(RoundedCornerShape(7.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(16.dp)
                    .clayDepth(RoundedCornerShape(8.dp), SurfaceVariantLight.copy(alpha = alpha), 1.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(18.dp)
                    .clayDepth(RoundedCornerShape(9.dp), PrimaryIndigoContainer.copy(alpha = alpha), 1.dp)
                    .clip(RoundedCornerShape(9.dp))
            )
        }
    }
}

/**
 * Clean Clay Empty State Card with tactile illustration container and action.
 */
@Composable
fun ClayEmptyState(
    icon: ImageVector? = null,
    useLogo: Boolean = false,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ClayCard(
            modifier = Modifier.size(96.dp),
            shape = CircleShape,
            color = SurfaceLight,
            elevation = 6.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (useLogo || icon == null) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.snapshop.app.R.drawable.ic_snapshop_logo),
                        contentDescription = "SnapShop",
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimaryLight,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            ClayButton(
                onClick = onActionClick,
                text = actionText,
                variant = ClayButtonVariant.Primary
            )
        }
    }
}

