package com.ascend.screens.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ascend.ui.theme.panel
import com.ascend.ui.theme.primary


@Composable
fun CustomFloatingNavBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf(
        Icons.Default.Home,
        Icons.Default.AddBox,
        Icons.Default.MenuBook,
        Icons.Default.Person
    )

    Row(
        modifier = Modifier
            .width(380.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(panel),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, icon ->
            val isSelected = selectedIndex == index

            Box(
                modifier = Modifier
                    .size(width = 70.dp, height = 50.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) primary else Color.Transparent)
                    .clickable { onItemSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}