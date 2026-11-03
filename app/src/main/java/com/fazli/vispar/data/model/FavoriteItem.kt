package com.fazli.vispar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteItem(
    val id: Int,
    val type: String, // "movie" or "series"
    val title: String,
    val description: String,
    val year: Int,
    val imdb: Double,
    val rating: Double,
    val duration: String?,
    val image: String,
    val cover: String,
    val genres: List<Genre>,
    val country: List<Country>,
    val sources: List<Source> = emptyList(), // Add sources for movies
    /** When this favourite was added (ms); drives the sync between devices. */
    val updatedAt: Long = 0,
)