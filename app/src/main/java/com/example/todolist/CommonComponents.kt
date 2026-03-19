package com.example.todolist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ImmersiveDialog(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        content()
    }
}

@Composable
private fun BoxScope.ScrollArrowButton(
    visible: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    color: Color,
    offset: Float,
    alignment: Alignment,
    padding: PaddingValues,
    onClick: (() -> Unit)?
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(alignment).padding(padding)
    ) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .offset(y = offset.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = onClick != null
                ) { onClick?.invoke() },
            shape = CircleShape,
            color = color,
            contentColor = Color.Black
        ) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
fun ScrollArrowsOverlay(
    canScrollBackward: Boolean,
    canScrollForward: Boolean,
    color: Color = NeonCyan,
    onUpClick: (() -> Unit)? = null,
    onDownClick: (() -> Unit)? = null
) {
    val offset by rememberInfiniteTransition(label = "arrowAnim").animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "arrowOffset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        ScrollArrowButton(
            visible = canScrollBackward,
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Scroll Up",
            color = color,
            offset = offset,
            alignment = Alignment.TopCenter,
            padding = PaddingValues(top = 8.dp),
            onClick = onUpClick
        )
        ScrollArrowButton(
            visible = canScrollForward,
            icon = Icons.Default.KeyboardArrowDown,
            contentDescription = "Scroll Down",
            color = color,
            offset = offset,
            alignment = Alignment.BottomCenter,
            padding = PaddingValues(bottom = 8.dp),
            onClick = onDownClick
        )
    }
}

@Composable
fun NeonBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = color,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SimpleDropdown(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            color = SurfaceDark,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selected, color = Color.White, fontSize = 14.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 250.dp).background(SurfaceDark).border(1.dp, NeonCyan.copy(alpha = 0.3f))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = Color.White) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String
) {
    ImmersiveDialog(onDismissRequest = onDismiss) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("DELETE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF3366),
                    letterSpacing = 2.sp
                )
            },
            text = {
                Text(text = message, color = Color.White.copy(alpha = 0.8f))
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .padding(16.dp)
                .border(BorderStroke(1.dp, Color(0xFFFF3366).copy(alpha = 0.5f)), RoundedCornerShape(28.dp))
        )
    }
}