package com.fazli.vispar.utils

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

object DeviceUtils {
    
    /**
     * Check if the device is a TV
     */
    fun isTv(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }
    
    /**
     * Check if the device is a tablet
     */
    fun isTablet(context: Context): Boolean {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentDisplay
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay
        }
        
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonalInches = kotlin.math.sqrt(widthInches * widthInches + heightInches * heightInches)
        
        // Typically, tablets have a screen size of 7 inches or larger
        return diagonalInches >= 7.0
    }
    
    /**
     * Check if the device is a foldable phone
     */
    fun isFoldable(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.hasSystemFeature("android.hardware.type.foldable")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if the device is a wearable (watch)
     */
    fun isWearable(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_WATCH
    }
    
    /**
     * Check if the device is in landscape mode
     */
    fun isLandscape(context: Context): Boolean {
        val orientation = context.resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }
    
    /**
     * Check if the device is in portrait mode
     */
    fun isPortrait(context: Context): Boolean {
        val orientation = context.resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_PORTRAIT
    }
    
    /**
     * Get the screen size in dp
     * @return Pair(widthDp, heightDp)
     */
    fun getScreenSizeInDp(context: Context): Pair<Float, Float> {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentDisplay
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay
        }
        
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        
        val widthDp = metrics.widthPixels / metrics.density
        val heightDp = metrics.heightPixels / metrics.density
        
        return Pair(widthDp, heightDp)
    }
    
    /**
     * Get the screen aspect ratio
     * @return aspect ratio (width / height)
     */
    fun getScreenAspectRatio(context: Context): Float {
        val (widthDp, heightDp) = getScreenSizeInDp(context)
        return widthDp / heightDp
    }
    
    /**
     * Check if the device has a notch
     */
    fun hasNotch(context: Context): Boolean {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowManager.currentDisplay
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }
            
            val realSize = Point()
            val screenSize = Point()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(realSize)
            } else {
                @Suppress("DEPRECATION")
                realSize.x = display.width
                @Suppress("DEPRECATION")
                realSize.y = display.height
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                screenSize.x = display.bounds.width()
                screenSize.y = display.bounds.height()
            } else {
                @Suppress("DEPRECATION")
                screenSize.x = display.width
                @Suppress("DEPRECATION")
                screenSize.y = display.height
            }
            
            realSize.x != screenSize.x || realSize.y != screenSize.y
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the number of grid columns based on screen size and device type
     */
    fun getGridColumns(resources: Resources): Int {
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val isTv = resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION
        val isTablet = isTablet(resources)
        val isFoldable = isFoldable(resources)
        
        return when {
            isTv -> {
                when {
                    screenWidthDp >= 1920 -> 6 // Large TVs
                    screenWidthDp >= 1280 -> 5 // Medium TVs
                    else -> 4 // Small TVs
                }
            }
            isTablet -> {
                when {
                    screenWidthDp >= 1200 -> 4 // Large tablets
                    screenWidthDp >= 900 -> 3 // Medium tablets
                    else -> 2 // Small tablets
                }
            }
            isFoldable -> {
                when {
                    screenWidthDp >= 800 -> 4 // Unfolded
                    else -> 2 // Folded
                }
            }
            else -> {
                when {
                    screenWidthDp >= 600 -> 3 // Large phones
                    screenWidthDp >= 400 -> 2 // Medium phones
                    else -> 1 // Small phones
                }
            }
        }
    }
    
    /**
     * Get the optimal item size for grid layout
     * @return item size in dp
     */
    fun getOptimalItemSize(context: Context): Float {
        val (screenWidthDp, _) = getScreenSizeInDp(context)
        val columns = getGridColumns(context.resources)
        return (screenWidthDp - (columns + 1) * 16) / columns // 16dp spacing
    }
    
    /**
     * Check if the device has a hardware keyboard
     */
    fun hasHardwareKeyboard(context: Context): Boolean {
        return try {
            val config = context.resources.configuration
            config.keyboard == Configuration.KEYBOARD_QWERTY
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the device category (phone, tablet, tv, wearable, foldable)
     */
    fun getDeviceCategory(context: Context): String {
        return when {
            isTv(context) -> "TV"
            isWearable(context) -> "Wearable"
            isTablet(context) -> "Tablet"
            isFoldable(context) -> "Foldable"
            else -> "Phone"
        }
    }
    
    /**
     * Check if the device is in multi-window mode
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun isInMultiWindowMode(context: Context): Boolean {
        return context.isInMultiWindowMode
    }
    
    /**
     * Check if the device is in split-screen mode
     */
    fun isInSplitScreenMode(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.isInMultiWindowMode
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the status bar height in pixels
     */
    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            // Fallback for devices without status bar height defined
            (24 * resources.displayMetrics.density).toInt()
        }
    }
    
    /**
     * Get the navigation bar height in pixels
     */
    fun getNavigationBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            // Fallback for devices without navigation bar height defined
            (48 * resources.displayMetrics.density).toInt()
        }
    }
    
    /**
     * Check if the device has soft navigation keys
     */
    fun hasSoftNavigationKeys(context: Context): Boolean {
        return try {
            val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
            val hasBackKey = ViewConfiguration.get(context).hasBackKey()
            !hasMenuKey && !hasBackKey
        } catch (e: Exception) {
            false
        }
    }
}

// Composable extensions for easier use in Jetpack Compose

/**
 * Check if the current device is a TV (Composable)
 */
@Composable
fun isTv(): Boolean {
    val context = LocalContext.current
    return DeviceUtils.isTv(context)
}

/**
 * Check if the current device is a tablet (Composable)
 */
@Composable
fun isTablet(): Boolean {
    val context = LocalContext.current
    return DeviceUtils.isTablet(context)
}

/**
 * Check if the current device is in landscape mode (Composable)
 */
@Composable
fun isLandscape(): Boolean {
    val context = LocalContext.current
    return DeviceUtils.isLandscape(context)
}

/**
 * Check if the current device is in portrait mode (Composable)
 */
@Composable
fun isPortrait(): Boolean {
    val context = LocalContext.current
    return DeviceUtils.isPortrait(context)
}

/**
 * Get the current screen size in dp (Composable)
 * @return Pair(widthDp, heightDp)
 */
@Composable
fun getScreenSizeInDp(): Pair<Float, Float> {
    val context = LocalContext.current
    return DeviceUtils.getScreenSizeInDp(context)
}

/**
 * Get the current screen aspect ratio (Composable)
 */
@Composable
fun getScreenAspectRatio(): Float {
    val context = LocalContext.current
    return DeviceUtils.getScreenAspectRatio(context)
}

/**
 * Get the current device category (Composable)
 */
@Composable
fun getDeviceCategory(): String {
    val context = LocalContext.current
    return DeviceUtils.getDeviceCategory(context)
}

/**
 * Get the number of grid columns for current device (Composable)
 */
@Composable
fun getGridColumns(): Int {
    val resources = LocalContext.current.resources
    return DeviceUtils.getGridColumns(resources)
}

/**
 * Get the optimal item size for grid layout (Composable)
 * @return item size in dp
 */
@Composable
fun getOptimalItemSize(): Float {
    val context = LocalContext.current
    return DeviceUtils.getOptimalItemSize(context)
}