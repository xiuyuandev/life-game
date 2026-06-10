package com.lifeup.app.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeup.app.ui.components.*
import com.lifeup.app.ui.theme.PixelColors

@Composable
fun CharacterCreateScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onCharacterCreated: () -> Unit
) {
    val character = viewModel.character.collectAsState(initial = null).value
    var name by remember { mutableStateOf("") }

    if (character != null) {
        onCharacterCreated()
        return
    }

    // Floating particles
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelColors.DeepSpace),
        contentAlignment = Alignment.Center
    ) {
        FloatingParticlesBackground()

        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title with glow
            val titleScale by rememberInfiniteTransition(label = "title").animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    tween(3000, easing = EaseInOutSine),
                    RepeatMode.Reverse
                ),
                label = "titleScale"
            )

            Text(
                text = "⚔️",
                fontSize = 72.sp,
                modifier = Modifier.scale(titleScale)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "人生升级",
                style = MaterialTheme.typography.displayMedium.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = PixelColors.AccentGold.copy(alpha = 0.5f),
                        blurRadius = 24f
                    )
                ),
                color = PixelColors.AccentGold,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "LifeUp",
                style = MaterialTheme.typography.titleLarge,
                color = PixelColors.TextMuted
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Create character card
            GlassCard(
                glowColor = PixelColors.PrimaryGlow,
                contentPadding = PaddingValues(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "创建你的角色",
                        style = MaterialTheme.typography.headlineSmall,
                        color = PixelColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        PixelColors.AccentGold.copy(alpha = 0.2f),
                                        PixelColors.SurfaceVariant
                                    )
                                )
                            )
                            .border(3.dp, PixelColors.AccentGold.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧙", fontSize = 44.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("角色名称", color = PixelColors.TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PixelColors.TextPrimary,
                            unfocusedTextColor = PixelColors.TextSecondary,
                            focusedBorderColor = PixelColors.Primary,
                            unfocusedBorderColor = PixelColors.Border
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    GlowButton(
                        text = "开始冒险",
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.createCharacter(name)
                            }
                        },
                        brush = Brush.horizontalGradient(
                            listOf(PixelColors.Primary, PixelColors.PrimaryVariant)
                        ),
                        glowColor = PixelColors.PrimaryGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "用游戏的感觉来掌控人生",
                style = MaterialTheme.typography.bodyMedium,
                color = PixelColors.TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FloatingParticlesBackground() {
    val colors = listOf(
        PixelColors.AccentGold,
        PixelColors.Primary,
        PixelColors.Secondary,
        PixelColors.AccentBlue
    )

    repeat(15) { index ->
        val infiniteTransition = rememberInfiniteTransition(label = "particle$index")
        val offsetX by infiniteTransition.animateFloat(
            initialValue = -0.1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween((3000 + index * 500), easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 400)
            ),
            label = "x"
        )
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 1.1f,
            targetValue = -0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween((4000 + index * 600), easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 300)
            ),
            label = "y"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(
                    x = (offsetX * 1000).dp,
                    y = (offsetY * 1000).dp
                )
        ) {
            Box(
                modifier = Modifier
                    .size((4 + index % 6).dp)
                    .background(colors[index % colors.size].copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}

private val EaseInOutSine: Easing = Easing { -(kotlin.math.cos(Math.PI * it) - 1) / 2 }
