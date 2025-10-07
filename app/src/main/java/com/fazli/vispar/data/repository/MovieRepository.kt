package com.fazli.vispar.data.repository

import com.fazli.vispar.data.model.Country
import com.fazli.vispar.data.model.Genre
import com.fazli.vispar.data.model.Movie
import com.fazli.vispar.data.model.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MovieRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val BASE_URL = "https://hostinnegar.com/api/movie/by/filtres/0/created"
    private val API_KEY = "4F5A9C3D9A86FA54EACEDDD635185"
    
    suspend fun getMovies(page: Int = 0): List<Movie> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL/$page/$API_KEY"
                val request = Request.Builder()
                    .url(url)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Failed to fetch movies: ${response.code}")
                    }
                    
                    val jsonData = response.body?.string()
                        ?: throw Exception("Empty response body")
                    
                    parseMovies(jsonData)
                }
            } catch (e: Exception) {
                throw Exception("Error fetching movies: ${e.message}")
            }
        }
    }
    
    private fun parseMovies(jsonData: String): List<Movie> {
        val movies = mutableListOf<Movie>()
        val jsonArray = JSONArray(jsonData)
        
        for (i in 0 until jsonArray.length()) {
            try {
                val movieObj = jsonArray.getJSONObject(i)
                val movie = parseMovie(movieObj)
                movies.add(movie)
            } catch (e: Exception) {
                // Skip items that fail to parse
                continue
            }
        }
        
        return movies
    }
    
    private fun parseMovie(movieObj: JSONObject): Movie {
        return Movie(
            id = movieObj.optInt("id", 0),
            type = movieObj.optString("type", ""),
            title = movieObj.optString("title", "Unknown Title"),
            description = movieObj.optString("description", "No description available"),
            year = movieObj.optInt("year", 0),
            imdb = movieObj.optDouble("imdb", 0.0),
            rating = movieObj.optDouble("rating", 0.0),
            duration = movieObj.optString("duration", null).takeIf { it != "null" && it != "N/A" },
            image = movieObj.optString("image", ""),
            cover = movieObj.optString("cover", ""),
            genres = try {
                parseGenres(movieObj.getJSONArray("genres"))
            } catch (e: Exception) {
                emptyList()
            },
            sources = try {
                parseSources(movieObj.getJSONArray("sources"))
            } catch (e: Exception) {
                emptyList()
            },
            country = try {
                parseCountries(movieObj.getJSONArray("country"))
            } catch (e: Exception) {
                emptyList()
            }
        )
    }
    
    private fun parseGenres(genresArray: JSONArray): List<Genre> {
        val genres = mutableListOf<Genre>()
        for (i in 0 until genresArray.length()) {
            try {
                val genreObj = genresArray.getJSONObject(i)
                genres.add(
                    Genre(
                        id = genreObj.optInt("id", 0),
                        title = genreObj.optString("title", "Unknown")
                    )
                )
            } catch (e: Exception) {
                // Skip genres that fail to parse
                continue
            }
        }
        return genres
    }
    
    private fun parseSources(sourcesArray: JSONArray): List<Source> {
        val sources = mutableListOf<Source>()
        for (i in 0 until sourcesArray.length()) {
            try {
                val sourceObj = sourcesArray.getJSONObject(i)
                sources.add(
                    Source(
                        id = sourceObj.optInt("id", 0),
                        quality = sourceObj.optString("quality", "Unknown"),
                        type = sourceObj.optString("type", "Unknown"),
                        url = sourceObj.optString("url", "")
                    )
                )
            } catch (e: Exception) {
                // Skip sources that fail to parse
                continue
            }
        }
        return sources
    }
    
    private fun parseCountries(countriesArray: JSONArray): List<Country> {
        val countries = mutableListOf<Country>()
        for (i in 0 until countriesArray.length()) {
            try {
                val countryObj = countriesArray.getJSONObject(i)
                countries.add(
                    Country(
                        id = countryObj.optInt("id", 0),
                        title = countryObj.optString("title", "Unknown"),
                        image = countryObj.optString("image", "")
                    )
                )
            } catch (e: Exception) {
                // Skip countries that fail to parse
                continue
            }
        }
        return countries
    }
}