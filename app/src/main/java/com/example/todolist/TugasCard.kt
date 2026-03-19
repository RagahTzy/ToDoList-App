package com.example.todolist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private fun isDeadlineNear(deadline: String): Boolean = try {
    ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(deadline, DateTimeFormatter.ofPattern("dd-MM-yyyy"))) <= 1L
} catch (e: Exception) { false }

@Composable
private fun MuteButton(muted: Boolean, onClick: () -> Unit) {
    val color = if (muted) Color.Gray else NeonRed
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (muted) Icons.Default.NotificationsOff else Icons.Default.NotificationsActive,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = color
            )
            Text(
                text = if (muted) "NOTIF OFF" else "BERHENTI NOTIF",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = color,
                letterSpacing = 1.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TugasCardNeon(
    tugas: Tugas,
    baseColor: Color,
    catColor: Color,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleMute: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")
    val alpha by rememberInfiniteTransition(label = "borderTransition").animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "borderAlpha"
    )

    val isNearDeadline = remember(tugas.deadline) { isDeadlineNear(tugas.deadline) }
    val accentColor = if (isNearDeadline) NeonRed else baseColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
            .neonGlow(accentColor, alpha = 0.2f * alpha)
            .border(
                BorderStroke(1.dp, Brush.linearGradient(listOf(accentColor.copy(alpha = alpha), Color.Transparent))),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tugas.namaMatkul,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DEADLINE: ${tugas.deadline}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isNearDeadline) NeonRed else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3366))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeonBadge(text = tugas.kategoriTugas, color = baseColor)
                    NeonBadge(text = tugas.kategoriMatkul, color = catColor)
                    if (isNearDeadline) MuteButton(muted = tugas.reminderMuted, onClick = onToggleMute)
                }
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Expand", tint = baseColor, modifier = Modifier.rotate(rotation))
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = baseColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("DESCRIPTION", style = MaterialTheme.typography.labelSmall, color = baseColor, fontWeight = FontWeight.Black)
                    Text(
                        text = tugas.deskripsi.ifBlank { "No description provided." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}