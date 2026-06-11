package com.lifeup.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lifeup.app.ui.theme.EnergyAmber
import com.lifeup.app.ui.theme.EnergyAmberDark

@Composable
fun EnergyBar(
    current: Float,
    cap: Float,
    modifier: Modifier = Modifier
) {
    val progress = if (cap > 0f) (current / cap).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {
        contentDescription = "能量条: ${current.toInt()}/${cap.toInt()}"
    }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "能量",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${current.toInt()}/${cap.toInt()}",
                style = MaterialTheme.typography.labelMedium,
                color = EnergyAmber
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            color = EnergyAmber,
            trackColor = EnergyAmberDark.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}
