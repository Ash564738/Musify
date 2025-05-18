package com.example.musify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.musify.ui.navigation.MusifyBottomNavigationDestinations

object MusifyBottomNavigationConstants {
    val navigationHeight = 60.dp
}

@Composable
fun MusifyBottomNavigation(
    navigationItems: List<MusifyBottomNavigationDestinations>,
    currentlySelectedItem: MusifyBottomNavigationDestinations,
    onItemClick: (MusifyBottomNavigationDestinations) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController,
    currentRoute: String
) {
    val gradientBrush = remember {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to Color.Black,
                0.3f to Color.Black.copy(alpha = 0.9f),
                0.5f to Color.Black.copy(alpha = 0.8f),
                0.7f to Color.Black.copy(alpha = 0.6f),
                0.9f to Color.Black.copy(alpha = 0.2f),
                1f to Color.Transparent
            ),
            startY = Float.POSITIVE_INFINITY,
            endY = 0.0f
        )
    }

    Surface(
        modifier = Modifier
            .background(gradientBrush)
            .then(modifier),
        color = Color.Transparent,
        elevation = 0.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(MusifyBottomNavigationConstants.navigationHeight)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            navigationItems.forEach { item ->
                BottomNavigationItem(
                    selected = item.route == currentRoute,
                    onClick = { onItemClick(item) },
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                if (item.route == currentRoute) item.filledIconVariantResourceId
                                else item.outlinedIconVariantResourceId
                            ),
                            contentDescription = null
                        )
                    },
                    label = { Text(text = item.label) }
                )
            }
        }
    }
}