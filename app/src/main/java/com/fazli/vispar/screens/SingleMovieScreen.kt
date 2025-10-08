package com.fazli.vispar.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fazli.vispar.R
import com.fazli.vispar.VideoPlayerActivity
import com.fazli.vispar.components.DownloadOptionsDialog
import com.fazli.vispar.data.model.FavoriteItem
import com.fazli.vispar.data.model.Movie
import com.fazli.vispar.data.model.Source
import com.fazli.vispar.ui.theme.VazirFontFamily  // import فونت از فایل مشترک
import com.fazli.vispar.utils.DownloadUtils
import com.fazli.vispar.utils.StorageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleMovieScreen(
    movieId: Int,
    navController: NavController
) {
    var movie by remember { mutableStateOf<Movie?>(null) }
    val context = LocalContext.current
    
    LaunchedEffect(movieId) {
        movie = StorageUtils.loadMovieFromFile(context, movieId)
    }
    
    // تنظیم جهت‌گیری راست‌چین برای کل صفحه
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                // نوار بالایی حرفه‌ای
                TopAppBar(
                    title = {
                        Text(
                            text = "جزئیات فیلم",
                            fontFamily = VazirFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        var isFavorite by remember { mutableStateOf(false) }
                        
                        // بررسی اینکه آیا فیلم قبلاً مورد علاقه شده است
                        LaunchedEffect(movieId) {
                            isFavorite = StorageUtils.isFavorite(context, movieId, "movie")
                        }
                        
                        IconButton(
                            onClick = {
                                if (isFavorite) {
                                    StorageUtils.removeFavorite(context, movieId, "movie")
                                    isFavorite = false
                                    // نمایش پیام Toast
                                    android.widget.Toast.makeText(context, "از موارد مورد علاقه حذف شد", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    movie?.let {
                                        // تبدیل فیلم به مورد علاقه با منابع
                                        val favoriteItem = FavoriteItem(
                                            id = it.id,
                                            type = "movie",
                                            title = it.title,
                                            description = it.description,
                                            year = it.year,
                                            imdb = it.imdb,
                                            rating = it.rating,
                                            duration = it.duration,
                                            image = it.image,
                                            cover = it.cover,
                                            genres = it.genres,
                                            country = it.country,
                                            sources = it.sources // شامل منابع در موارد مورد علاقه
                                        )
                                        StorageUtils.saveFavorite(context, favoriteItem)
                                        isFavorite = true
                                        // نمایش پیام Toast
                                        android.widget.Toast.makeText(context, "به موارد مورد علاقه اضافه شد", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "مورد علاقه",
                                tint = if (isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                
                if (movie != null) {
                    MovieDetailsContent(
                        movie = movie!!,
                        onPlayClick = { source ->
                            // راه‌اندازی اکتیویتی پخش ویدیو
                            VideoPlayerActivity.start(context, source.url)
                        }
                    )
                } else {
                    // نمایش وضعیت بارگذاری یا خطا
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Card(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "فیلم یافت نشد",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "متاسفانه اطلاعات فیلم مورد نظر در دسترس نیست",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = VazirFontFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieDetailsContent(
    movie: Movie,
    onPlayClick: (Source) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSource by remember { mutableStateOf<Source?>(null) }
    var showSourceDialog by remember { mutableStateOf(false) }
    
    // دیالوگ انتخاب منبع
    if (showSourceDialog && selectedSource != null) {
        SourceOptionsDialog(
            source = selectedSource!!,
            onDismiss = { showSourceDialog = false },
            onDownload = { source ->
                showSourceDialog = false
                DownloadUtils.openUrl(context, source.url)
            },
            onPlay = { source ->
                showSourceDialog = false
                onPlayClick(source)
            }
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // هدر فیلم با پس‌زمینه کاور و تصویر پیش‌زمینه
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            // تصویر پس‌زمینه کاور (تار شده)
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(movie.cover)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // هم‌گرادینت همپوشانی برای خوانایی بهتر متن
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )
            
            // پوستر پیش‌زمینه فیلم
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .height(220.dp)
                    .width(160.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(movie.image)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        // عنوان فیلم با کشور و سال
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = VazirFontFamily,
                fontSize = 26.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // کشور و سال
            val countryText = if (movie.country.isNotEmpty()) {
                "${movie.country.joinToString(", ") { it.title }} (${movie.year})"
            } else {
                "(${movie.year})"
            }
            
            Text(
                text = countryText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = VazirFontFamily,
                fontSize = 16.sp
            )
        }
        
        // امتیاز
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "امتیاز",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = String.format("%.1f", movie.imdb),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = VazirFontFamily,
                fontSize = 22.sp
            )
        }
        
        // ژانرها
        if (movie.genres.isNotEmpty()) {
            Text(
                text = "ژانرها",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                fontFamily = VazirFontFamily,
                fontSize = 18.sp
            )
            
            // نمایش بهبود یافته ژانرها با بسته‌بندی و استایل بهتر
            LazyRow(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(movie.genres) { genre ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(50.dp), // گوشه‌های گردتر
                        modifier = Modifier
                            .height(32.dp) // ارتفاع ثابت برای یکنواختی
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = genre.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium,
                                fontFamily = VazirFontFamily,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
        
        // توضیحات
        Text(
            text = "توضیحات",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            fontFamily = VazirFontFamily,
            fontSize = 18.sp
        )
        
        // تنظیم جهت‌چینی برای متن توضیحات به RTL
        Card(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = movie.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Right,
                fontFamily = VazirFontFamily,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
        
        // گزینه‌های منابع/کیفیت
        if (movie.sources.isNotEmpty()) {
            Text(
                text = "کیفیت‌های موجود",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                fontFamily = VazirFontFamily,
                fontSize = 18.sp
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            ) {
                movie.sources.forEach { source ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSource = source
                                showSourceDialog = true
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = source.quality,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = VazirFontFamily,
                                fontSize = 16.sp
                            )
                            
                            Card(
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "پخش",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SourceOptionsDialog(
    source: Source,
    onDismiss: () -> Unit,
    onDownload: (Source) -> Unit,
    onPlay: (Source) -> Unit
) {
    val context = LocalContext.current
    var showDownloadOptions by remember { mutableStateOf(false) }
    
    if (showDownloadOptions) {
        DownloadOptionsDialog(
            source = source,
            onDismiss = { showDownloadOptions = false },
            onCopyLink = { DownloadUtils.copyToClipboard(context, source.url) },
            onDownloadWithBrowser = { DownloadUtils.openUrl(context, source.url) },
            onDownloadWithADM = { DownloadUtils.openWithADM(context, source.url) },
            onOpenInVLC = { DownloadUtils.openWithVLC(context, source.url) },
            onOpenInMXPlayer = { DownloadUtils.openWithMXPlayer(context, source.url) },
            onOpenInKMPlayer = { DownloadUtils.openWithKMPlayer(context, source.url) }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = source.quality,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = VazirFontFamily,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "یک عمل برای این کیفیت ویدیو انتخاب کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = VazirFontFamily,
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showDownloadOptions = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation()
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "دانلود",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "گزینه‌های دانلود",
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                }
                
                Button(
                    onClick = { onPlay(source) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "پخش",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "پخش در برنامه",
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                }
                
                // دکمه انصراف به پایین دیالوگ منتقل شد
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "انصراف",
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 6.dp
    )
}

fun openUrl(context: Context, url: String) {
    DownloadUtils.openUrl(context, url)
}