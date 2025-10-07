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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fazli.vispar.R
import com.fazli.vispar.VideoPlayerActivity
import com.fazli.vispar.components.DownloadOptionsDialog
import com.fazli.vispar.data.model.FavoriteItem
import com.fazli.vispar.data.model.Episode
import com.fazli.vispar.data.model.Season
import com.fazli.vispar.data.model.Series
import com.fazli.vispar.data.model.Source
import com.fazli.vispar.ui.series.SeasonsViewModel
import com.fazli.vispar.utils.DownloadUtils
import com.fazli.vispar.utils.StorageUtils

// تعریف فونت وزیری
private val VazirFontFamily = FontFamily(
    Font(R.font.vazir_regular, FontWeight.Normal),
    Font(R.font.vazir_bold, FontWeight.Bold)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleSeriesScreen(
    seriesId: Int,
    navController: NavController
) {
    var series by remember { mutableStateOf<Series?>(null) }
    val context = LocalContext.current
    val seasonsViewModel: SeasonsViewModel = viewModel()
    var selectedEpisode by remember { mutableStateOf<Episode?>(null) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var showDownloadMenu by remember { mutableStateOf(false) }
    var downloadSources by remember { mutableStateOf<List<Source>>(emptyList()) }
    
    LaunchedEffect(seriesId) {
        series = StorageUtils.loadSeriesFromFile(context, seriesId)
        seasonsViewModel.loadSeasons(seriesId)
    }
    
    // دیالوگ انتخاب منبع
    if (showSourceDialog && selectedEpisode != null) {
        SourceOptionsDialog(
            episode = selectedEpisode!!,
            onDismiss = { showSourceDialog = false },
            onDownload = { source ->
                showSourceDialog = false
                openUrlSeries(context, source.url)
            },
            onPlay = { source ->
                showSourceDialog = false
                // راه‌اندازی اکتیویتی پخش ویدیو با URL منبع انتخاب شده
                VideoPlayerActivity.start(context, source.url)
            }
        )
    }
    
    // منوی دانلود
    if (showDownloadMenu && downloadSources.isNotEmpty()) {
        DownloadMenu(
            sources = downloadSources,
            onDismiss = { showDownloadMenu = false },
            onDownload = { source ->
                showDownloadMenu = false
                openUrlSeries(context, source.url)
            }
        )
    }
    
    // رندر محتوا بدون Scaffold چون قبلاً در MainScreen's Scaffold است
    if (series != null) {
        SeriesDetailsContent(
            series = series!!,
            seasonsViewModel = seasonsViewModel,
            onBackClick = { navController.popBackStack() },
            onEpisodeClick = { episode ->
                if (episode.sources.isNotEmpty()) {
                    if (episode.sources.size > 1) {
                        // نمایش دیالوگ انتخاب منبع اگر منابع متعدد وجود داشته باشد
                        selectedEpisode = episode
                        showSourceDialog = true
                    } else {
                        // پخش مستقیم اگر فقط یک منبع وجود داشته باشد
                        VideoPlayerActivity.start(context, episode.sources[0].url)
                    }
                }
            },
            onDownloadClick = { episode ->
                if (episode.sources.isNotEmpty()) {
                    if (episode.sources.size > 1) {
                        // نمایش منوی دانلود اگر منابع متعدد وجود داشته باشد
                        downloadSources = episode.sources
                        showDownloadMenu = true
                    } else {
                        // نمایش گزینه‌های دانلود حتی برای منبع تکی
                        downloadSources = episode.sources
                        showDownloadMenu = true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // نمایش وضعیت بارگذاری یا خطا
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت"
                    )
                }
                Text(
                    text = "سریال یافت نشد",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp),
                    fontFamily = VazirFontFamily,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun SourceOptionsDialog(
    episode: Episode,
    onDismiss: () -> Unit,
    onDownload: (Source) -> Unit,
    onPlay: (Source) -> Unit
) {
    val context = LocalContext.current
    var selectedSource by remember { mutableStateOf<Source?>(null) }
    var showDownloadOptions by remember { mutableStateOf(false) }
    
    if (showDownloadOptions && selectedSource != null) {
        DownloadOptionsDialog(
            source = selectedSource!!,
            onDismiss = { showDownloadOptions = false },
            onCopyLink = { DownloadUtils.copyToClipboard(context, selectedSource!!.url) },
            onDownloadWithBrowser = { DownloadUtils.openUrl(context, selectedSource!!.url) },
            onDownloadWithADM = { DownloadUtils.openWithADM(context, selectedSource!!.url) },
            onOpenInVLC = { DownloadUtils.openWithVLC(context, selectedSource!!.url) },
            onOpenInMXPlayer = { DownloadUtils.openWithMXPlayer(context, selectedSource!!.url) },
            onOpenInKMPlayer = { DownloadUtils.openWithKMPlayer(context, selectedSource!!.url) }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "قسمت: ${episode.title}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = VazirFontFamily,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = "کیفیت را برای پخش انتخاب کنید",
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
                // دکمه‌های پخش برای هر منبع/کیفیت
                episode.sources.forEach { source ->
                    Button(
                        onClick = { onPlay(source) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = androidx.compose.material3.ButtonDefaults.elevatedButtonElevation()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "پخش",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "پخش ${source.quality}",
                            fontFamily = VazirFontFamily,
                            fontSize = 16.sp
                        )
                    }
                }
                
                // دکمه انصراف
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "انصراف",
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 6.dp
    )
}

@Composable
fun DownloadMenu(
    sources: List<Source>,
    onDismiss: () -> Unit,
    onDownload: (Source) -> Unit
) {
    val context = LocalContext.current
    var selectedSource by remember { mutableStateOf<Source?>(null) }
    var showDownloadOptions by remember { mutableStateOf(false) }
    
    // اگر فقط یک منبع وجود دارد، مستقیماً به عنوان انتخاب شده تنظیم و گزینه‌های دانلود را نمایش می‌دهیم
    LaunchedEffect(sources) {
        if (sources.size == 1) {
            selectedSource = sources[0]
            showDownloadOptions = true
        }
    }
    
    if (showDownloadOptions && selectedSource != null) {
        DownloadOptionsDialog(
            source = selectedSource!!,
            onDismiss = { 
                showDownloadOptions = false
                // اگر فقط یک منبع داشتیم، منو را نیز ببند
                if (sources.size == 1) {
                    onDismiss()
                }
            },
            onCopyLink = { DownloadUtils.copyToClipboard(context, selectedSource!!.url) },
            onDownloadWithBrowser = { DownloadUtils.openUrl(context, selectedSource!!.url) },
            onDownloadWithADM = { DownloadUtils.openWithADM(context, selectedSource!!.url) },
            onOpenInVLC = { DownloadUtils.openWithVLC(context, selectedSource!!.url) },
            onOpenInMXPlayer = { DownloadUtils.openWithMXPlayer(context, selectedSource!!.url) },
            onOpenInKMPlayer = { DownloadUtils.openWithKMPlayer(context, selectedSource!!.url) }
        )
    }
    
    // فقط انتخاب کیفیت را نمایش می‌دهیم اگر منابع متعدد وجود داشته باشد
    if (sources.size > 1) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "انتخاب کیفیت برای دانلود",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirFontFamily,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "یک گزینه کیفیت برای دانلود انتخاب کنید",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = VazirFontFamily,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sources.forEach { source ->
                        Button(
                            onClick = { 
                                selectedSource = source
                                showDownloadOptions = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            elevation = androidx.compose.material3.ButtonDefaults.elevatedButtonElevation()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "دانلود",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = source.quality,
                                fontFamily = VazirFontFamily,
                                fontSize = 16.sp
                            )
                        }
                    }
                    
                    // دکمه انصراف
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "انصراف",
                            fontFamily = VazirFontFamily,
                            fontSize = 16.sp
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp
        )
    }
}

@Composable
fun SeriesDetailsContent(
    series: Series,
    seasonsViewModel: SeasonsViewModel,
    onBackClick: () -> Unit,
    onEpisodeClick: (Episode) -> Unit,
    onDownloadClick: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var selectedSeasonIndex by remember { mutableStateOf(0) }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        item {
            // هدر سریال با پس‌زمینه کاور و تصویر پیش‌زمینه
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // تصویر پس‌زمینه کاور (تار شده)
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(series.cover)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop
                )
                
                // هم‌گرادینت همپوشانی برای خوانایی بهتر متن
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                )
                
                // پوستر پیش‌زمینه سریال
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(series.image)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = series.title,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp)
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                
                // دکمه بازگشت
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // دکمه مورد علاقه
                var isFavorite by remember { mutableStateOf(false) }
                val context = LocalContext.current
                val seriesId = series.id
                
                // بررسی اینکه آیا سریال قبلاً مورد علاقه شده است
                LaunchedEffect(seriesId) {
                    isFavorite = StorageUtils.isFavorite(context, seriesId, "series")
                }
                
                IconButton(
                    onClick = {
                        if (isFavorite) {
                            StorageUtils.removeFavorite(context, seriesId, "series")
                            isFavorite = false
                            // نمایش پیام Toast
                            android.widget.Toast.makeText(context, "از موارد مورد علاقه حذف شد", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            // تبدیل سریال به مورد علاقه
                            val favoriteItem = FavoriteItem(
                                id = series.id,
                                type = "series",
                                title = series.title,
                                description = series.description,
                                year = series.year,
                                imdb = series.imdb,
                                rating = series.rating,
                                duration = series.duration,
                                image = series.image,
                                cover = series.cover,
                                genres = series.genres,
                                country = series.country
                            )
                            StorageUtils.saveFavorite(context, favoriteItem)
                            isFavorite = true
                            // نمایش پیام Toast
                            android.widget.Toast.makeText(context, "به موارد مورد علاقه اضافه شد", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "مورد علاقه",
                        tint = if (isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        item {
            // عنوان سریال با کشور و سال
            Text(
                text = series.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontFamily = VazirFontFamily,
                fontSize = 24.sp
            )
        }
        
        item {
            // کشور و سال
            val countryText = if (series.country.isNotEmpty()) {
                "${series.country.joinToString(", ") { it.title }} (${series.year})"
            } else {
                "(${series.year})"
            }
            
            Text(
                text = countryText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontFamily = VazirFontFamily,
                fontSize = 16.sp
            )
        }
        
        item {
            // امتیاز
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "امتیاز",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = String.format("%.1f", series.imdb),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = VazirFontFamily,
                    fontSize = 20.sp
                )
            }
        }
        
        item {
            // ژانرها
            if (series.genres.isNotEmpty()) {
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
                    items(series.genres) { genre ->
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
        }
        
        item {
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
        }
        
        item {
            // تنظیم جهت‌چینی برای متن توضیحات به RTL
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = series.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Right,
                    fontFamily = VazirFontFamily,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
        
        // انتخاب فصل‌ها
        if (seasonsViewModel.seasons.isNotEmpty()) {
            item {
                Text(
                    text = "فصل‌ها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                    fontFamily = VazirFontFamily,
                    fontSize = 18.sp
                )
            }
            
            item {
                LazyRow(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(seasonsViewModel.seasons.size) { index ->
                        val season = seasonsViewModel.seasons[index]
                        Card(
                            modifier = Modifier
                                .clickable { selectedSeasonIndex = index },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedSeasonIndex == index) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = season.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedSeasonIndex == index) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = VazirFontFamily,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // قسمت‌های فصل انتخاب شده
        item {
            if (seasonsViewModel.isLoading) {
                Text(
                    text = "در حال بارگذاری فصل‌ها...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp),
                    fontFamily = VazirFontFamily,
                    fontSize = 16.sp
                )
            } else if (seasonsViewModel.errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "خطا در بارگذاری فصل‌ها: ${seasonsViewModel.errorMessage}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { seasonsViewModel.loadSeasons(series.id) },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "تلاش مجدد",
                            fontFamily = VazirFontFamily,
                            fontSize = 16.sp
                        )
                    }
                }
            } else if (seasonsViewModel.seasons.isNotEmpty()) {
                val selectedSeason = seasonsViewModel.seasons[selectedSeasonIndex]
                Column {
                    Text(
                        text = selectedSeason.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                        fontFamily = VazirFontFamily,
                        fontSize = 18.sp
                    )
                    
                    selectedSeason.episodes.forEach { episode ->
                        EpisodeItem(
                            episode = episode,
                            onPlayClick = { onEpisodeClick(episode) },
                            onDownloadClick = { onDownloadClick(episode) }
                        )
                    }
                }
            } else {
                Text(
                    text = "فصلی موجود نیست",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                    fontFamily = VazirFontFamily,
                    fontSize = 16.sp
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EpisodeItem(
    episode: Episode,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // تصویر قسمت
                if (episode.image.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(episode.image)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = episode.title,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                // جزئیات قسمت
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = episode.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                    
                    // نمایش تعداد منابع اگر منابع متعدد وجود داشته باشد
                    if (episode.sources.size > 1) {
                        Text(
                            text = "${episode.sources.size} کیفیت موجود",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = VazirFontFamily,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            // دکمه‌های عملیات
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // دکمه دانلود
                if (episode.sources.isNotEmpty()) {
                    IconButton(
                        onClick = { onDownloadClick() },
                        modifier = Modifier
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "دانلود",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // دکمه پخش
                IconButton(
                    onClick = { onPlayClick() },
                    modifier = Modifier
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "پخش",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun openUrlSeries(context: Context, url: String) {
    DownloadUtils.openUrl(context, url)
}