package com.example.todolist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsDialog(
    viewModel: TugasViewModel,
    onDismiss: () -> Unit
) {
    val settings by viewModel.notificationSettings.collectAsState()
    var mode by remember { mutableStateOf(settings.mode) }
    var intervalHours by remember { mutableStateOf(settings.intervalHours.toString()) }
    var intervalMinutes by remember { mutableStateOf(settings.intervalMinutes.toString()) }
    var hour by remember { mutableStateOf(settings.specificHour) }
    var minute by remember { mutableStateOf(settings.specificMinute) }
    val scope = rememberCoroutineScope()

    ImmersiveDialog(onDismissRequest = onDismiss) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateNotificationSettings(
                            NotificationSettings(
                                mode = mode,
                                intervalHours = intervalHours.toIntOrNull() ?: 1,
                                intervalMinutes = intervalMinutes.toIntOrNull() ?: 0,
                                specificHour = hour,
                                specificMinute = minute
                            )
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) {
                    Text("SAVE SETTINGS", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
            },
            title = { Text("NOTIF SETTINGS", color = NeonCyan, fontWeight = FontWeight.Black) },
            text = {
                val scrollState = rememberScrollState()
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 450.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Mode", color = Color.White, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { mode = NotificationMode.INTERVAL },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == NotificationMode.INTERVAL,
                                onClick = { mode = NotificationMode.INTERVAL },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                            )
                            Text("Interval Mode", color = Color.White)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { mode = NotificationMode.SPECIFIC_TIME },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == NotificationMode.SPECIFIC_TIME,
                                onClick = { mode = NotificationMode.SPECIFIC_TIME },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                            )
                            Text("Specific Time Mode", color = Color.White)
                        }

                        if (mode == NotificationMode.INTERVAL) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = intervalHours,
                                    onValueChange = { if (it.all { c -> c.isDigit() }) intervalHours = it },
                                    label = { Text("Hours", color = NeonCyan.copy(alpha = 0.6f)) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan
                                    )
                                )
                                OutlinedTextField(
                                    value = intervalMinutes,
                                    onValueChange = { if (it.all { c -> c.isDigit() }) intervalMinutes = it },
                                    label = { Text("Minutes", color = NeonCyan.copy(alpha = 0.6f)) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan
                                    )
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Time:", color = Color.White)
                                SimpleDropdown(
                                    options = (0..23).map { it.toString().padStart(2, '0') },
                                    selected = hour.toString().padStart(2, '0'),
                                    onSelected = { hour = it.toInt() },
                                    modifier = Modifier.width(70.dp)
                                )
                                Text(":", color = Color.White)
                                SimpleDropdown(
                                    options = (0..59).map { it.toString().padStart(2, '0') },
                                    selected = minute.toString().padStart(2, '0'),
                                    onSelected = { minute = it.toInt() },
                                    modifier = Modifier.width(70.dp)
                                )
                            }
                        }
                    }
                    ScrollArrowsOverlay(
                        canScrollBackward = scrollState.canScrollBackward,
                        canScrollForward = scrollState.canScrollForward,
                        onUpClick = { scope.launch { scrollState.animateScrollTo(0) } },
                        onDownClick = { scope.launch { scrollState.animateScrollTo(scrollState.maxValue) } }
                    )
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(24.dp)
        )
    }
}