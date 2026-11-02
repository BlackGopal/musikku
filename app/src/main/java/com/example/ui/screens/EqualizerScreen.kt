package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomPresetEntity
import com.example.player.DefaultEqPresets
import com.example.player.EqPreset
import com.example.ui.components.liquidGlass
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.HeartRed
import com.example.ui.theme.NeonLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EqualizerScreen(
    isEqEnabled: Boolean,
    eqBands: List<Int>,
    activePresetName: String,
    customPresets: List<CustomPresetEntity>,
    hasHardwareEq: Boolean,
    isPowerSaver: Boolean,
    onToggleEq: (Boolean) -> Unit,
    onBandChanged: (Int, Int) -> Unit,
    onApplyPreset: (EqPreset) -> Unit,
    onSaveCustomPreset: (String, List<Int>) -> Unit,
    onDeleteCustomPreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    var showSavePresetDialog by remember { mutableStateOf(false) }

    val allPresets = remember(customPresets) {
        val builtIn = DefaultEqPresets.BUILT_IN
        val custom = customPresets.map {
            EqPreset(
                name = it.name,
                isCustom = true,
                bands = listOf(it.band0, it.band1, it.band2, it.band3, it.band4)
            )
        }
        builtIn + custom
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(bottom = 120.dp)
    ) {
        // Header with switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Equalizer 5-Band",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEqEnabled) "Aktif • Efek langsung terdengar" else "Nonaktif",
                    color = if (isEqEnabled) NeonLime else TextSecondary,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = isEqEnabled,
                onCheckedChange = onToggleEq,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = NeonLime,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = DarkSurfaceElevated
                ),
                modifier = Modifier.testTag("eq_master_switch")
            )
        }

        // Hardware Support Note if device has limitations
        if (!hasHardwareEq) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FF9100))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Output audio perangkat saat ini tidak menyediakan modul DSP hardware.",
                    color = TextPrimary,
                    fontSize = 11.sp
                )
            }
        }

        // Preset Chips Horizontal Row
        Text(
            text = "Preset Suara",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            allPresets.forEach { preset ->
                val isSelected = preset.name == activePresetName
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) NeonLime else DarkSurfaceElevated)
                        .clickable { onApplyPreset(preset) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("eq_preset_${preset.name.lowercase().replace(" ", "_")}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = preset.name,
                        color = if (isSelected) Color(0xFF0C1400) else TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    if (preset.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus Preset",
                            tint = if (isSelected) Color.Black else TextSecondary,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onDeleteCustomPreset(preset.name) }
                        )
                    }
                }
            }

            // Button to save current custom preset
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x26FFFFFF))
                    .clickable { showSavePresetDialog = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("save_custom_preset_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Simpan Preset",
                        tint = NeonLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Simpan Preset",
                        color = NeonLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5 Interactive Vertical Sliders Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .liquidGlass(
                    shape = RoundedCornerShape(28.dp),
                    backgroundColor = Color(0x18161226),
                    elevation = 8.dp,
                    isPowerSaver = isPowerSaver
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("+15 dB", color = TextSecondary, fontSize = 11.sp)
                    Text("0 dB", color = TextSecondary, fontSize = 11.sp)
                    Text("-15 dB", color = TextSecondary, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sliders list
                bandLabels.forEachIndexed { index, label ->
                    val bandVal = eqBands.getOrElse(index) { 0 }
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${if (bandVal > 0) "+$bandVal" else "$bandVal"} dB",
                                color = if (bandVal != 0) NeonLime else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Slider(
                            value = bandVal.toFloat(),
                            onValueChange = { newVal ->
                                onBandChanged(index, newVal.toInt())
                            },
                            valueRange = -15f..15f,
                            steps = 30,
                            enabled = isEqEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonLime,
                                activeTrackColor = NeonLime,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                                disabledThumbColor = TextSecondary,
                                disabledActiveTrackColor = TextSecondary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("eq_slider_band_$index")
                        )
                    }
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        var presetNameInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Simpan Preset Baru") },
            text = {
                Column {
                    Text(
                        text = "Simpan pengaturan slider saat ini sebagai preset kustom.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        placeholder = { Text("Contoh: Bass Head") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            onSaveCustomPreset(presetNameInput.trim(), eqBands)
                            showSavePresetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime)
                ) {
                    Text("Simpan", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
