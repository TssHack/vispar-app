package com.fazli.vispar.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    text = "آیا از حذف تمام موارد مورد علاقه مطمئن هستید؟ این عمل قابل بازگشت نیست.",
                    fontFamily = VazirFontFamily,
                    textAlign = TextAlign.Right
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
                        fontFamily = VazirFontFamily
                    )
                }
            }
        )
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // هدر با دکمه بازگشت و دکمه حذف همه
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = Color(0xFF6200EE)
                )
            }
            
            Text(
                text = stringResource(R.string.favorites),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                fontSize = 20.sp,
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold
            )
            
            // دکمه حذف همه (فقط در صورت وجود موارد مورد علاقه نمایش داده شود)
            if (favorites.isNotEmpty()) {
                IconButton(
                    onClick = { showDeleteAllDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف همه",
                        tint = Color.Red
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom = 16.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = "هنوز مورد علاقه‌ای اضافه نشده",
                        fontSize = 18.sp,
                        fontFamily = VazirFontFamily,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // تصویر پوستر
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(favorite.image)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = favorite.title,
                modifier = Modifier
                    .height(100.dp)
                    .width(70.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
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
                    maxLines = 2
                )
                
                // نمایش نوع و سال
                Text(
                    text = "${getTypeInPersian(favorite.type)} • ${favorite.year}",
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = 14.sp,
                    fontFamily = VazirFontFamily,
                    color = Color.Gray
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 4.dp),
                        tint = Color.Red
                    )
                    Text(
                        text = String.format("%.1f", favorite.imdb),
                        fontSize = 14.sp,
                        fontFamily = VazirFontFamily
                    )
                }
            }
            
            // دکمه حذف برای مورد خاص
            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف",
                    tint = Color.Red
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