package com.employeeapp.presentation.screens.topearners

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.employeeapp.domain.model.Employee
import com.employeeapp.presentation.components.EmployeeAvatar
import com.employeeapp.presentation.viewmodel.EmployeeListViewModel
import org.koin.compose.viewmodel.koinViewModel

private val RankColors = listOf(
    Color(0xFFFFD700),  // Gold - 1st
    Color(0xFFC0C0C0),  // Silver - 2nd
    Color(0xFFCD7F32),  // Bronze - 3rd
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopEarnersScreen(
    onNavigateBack: () -> Unit,
    viewModel: EmployeeListViewModel = koinViewModel()
) {
    val topEarners by viewModel.topEarners.collectAsState()
    val topN by viewModel.topNCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Top Earners", fontWeight = FontWeight.Bold)
                        Text("Min-Heap O(n log k) Algorithm", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // N stepper
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Top", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "$topN",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = topN.toFloat(),
                        onValueChange = { viewModel.updateTopN(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("1", style = MaterialTheme.typography.labelSmall)
                        Text("10", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(topEarners) { index, employee ->
                    TopEarnerCard(rank = index + 1, employee = employee)
                }
            }
        }
    }
}

@Composable
fun TopEarnerCard(rank: Int, employee: Employee) {
    val rankColor = RankColors.getOrElse(rank - 1) { MaterialTheme.colorScheme.primary }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(rankColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (rank <= 3) {
                    Icon(Icons.Default.EmojiEvents, null, tint = rankColor, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        "#$rank",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            EmployeeAvatar(
                name = employee.fullName,
                imagePath = employee.profileImagePath,
                department = employee.department.displayName,
                size = 44
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    employee.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    employee.department.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${"%,.0f".format(employee.salary)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (rank <= 3) {
                    Text(
                        "${rank}${if (rank == 1) "st" else if (rank == 2) "nd" else "rd"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = rankColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
