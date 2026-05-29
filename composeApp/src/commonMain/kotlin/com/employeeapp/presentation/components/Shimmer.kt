package com.employeeapp.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    val shimmerBase = MaterialTheme.colorScheme.surfaceVariant
    val shimmerHighlight = MaterialTheme.colorScheme.surface

    return Brush.linearGradient(
        colors = listOf(shimmerBase, shimmerHighlight, shimmerBase),
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    brush: Brush = ShimmerBrush()
) {
    Box(modifier = modifier.background(brush = brush, shape = MaterialTheme.shapes.small))
}

@Composable
fun EmployeeCardShimmer(brush: Brush = ShimmerBrush()) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(brush = brush)
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(Modifier.fillMaxWidth(0.6f).height(16.dp), brush)
                ShimmerBox(Modifier.fillMaxWidth(0.4f).height(12.dp), brush)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(Modifier.width(80.dp).height(24.dp).clip(MaterialTheme.shapes.extraLarge), brush)
                    ShimmerBox(Modifier.width(70.dp).height(24.dp).clip(MaterialTheme.shapes.extraLarge), brush)
                }
            }
            ShimmerBox(Modifier.size(32.dp).clip(CircleShape), brush)
        }
    }
}

@Composable
fun EmployeeListShimmer() {
    val brush = ShimmerBrush()
    LazyColumn {
        items(6) {
            EmployeeCardShimmer(brush)
        }
    }
}
