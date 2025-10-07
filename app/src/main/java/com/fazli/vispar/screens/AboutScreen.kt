package com.fazli.vispar.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fazli.vispar.BuildConfig
import com.fazli.vispar.R

// تعریف فونت وزیری
private val VazirFontFamily = FontFamily(
    Font(R.font.vazir_regular, FontWeight.Normal),
    Font(R.font.vazir_bold, FontWeight.Bold)
)

@Composable
fun AboutScreen(navController: NavController?) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // تنظیم جهت‌گیری راست‌چین برای کل صفحه
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            // هدر با دکمه بازگشت
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController?.popBackStack() },
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = stringResource(R.string.about),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    fontSize = 22.sp,
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // محتوای اصلی
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // لوگوی برنامه
                Card(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(120.dp),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.splash_logo),
                        contentDescription = "لوگوی برنامه",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // نام برنامه
                Text(
                    text = "ویسپار",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirFontFamily,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // نسخه و معماری برنامه
                Card(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "نسخه ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) - ${getArchitecture()}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontFamily = VazirFontFamily,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // اطلاعات توسعه‌دهنده
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // تصویر توسعه‌دهنده - اصلاح مسیر فایل
                        Image(
                            painter = painterResource(id = R.drawable.ehsan), // فایل باید در پوشه drawable قرار گیرد
                            contentDescription = "تصویر توسعه‌دهنده",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "توسعه یافته توسط احسان فضلی",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            fontFamily = VazirFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "توسعه‌دهنده اندروید",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            fontFamily = VazirFontFamily,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // کارت لینک‌ها
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "ارتباط با ما",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp),
                            fontFamily = VazirFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Divider(
                            modifier = Modifier.padding(bottom = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // لینک کانال تلگرام
                        LinkItem(
                            icon = Icons.Default.Send,
                            text = "کانال تلگرام",
                            url = "https://t.me/nexzoteam"
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Divider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // لینک تلگرام توسعه‌دهنده
                        LinkItem(
                            icon = Icons.Default.Send,
                            text = "تلگرام توسعه‌دهنده",
                            url = "https://t.me/Devehsan"
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Divider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // لینک ایمیل توسعه‌دهنده
                        LinkItem(
                            icon = Icons.Default.Email,
                            text = "ایمیل توسعه‌دهنده",
                            url = "ehsanfazlinejad@gmail.com",
                            isEmail = true
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // کپی‌رایت
                Text(
                    text = "© ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} ویسپار. تمامی حقوق محفوظ است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontFamily = VazirFontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
fun LinkItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    url: String,
    isEmail: Boolean = false
) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = if (isEmail) {
                        Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$url")
                        }
                    } else {
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // مدیریت خطا در صورت نیاز
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = VazirFontFamily,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "باز کردن لینک",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getArchitecture(): String {
    return try {
        // دریافت معماری اصلی دستگاه
        val supportedAbis = android.os.Build.SUPPORTED_ABIS
        if (supportedAbis.isNotEmpty()) {
            when (supportedAbis[0]) {
                "arm64-v8a" -> "ARM64"
                "armeabi-v7a" -> "ARM32"
                "x86_64" -> "x86_64"
                "x86" -> "x86"
                else -> supportedAbis[0]
            }
        } else {
            "ناشناخته"
        }
    } catch (e: Exception) {
        "ناشناخته"
    }
}
