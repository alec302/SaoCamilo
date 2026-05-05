package com.pisc.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
expect fun SetStatusBarColors(isDarkTheme: Boolean, statusBarColor: Color)
