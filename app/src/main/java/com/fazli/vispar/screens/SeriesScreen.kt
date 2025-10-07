package com.fazli.vispar.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fazli.vispar.R
import com.fazli.vispar.data.model.Series
import com.fazli.vispar.ui.series.SeriesViewModel
import com.fazli.vispar.utils.DeviceUtils
import com.fazli.vispar.utils.StorageUtils

// تعریف فونت وزیری
private val VazirFontFamily = FontFamily(
    Font(R.font.vazir_regular, FontWeight.Normal),
    Font(R.font.vazir_bold, FontWeight.Bold)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen(
    viewModel: SeriesViewModel = viewModel(),
    navController: NavController? = null
) {
    val series = viewModel.series
    val isLoading = viewModel.isLoading
    val isLoadingMore = viewModel.isLoadingMore
    val errorMessage = viewModel.errorMessage
    
    LaunchedEffect(Unit) {
        if (series.isEmpty()) {
            viewModel.loadSeries()
        }
    }
    
    // تنظیم جهت‌گیری راست‌چین برای کل صفحه
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                // نوار بالایی حرفه‌ای
                TopAppBar(
                    title = {
                        Text(
                            text = "سریال‌ها",
                            fontFamily = VazirFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                when {
                    isLoading && series.isEmpty() -> {
                        // نمایش انیمیشن مدرن هنگام بارگذاری اولیه سریال‌ها
                        LoadingScreenSeries()
                    }
                    errorMessage != null && series.isEmpty() -> {
                        ErrorScreenSeries(
                            errorMessage = errorMessage,
                            onRetry = { viewModel.retry() }
                        )
                    }
                    else -> {
                        SeriesGrid(
                            series = series,
                            isLoading = isLoading,
                            isLoadingMore = isLoadingMore,
                            errorMessage = errorMessage,
                            onRetry = { viewModel.retry() },
                            onRefresh = { viewModel.refresh() },
                            onLoadMore = { viewModel.loadMoreSeries() },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreenSeries() {
    val shimmerColor = MaterialTheme.colorScheme.surfaceVariant
    val shimmerColorShade = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    
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
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // اضافه کردن عنوان هنگام بارگذاری
        Text(
            text = "در حال بارگذاری سریال‌ها...",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
            fontFamily = VazirFontFamily,
            fontSize = 20.sp
        )
        
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(initialAlpha = 0.3f),
            exit = fadeOut()
        ) {
            val columns = DeviceUtils.getGridColumns(LocalContext.current.resources)
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(6) { // نمایش 6 جایگزین بارگذاری
                    ShimmerSeriesItem(shimmerColor, shimmerColorShade)
                }
            }
        }
    }
}

@Composable
fun ShimmerSeriesItem(
    shimmerColor: Color,
    shimmerColorShade: Color
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200)
        ), label = "shimmer_translate"
    )
    
    val brush = Brush.linearGradient(
        colors = listOf(
            shimmerColor,
            shimmerColorShade,
            shimmerColor,
            shimmerColorShade,
            shimmerColor
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // پوستر سریال شیمر
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // عنوان شیمر
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(24.dp)
                    .background(brush)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // سال شیمر
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(18.dp)
                    .background(brush)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ژانرها شیمر
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(18.dp)
                    .background(brush)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // امتیاز شیمر
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(brush)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(18.dp)
                        .background(brush)
                )
            }
        }
    }
}

@Composable
fun SeriesGrid(
    series: List<Series>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    navController: NavController? = null
) {
    val seriesList = series.toList()
    val context = LocalContext.current
    
    val columns = DeviceUtils.getGridColumns(LocalContext.current.resources)
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
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
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(seriesList) { index, seriesItem ->
            SeriesItem(
                series = seriesItem,
                onClick = {
                    // ذخیره سریال در حافظه
                    StorageUtils.saveSeriesToFile(context, seriesItem)
                    // هدایت به صفحه تکی سریال
                    navController?.navigate("single_series/${seriesItem.id}")
                }
            )
            
            // بارگذاری بیشتر وقتی به انتهای لیست نزدیک می‌شویم
            if (index >= seriesList.size - 3) {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }
        
        if (isLoadingMore) {
            item(span = { GridCells.Fixed(columns) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // نمایشگر پیشرفت مدرن متحرک
                    ModernCircularProgressIndicatorSeries()
                }
            }
        }
        
        if (errorMessage != null) {
            item(span = { GridCells.Fixed(columns) }) {
                ErrorItemSeries(
                    errorMessage = errorMessage,
                    onRetry = onRetry
                )
            }
        }
        
        // اضافه کردن فاصله کوچک در پایین برای جلوگیری از padding بیش از حد
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ModernCircularProgressIndicatorSeries() {
    val transition = rememberInfiniteTransition(label = "progress")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        ), label = "progress_anim"
    )
    
    // اضافه کردن انیمیشن چرخش برای افکت پویاتر
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            )
        ), label = "rotation_anim"
    )
    
    Card(
        modifier = Modifier
            .size(64.dp)
            .rotate(rotation),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SeriesItem(
    series: Series,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // پوستر سریال
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(series.image)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = series.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // جزئیات سریال
            Column {
                Text(
                    text = series.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = VazirFontFamily,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = series.year.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = VazirFontFamily,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // ژانرها
                if (series.genres.isNotEmpty()) {
                    Text(
                        text = series.genres.joinToString(", ") { it.title },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = VazirFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // امتیاز
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "امتیاز",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = String.format("%.1f", series.imdb),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorScreenSeries(
    errorMessage: String,
    onRetry: () -> Unit
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp),
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
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        
        Text(
            text = "خطا در بارگذاری سریال‌ها",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp),
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp),
            fontFamily = VazirFontFamily,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "تلاش مجدد",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "تلاش مجدد",
                fontFamily = VazirFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ErrorItemSeries(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "خطا در بارگذاری سریال‌های بیشتر",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(bottom = 12.dp),
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
            
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(bottom = 20.dp),
                fontFamily = VazilFontFamily,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onErrorContainer,
                    contentColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تلاش مجدد",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تلاش مجدد",
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}