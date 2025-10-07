package com.fazli.vispar.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fazli.vispar.R
import com.fazli.vispar.data.model.FavoriteItem
import com.fazli.vispar.navigation.AppScreens
import com.fazli.vispar.utils.StorageUtils

// تعریف فونت وزیری
private val VazirFontFamily = FontFamily(
    Font(R.font.vazir_regular, FontWeight.Normal),
    Font(R.font.vazir_bold, FontWeight.Bold)
)

@Composable
fun FavoritesScreen(navController: NavController) {
    var favorites by remember { mutableStateOf<List<FavoriteItem>>(emptyList()) }
    val context = LocalContext.current
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    
    // بارگذاری موارد مورد علاقه هنگام نمایش صفحه
    LaunchedEffect(Unit) {
        favorites = StorageUtils.loadAllFavorites(context)
    }
    
    // دیالوگ تایید حذف تمام موارد مورد علاقه
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { 
                Text(
                    text = "حذف تمام موارد مورد علاقه",
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = { 
                Text(
                    text = "آیا از حذف تمام موارد مورد علاقه مطمئن هستید؟ این عمل قابل بازگشت نیست.",
                    fontFamily = VazirFontFamily,
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        StorageUtils.clearAllFavorites(context)
                        favorites = emptyList()
                        showDeleteAllDialog = false
                        // نمایش پیام Toast
                        android.widget.Toast.makeText(context, "تمام موارد مورد علاقه حذف شدند", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = "حذف",
                        fontFamily = VazirFontFamily,
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAllDialog = false }
                ) {
                    Text(
                        text = "انصراف",
                        fontFamily = VazirFontFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
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
            // هدر با دکمه بازگشت و دکمه حذف همه
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
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
                    text = stringResource(R.string.favorites),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    fontSize = 22.sp,
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                // دکمه حذف همه (فقط در صورت وجود موارد مورد علاقه نمایش داده شود)
                if (favorites.isNotEmpty()) {
                    IconButton(
                        onClick = { showDeleteAllDialog = true },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف همه",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // محتوای اصلی
            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .size(120.dp)
                                .padding(bottom = 24.dp),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Text(
                            text = "هنوز مورد علاقه‌ای اضافه نشده",
                            fontSize = 20.sp,
                            fontFamily = VazirFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "برای افزودن به مورد علاقه‌ها، روی آیکون قلب در صفحه جزئیات ضربه بزنید",
                            fontSize = 16.sp,
                            fontFamily = VazirFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                ) {
                    items(favorites) { favorite ->
                        FavoriteItemCard(
                            favorite = favorite,
                            onClick = {
                                // ذخیره مورد علاقه در پایگاه داده مناسب قبل از هدایت
                                StorageUtils.saveFavoriteToDatabase(context, favorite)
                                
                                // هدایت به صفحه مناسب بر اساس نوع
                                when (favorite.type) {
                                    "movie" -> {
                                        navController.navigate("${AppScreens.SingleMovie.route.replace("{movieId}", favorite.id.toString())}")
                                    }
                                    "series" -> {
                                        navController.navigate("${AppScreens.SingleSeries.route.replace("{seriesId}", favorite.id.toString())}")
                                    }
                                }
                            },
                            onDelete = {
                                StorageUtils.removeFavorite(context, favorite.id, favorite.type)
                                // به‌روزرسانی لیست موارد مورد علاقه
                                favorites = StorageUtils.loadAllFavorites(context)
                                // نمایش پیام Toast
                                android.widget.Toast.makeText(context, "از موارد مورد علاقه حذف شد", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    
                    // اضافه کردن فضای خالی در انتهای لیست
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteItemCard(
    favorite: FavoriteItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // تصویر پوستر
            Card(
                modifier = Modifier
                    .height(120.dp)
                    .width(85.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(favorite.image)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = favorite.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // عنوان و جزئیات
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = favorite.title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = 18.sp,
                    fontFamily = VazirFontFamily,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // نمایش نوع و سال
                Row(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getTypeInPersian(favorite.type),
                        fontSize = 14.sp,
                        fontFamily = VazirFontFamily,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = " • ${favorite.year}",
                        fontSize = 14.sp,
                        fontFamily = VazirFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format("%.1f", favorite.imdb),
                        fontSize = 14.sp,
                        fontFamily = VazirFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // دکمه حذف برای مورد خاص
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        CircleShape
                    )
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// تابع کمکی برای تبدیل نوع به فارسی
private fun getTypeInPersian(type: String): String {
    return when (type.lowercase()) {
        "movie" -> "فیلم"
        "series" -> "سریال"
        else -> type
    }

}
