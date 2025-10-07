package com.fazli.vispar.data.repository

import com.fazli.vispar.data.model.Country
import com.fazli.vispar.data.model.Genre
import com.fazli.vispar.data.model.Series
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SeriesRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val BASE_URL = "https://hostinnegar.com/api/serie/by/filtres/0/created"
    private val API_KEY = "4F5A9C3D9A86FA54EACEDDD635185"
    
    suspend fun getSeries(page: Int = 0): List<Series> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL/$page/$API_KEY"
                val request = Request.Builder()
                    .url(url)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Failed to fetch series: ${response.code}")
                    }
                    
                    val jsonData = response.body?.string()
                        ?: throw Exception("Empty response body")
                    
                    parseSeries(jsonData)
                }
            } catch (e: Exception) {
                throw Exception("Error fetching series: ${e.message}")
            }
        }
    }
    
    private fun parseSeries(jsonData: String): List<Series> {
        val seriesList = mutableListOf<Series>()
        val jsonArray = JSONArray(jsonData)
        
        for (i in 0 until jsonArray.length()) {
            try {
                val seriesObj = jsonArray.getJSONObject(i)
                val series = parseSeriesItem(seriesObj)
                seriesList.add(series)
            } catch (e: Exception) {
                // Skip items that fail to parse
                continue
            }
        }
        
        return seriesList
    }
    
    private fun parseSeriesItem(seriesObj: JSONObject): Series {
        return Series(
            id = seriesObj.optInt("id", 0),
            type = seriesObj.optString("type", ""),
            title = seriesObj.optString("title", "Unknown Title"),
            description = seriesObj.optString("description", "No description available"),
            year = seriesObj.optInt("year", 0),
            imdb = seriesObj.optDouble("imdb", 0.0),
            rating = seriesObj.optDouble("rating", 0.0),
            duration = seriesObj.optString("duration", null).takeIf { it != "null" && it != "N/A" },
            image = seriesObj.optString("image", ""),
            cover = seriesObj.optString("cover", ""),
            genres = try {
                parseGenres(seriesObj.getJSONArray("genres"))
            } catch (e: Exception) {
                emptyList()
            },
            country = try {
                parseCountries(seriesObj.getJSONArray("country"))
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