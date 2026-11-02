package com.fazli.vispar.ui.glass

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fazli.vispar.ui.theme.VazirFontFamily

/** Type ramp for the glass UI, built on the Vazir family. Colours come from [GlassColors]. */
object GlassType {
    val LargeTitle = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 44.sp)
    val Title = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 34.sp)
    val Title2 = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 30.sp)
    val Headline = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp)
    val Body = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 26.sp)
    val Callout = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp)
    val Caption = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp)
    val CaptionBold = Caption.copy(fontWeight = FontWeight.Bold)
}
