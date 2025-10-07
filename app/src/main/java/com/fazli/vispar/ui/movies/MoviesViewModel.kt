package com.fazli.vispar.ui.movies

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazli.vispar.data.model.Movie
import com.fazli.vispar.data.repository.MovieRepository
import com.fazli.vispar.utils.LanguageUtils
import kotlinx.coroutines.launch

class MoviesViewModel : ViewModel() {
    private val repository = MovieRepository()
    
    var movies by mutableStateOf<List<Movie>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var isLoadingMore by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var currentPage by mutableStateOf(0)
        private set
    
    var canLoadMore by mutableStateOf(true)
        private set
    
    init {
        loadMovies()
    }
    
    fun loadMovies(page: Int = 0) {
        viewModelScope.launch {
            try {
                if (page == 0) {
                    isLoading = true
                } else {
                    isLoadingMore = true
                }
                errorMessage = null
                
                val newMovies = repository.getMovies(page)
                
                // Filter out movies with Farsi titles
                val filteredMovies = newMovies.filter { movie ->
                    LanguageUtils.shouldDisplayTitle(movie.title)
                }
                
                // If we get fewer movies than expected, we've reached the end
                canLoadMore = filteredMovies.isNotEmpty()
                
                if (page == 0) {
                    movies = filteredMovies
                } else {
                    movies = movies + filteredMovies
                }
                
                currentPage = page
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
                isLoadingMore = false
            }
        }
    }
    
    fun loadMoreMovies() {
        if (!isLoading && !isLoadingMore && canLoadMore) {
            loadMovies(currentPage + 1)
        }
    }
    
    fun retry() {
        loadMovies(currentPage)
    }
    
    fun refresh() {
        loadMovies(0)
    }
}