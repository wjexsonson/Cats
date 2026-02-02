package com.example.cats.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cats.data.model.Cat
import com.example.cats.data.repository.CatRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: CatRepository
) : ViewModel() {

    private val _cats = MutableLiveData<List<Cat>>(emptyList())
    val cats: LiveData<List<Cat>> = _cats

    private val _favoriteCats = MutableLiveData<List<Cat>>(emptyList())
    val favoriteCats: LiveData<List<Cat>> = _favoriteCats

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorText = MutableLiveData<String?>(null)
    val errorText: LiveData<String?> = _errorText

    private var currentPage = 0
    private val loadMutex = Mutex()

    init {
        subscribeToFavoriteChanges()
        loadFavoriteCats()
    }

    private fun subscribeToFavoriteChanges() {
        viewModelScope.launch {
            try {
                repository.getFavoriteIds().collectLatest { favoriteIds ->
                    val currentList = _cats.value.orEmpty()
                    if (currentList.isEmpty()) return@collectLatest

                    val newList = currentList.map { cat ->
                        cat.copy(isFavorite = favoriteIds.contains(cat.id))
                    }
                    _cats.postValue(newList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadFavoriteCats() {
        viewModelScope.launch {
            try {
                repository.getFavoriteCats().collectLatest { cats ->
                    _favoriteCats.postValue(cats)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addToFavorites(cat: Cat) {
        viewModelScope.launch {
            try {
                repository.addToFavorites(cat)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorText.value = "Не удалось добавить в избранное"
            }
        }
    }

    fun removeFromFavorites(id: String) {
        viewModelScope.launch {
            try {
                repository.removeFromFavorites(id)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorText.value = "Ошибка удаления"
            }
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            try {
                repository.clearAllFavorites()
            } catch (e: Exception) {
                e.printStackTrace()
                _errorText.value = "Не удалось очистить избранное"
            }
        }
    }

    fun loadMoreCats() {
        viewModelScope.launch {
            if (!loadMutex.tryLock()) return@launch

            _isLoading.value = true
            _errorText.value = null

            try {
                val newCats = repository.getCats(page = currentPage)
                val merged = _cats.value.orEmpty() + newCats
                _cats.value = merged
                currentPage++
            } catch (e: Exception) {
                e.printStackTrace()
                _errorText.value = "Ошибка сети: проверьте интернет"
            } finally {
                _isLoading.value = false
                loadMutex.unlock()
            }
        }
    }
}
