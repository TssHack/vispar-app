package com.fazli.vispar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fazli.vispar.R
import com.fazli.vispar.data.model.Source
import com.fazli.vispar.ui.theme.VazirFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadOptionsDialog(
    source: Source,
    onDismiss: () -> Unit,
    onCopyLink: () -> Unit,
    onDownloadWithBrowser: () -> Unit,
    onDownloadWithADM: () -> Unit,
    onOpenInVLC: () -> Unit,
    onOpenInMXPlayer: () -> Unit,
    onOpenInKMPlayer: () -> Unit
) {
    // تنظیم جهت‌گیری راست‌چین برای دیالوگ
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // هدر دیالوگ با طراحی جذاب
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // عنوان و کیفیت
                Text(
                    text = "گزینه‌های دانلود",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirFontFamily,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // کارت کیفیت
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "کیفیت: ${source.quality}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            fontFamily = VazirFontFamily,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // بخش گزینه‌های دانلود
                Text(
                    text = "دانلود",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = VazirFontFamily,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // گزینه‌های دانلود با طراحی کارت‌های زیبا
                DownloadOptionCard(
                    icon = Icons.Default.ContentCopy,
                    text = "کپی لینک",
                    description = "کپی لینک مستقیم ویدیو",
                    onClick = {
                        onCopyLink()
                        onDismiss()
                    },
                    iconColor = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DownloadOptionCard(
                    icon = Icons.Default.OpenInBrowser,
                    text = "دانلود با مرورگر",
                    description = "دانلود با مرورگر پیش‌فرض",
                    onClick = {
                        onDownloadWithBrowser()
                        onDismiss()
                    },
                    iconColor = MaterialTheme.colorScheme.tertiary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DownloadOptionCard(
                    icon = Icons.Default.Download,
                    text = "دانلود با ADM",
                    description = "دانلود با مدیریت دانلود ADM",
                    onClick = {
                        onDownloadWithADM()
                        onDismiss()
                    },
                    iconColor = Color(0xFF4CAF50) // رنگ سبز برای ADM
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // بخش گزینه‌های پخش
                Text(
                    text = "پخش",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = VazirFontFamily,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // گزینه‌های پخش با طراحی کارت‌های زیبا
                DownloadOptionCard(
                    icon = Icons.Default.PlayArrow,
                    text = "باز کردن در VLC Player",
                    description = "پخش مستقیم در VLC",
                    onClick = {
                        onOpenInVLC()
                        onDismiss()
                    },
                    iconColor = Color(0xFFFF9800) // رنگ نارنجی برای VLC
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DownloadOptionCard(
                    icon = Icons.Default.PlayArrow,
                    text = "باز کردن در MX Player",
                    description = "پخش مستقیم در MX Player",
                    onClick = {
                        onOpenInMXPlayer()
                        onDismiss()
                    },
                    iconColor = Color(0xFF2196F3) // رنگ آبی برای MX Player
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DownloadOptionCard(
                    icon = Icons.Default.PlayArrow,
                    text = "باز کردن در KM Player",
                    description = "پخش مستقیم در KM Player",
                    onClick = {
                        onOpenInKMPlayer()
                        onDismiss()
                    },
                    iconColor = Color(0xFF9C27B0) // رنگ بنفش برای KM Player
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // دکمه انصراف با طراحی زیبا
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(50.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "انصراف",
                                fontFamily = VazirFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadOptionCard(
    icon: ImageVector,
    text: String,
    description: String,
    onClick: () -> Unit,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // آیکون با پس‌زمینه گرد
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // متن و توضیحات
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontFamily = VazirFontFamily,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = VazirFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // فلش جهت‌نما
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}