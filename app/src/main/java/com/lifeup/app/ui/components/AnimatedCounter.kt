package com.lifeup.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    var oldCount by remember { mutableIntStateOf(count) }

    SideEffect {
        oldCount = count
    }

    Row(modifier = modifier) {
        val countString = count.toString()
        val oldCountString = oldCount.toString()

        for (i in countString.indices) {
            val newChar = countString[i]
            val oldChar = oldCountString.getOrNull(i)

            if (newChar != oldChar) {
                AnimatedContent(
                    targetState = newChar,
                    transitionSpec = {
                        slideInVertically { it } with slideOutVertically { -it }
                    },
                    label = "counter"
                ) { char ->
                    Text(
                        text = char.toString(),
                        style = style,
                        softWrap = false
                    )
                }
            } else {
                Text(
                    text = newChar.toString(),
                    style = style,
                    softWrap = false
                )
            }
        }
    }
}
