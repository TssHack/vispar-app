package com.fazli.vispar.utils

import android.content.Context
import android.util.Log
import com.fazli.vispar.account.SyncManager
import com.fazli.vispar.data.model.FavoriteItem
import com.fazli.vispar.data.model.Movie
import com.fazli.vispar.data.model.Series
import com.fazli.vispar.data.model.SubtitleSettings
import com.fazli.vispar.data.model.VideoPlayerSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Date

object StorageUtils {
    private const val TAG = "StorageUtils"
    
    // Favorites: one JSON file, newest first. Every change is stamped and reported to the sync.
    private val favoritesJson = Json { ignoreUnknownKeys = true }

    fun saveFavorite(context: Context, favorite: FavoriteItem) {
        try {
            val stamped = favorite.copy(updatedAt = System.currentTimeMillis())
            val favorites = loadAllFavorites(context).filterNot { it.id == favorite.id && it.type == favorite.type }
            writeFavorites(context, listOf(stamped) + favorites)
            SyncManager.favoriteChanged(context, favorite.type, favorite.id, deleted = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving favorite", e)
        }
    }

    fun removeFavorite(context: Context, id: Int, type: String) {
        try {
            writeFavorites(context, loadAllFavorites(context).filterNot { it.id == id && it.type == type })
            SyncManager.favoriteChanged(context, type, id, deleted = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing favorite", e)
        }
    }

    fun clearAllFavorites(context: Context) {
        val all = loadAllFavorites(context)
        writeFavorites(context, emptyList())
        all.forEach { SyncManager.favoriteChanged(context, it.type, it.id, deleted = true) }
    }

    fun isFavorite(context: Context, id: Int, type: String): Boolean =
        loadAllFavorites(context).any { it.id == id && it.type == type }

    @Synchronized
    fun loadAllFavorites(context: Context): List<FavoriteItem> {
        return try {
            val file = File(context.filesDir, "favorites.json")
            if (file.exists()) favoritesJson.decodeFromString<List<FavoriteItem>>(file.readText()) else emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading all favorites", e)
            emptyList()
        }
    }

    /** Replaces the stored list as is (used by the sync, which already stamped the items). */
    @Synchronized
    fun writeFavorites(context: Context, favorites: List<FavoriteItem>) {
        try {
            File(context.filesDir, "favorites.json").writeText(favoritesJson.encodeToString(favorites))
        } catch (e: Exception) {
            Log.e(TAG, "Error writing favorites", e)
        }
    }

    // Function to save favorite to movie or series database when navigating to it
    fun saveFavoriteToDatabase(context: Context, favorite: FavoriteItem) {
        try {
            when (favorite.type) {
                "movie" -> {
                    // Convert FavoriteItem to Movie
                    val movie = Movie(
                        id = favorite.id,
                        type = favorite.type,
                        title = favorite.title,
                        description = favorite.description,
                        year = favorite.year,
                        imdb = favorite.imdb,
                        rating = favorite.rating,
                        duration = favorite.duration,
                        image = favorite.image,
                        cover = favorite.cover,
                        genres = favorite.genres,
                        sources = favorite.sources, // Include sources from favorites
                        country = favorite.country
                    )
                    saveMovieToFile(context, movie)
                }
                "series" -> {
                    // Convert FavoriteItem to Series
                    val series = Series(
                        id = favorite.id,
                        type = favorite.type,
                        title = favorite.title,
                        description = favorite.description,
                        year = favorite.year,
                        imdb = favorite.imdb,
                        rating = favorite.rating,
                        duration = favorite.duration,
                        image = favorite.image,
                        cover = favorite.cover,
                        genres = favorite.genres,
                        country = favorite.country
                    )
                    saveSeriesToFile(context, series)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving favorite to database", e)
        }
    }
    
    fun saveMovieToFile(context: Context, movie: Movie) {
        try {
            // Clear all existing movie files first
            clearAllMovies(context)
            
            val jsonString = Json.encodeToString(movie)
            val fileName = "movie_${movie.id}.json"
            val file = File(context.filesDir, fileName)
            file.writeText(jsonString)
            Log.d(TAG, "Movie saved to file: $fileName")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving movie to file", e)
        }
    }
    
    fun loadMovieFromFile(context: Context, movieId: Int): Movie? {
        return try {
            val fileName = "movie_$movieId.json"
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val jsonString = file.readText()
                Json.decodeFromString<Movie>(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading movie from file", e)
            null
        }
    }
    
    /**
     * Delete all movie files from storage
     */
    fun clearAllMovies(context: Context) {
        try {
            val filesDir = context.filesDir
            val movieFiles = filesDir.listFiles { file ->
                file.name.startsWith("movie_") && file.name.endsWith(".json")
            }
            
            movieFiles?.forEach { file ->
                file.delete()
                Log.d(TAG, "Deleted movie file: ${file.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all movies", e)
        }
    }
    
    // Series functions
    fun saveSeriesToFile(context: Context, series: Series) {
        try {
            // Clear all existing series files first
            clearAllSeries(context)
            
            val jsonString = Json.encodeToString(series)
            val fileName = "series_${series.id}.json"
            val file = File(context.filesDir, fileName)
            file.writeText(jsonString)
            Log.d(TAG, "Series saved to file: $fileName")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving series to file", e)
        }
    }
    
    fun loadSeriesFromFile(context: Context, seriesId: Int): Series? {
        return try {
            val fileName = "series_$seriesId.json"
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val jsonString = file.readText()
                Json.decodeFromString<Series>(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading series from file", e)
            null
        }
    }
    
    /**
     * Delete all series files from storage
     */
    fun clearAllSeries(context: Context) {
        try {
            val filesDir = context.filesDir
            val seriesFiles = filesDir.listFiles { file ->
                file.name.startsWith("series_") && file.name.endsWith(".json")
            }
            
            seriesFiles?.forEach { file ->
                file.delete()
                Log.d(TAG, "Deleted series file: ${file.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all series", e)
        }
    }
    
    // Subtitle settings functions
    fun saveSubtitleSettings(context: Context, settings: SubtitleSettings) {
        try {
            val jsonString = Json.encodeToString(settings)
            val file = File(context.filesDir, "subtitle_settings.json")
            file.writeText(jsonString)
            Log.d(TAG, "Subtitle settings saved to file")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving subtitle settings to file", e)
        }
    }
    
    fun loadSubtitleSettings(context: Context): SubtitleSettings {
        return try {
            val file = File(context.filesDir, "subtitle_settings.json")
            if (file.exists()) {
                val jsonString = file.readText()
                Json.decodeFromString<SubtitleSettings>(jsonString)
            } else {
                SubtitleSettings.DEFAULT
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading subtitle settings from file", e)
            SubtitleSettings.DEFAULT
        }
    }
    
    // Video player settings functions
    fun saveVideoPlayerSettings(context: Context, settings: VideoPlayerSettings) {
        try {
            val jsonString = Json.encodeToString(settings)
            val file = File(context.filesDir, "video_player_settings.json")
            file.writeText(jsonString)
            Log.d(TAG, "Video player settings saved to file")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving video player settings to file", e)
        }
    }
    
    fun loadVideoPlayerSettings(context: Context): VideoPlayerSettings {
        return try {
            val file = File(context.filesDir, "video_player_settings.json")
            if (file.exists()) {
                val jsonString = file.readText()
                Json.decodeFromString<VideoPlayerSettings>(jsonString)
            } else {
                VideoPlayerSettings.DEFAULT
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading video player settings from file", e)
            VideoPlayerSettings.DEFAULT
        }
    }
}