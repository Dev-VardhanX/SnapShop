package com.snapshop.app.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Custom Claymorphism modifier that adds:
 * 1. Soft dual-layer shadow depth
 * 2. Subtle directional 3D light gradient (soft molded clay volume)
 * 3. Soft specular inner highlight stroke along top-left
 * 4. Smooth physical compression when pressed
 */
fun Modifier.claySurface(
    shape: Shape = RoundedCornerShape(22.dp),
    color: Color = SurfaceLight,
    elevation: Dp = 6.dp,
    pressedElevation: Dp = 2.dp,
    highlightColor: Color = Color.White.copy(alpha = 0.85f),
    shadowColor: Color = ClayShadowLight,
    isPressed: Boolean = false,
    highlightStrokeWidth: Dp = 1.2.dp
): Modifier = this
    .shadow(
        elevation = if (isPressed) pressedElevation else elevation,
        shape = shape,
        ambientColor = shadowColor,
        spotColor = shadowColor
    )
    .clip(shape)
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                color,
                color.copy(alpha = 0.96f)
            ),
            start = Offset(0f, 0f),
            end = Offset(400f, 600f)
        )
    )
    .drawWithContent {
        drawContent()
        // Specular highlight on top-left edge
        val strokePx = highlightStrokeWidth.toPx()
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    highlightColor,
                    highlightColor.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width * 0.7f, size.height * 0.7f)
            ),
            size = size,
            style = Stroke(width = strokePx)
        )
    }

/**
 * Interactive Clay Card with tactile press feedback and smooth 3D molded elevation.
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    color: Color = SurfaceLight,
    elevation: Dp = 6.dp,
    pressedElevation: Dp = 2.dp,
    shadowColor: Color = ClayShadowLight,
    highlightColor: Color = Color.White.copy(alpha = 0.85f),
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentElevation by animateDpAsState(
        targetValue = if (isPressed && enabled) pressedElevation else elevation,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "clayElevation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && onClick != null) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "clayScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = currentElevation,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0.97f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(500f, 800f)
                )
            )
            .drawWithContent {
                drawContent()
                // Top-left subtle specular highlight
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            highlightColor,
                            highlightColor.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width * 0.8f, size.height * 0.8f)
                    ),
                    size = size,
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
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
    shape: Shape = RoundedCornerShape(18.dp),
    icon: ImageVector? = null,
    text: String? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    content: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (bgColor, textColor, highlight) = when (variant) {
        ClayButtonVariant.Primary -> Triple(
            PrimaryIndigo,
            OnPrimaryIndigo,
            Color.White.copy(alpha = 0.35f)
        )
        ClayButtonVariant.Secondary -> Triple(
            SecondaryLavenderLight,
            PrimaryIndigo,
            Color.White.copy(alpha = 0.8f)
        )
        ClayButtonVariant.Accent -> Triple(
            AccentMint,
            OnAccentMint,
            Color.White.copy(alpha = 0.4f)
        )
        ClayButtonVariant.Surface -> Triple(
            SurfaceLight,
            TextPrimaryLight,
            Color.White.copy(alpha = 0.9f)
        )
        ClayButtonVariant.Destructive -> Triple(
            SoftRed,
            RedHeart,
            Color.White.copy(alpha = 0.8f)
        )
    }

    val currentElevation by animateDpAsState(
        targetValue = if (!enabled) 0.dp else if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnElevation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )

    val effectiveBg = if (enabled) bgColor else bgColor.copy(alpha = 0.5f)
    val effectiveText = if (enabled) textColor else textColor.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = currentElevation,
                shape = shape,
                ambientColor = if (variant == ClayButtonVariant.Primary) PrimaryIndigo.copy(alpha = 0.35f) else ClayShadowLight,
                spotColor = if (variant == ClayButtonVariant.Primary) PrimaryIndigo.copy(alpha = 0.45f) else ClayKeyShadowLight
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        effectiveBg,
                        effectiveBg.copy(alpha = 0.92f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(300f, 400f)
                )
            )
            .drawWithContent {
                drawContent()
                if (enabled) {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                highlight,
                                highlight.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width * 0.7f, size.height * 0.7f)
                        ),
                        size = size,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }
            }
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
 * Tactile circular or rounded squircle clay icon button (for Wishlist, back, clear, settings).
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
    elevation: Dp = 4.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentElevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else elevation,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconBtnElevation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconBtnScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = currentElevation,
                shape = shape,
                ambientColor = ClayAmbientShadowLight,
                spotColor = ClayShadowLight
            )
            .clip(shape)
            .background(containerColor)
            .drawWithContent {
                drawContent()
                val drawSize = this.size
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(drawSize.width * 0.7f, drawSize.height * 0.7f)
                    ),
                    size = drawSize,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
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
 * Tactile molded clay chip / filter pill with pressed active state.
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
    val elevation = if (selected) 2.dp else if (isPressed) 1.dp else 4.dp
    val scale = if (isPressed) 0.96f else 1f

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(14.dp),
                ambientColor = ClayAmbientShadowLight,
                spotColor = if (selected) PrimaryIndigo.copy(alpha = 0.25f) else ClayShadowLight
            )
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = if (selected) {
                            listOf(
                                PrimaryIndigo.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color.Transparent
                            )
                        },
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    size = size,
                    style = Stroke(width = if (selected) 1.5.dp.toPx() else 1.dp.toPx())
                )
            }
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 7.dp, vertical = 3.dp),
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
    shape: Shape = RoundedCornerShape(22.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = ClayAmbientShadowLight,
                spotColor = ClayShadowLight
            )
            .clip(shape)
            .background(SurfaceLight)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceVariantLight.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(SurfaceVariantLight.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantLight.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(PrimaryIndigoContainer.copy(alpha = alpha))
            )
        }
    }
}

/**
 * Clean Clay Empty State Card with tactile illustration container and action.
 */
@Composable
fun ClayEmptyState(
    icon: ImageVector,
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(46.dp)
                )
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
