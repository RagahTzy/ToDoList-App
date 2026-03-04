package com.example.todolist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.colorResource
import com.example.todolist.R

@Composable
fun StudentItem(
    mahasiswa: Mahasiswa,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.dark_surface)
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "NIM: ${mahasiswa.nim}",
                color = colorResource(id = R.color.neon_green)
            )
            Text(
                text = "Nama: ${mahasiswa.nama}",
                color = colorResource(id = R.color.text_primary)
            )
            Text(
                text = "Jurusan: ${mahasiswa.jurusan}",
                color = colorResource(id = R.color.text_primary)
            )
        }
    }
}