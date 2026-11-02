package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.IconMuted
import com.example.ui.theme.NeonLime

enum class NavTab(val title: String, val testTag: String) {
    HOME("Beranda", "nav_tab_home"),
    LIBRARY("Pustaka", "nav_tab_library"),
    PLAYLISTS("Playlist", "nav_tab_playlists"),
    EQUALIZER("Equalizer", "nav_tab_equalizer"),
    SETTINGS("Pengaturan", "nav_tab_settings")
}

@Composable
fun FloatingBottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    isPowerSaver: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .height(64.dp)
                .liquidGlass(
                    shape = RoundedCornerShape(32.dp),
                    backgroundColor = Color(0x18141126),
                    elevation = 12.dp,
                    isPowerSaver = isPowerSaver
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val icon = getTabIcon(tab, isSelected)

                val pillBgColor by animateColorAsState(
                    targetValue = if (isSelected) NeonLime else Color.Transparent,
                    animationSpec = spring(),
                    label = "pill_bg"
                )
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF0F1500) else IconMuted,
                    animationSpec = spring(),
                    label = "icon_tint"
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(pillBgColor)
                        .bouncyClickable { onTabSelected(tab) }
                        .testTag(tab.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.title,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun getTabIcon(tab: NavTab, isSelected: Boolean): ImageVector {
    return when (tab) {
        NavTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
        NavTab.LIBRARY -> if (isSelected) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic
        NavTab.PLAYLISTS -> if (isSelected) Icons.Filled.QueueMusic else Icons.Outlined.QueueMusic
        NavTab.EQUALIZER -> if (isSelected) Icons.Filled.Equalizer else Icons.Outlined.Equalizer
        NavTab.SETTINGS -> if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings
    }
}
