package com.fazli.vispar.ui.theme

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.fazli.vispar.R

object CustomIcons {
    // فقط شناسه‌های منابع را نگه می‌داریم
    val Movie: Int = R.drawable.ic_movie
    val Series: Int = R.drawable.ic_series
    val Search: Int = R.drawable.ic_search
    val Settings: Int = R.drawable.ic_settings
    
    // تابع کمکی برای تبدیل شناسه به ImageVector
    @Composable
    fun toImageVector(resourceId: Int): ImageVector {
        return vectorResource(resourceId)
    }
}
