package com.pisc.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle

@Composable
actual fun SetStatusBarColors(isDarkTheme: Boolean, statusBarColor: Color) {
    SideEffect {
        val style = if (isDarkTheme) UIUserInterfaceStyle.UIUserInterfaceStyleDark else UIUserInterfaceStyle.UIUserInterfaceStyleLight
        UIApplication.sharedApplication.keyWindow?.overrideUserInterfaceStyle = style
    }
}
