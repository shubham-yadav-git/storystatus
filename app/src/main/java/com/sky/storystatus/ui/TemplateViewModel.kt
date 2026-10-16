package com.sky.storystatus.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sky.storystatus.data.FavoritesRepository
import com.sky.storystatus.data.TemplateRepository
import com.sky.storystatus.export.DefaultStatusImageRenderer
import com.sky.storystatus.export.ShareManager
import com.sky.storystatus.model.StatusTemplate
import com.sky.storystatus.model.TemplateBackground
import com.sky.storystatus.model.TemplateCustomization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplateViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TemplateRepository(application)
    private val favoritesRepository = FavoritesRepository(application)
    private val renderer = DefaultStatusImageRenderer(application)
    private val shareManager = ShareManager(application)

    private val _templates = MutableStateFlow<List<StatusTemplate>>(emptyList())
    val templates: StateFlow<List<StatusTemplate>> = _templates.asStateFlow()

    private val _featuredTemplates = MutableStateFlow<List<StatusTemplate>>(emptyList())
    val featuredTemplates: StateFlow<List<StatusTemplate>> = _featuredTemplates.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(repository.getCategories())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _languages = MutableStateFlow<List<String>>(repository.getLanguages())
    val languages: StateFlow<List<String>> = _languages.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(repository.getTags())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("All")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedTag = MutableStateFlow("All")
    val selectedTag: StateFlow<String> = _selectedTag.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val favorites: StateFlow<Set<String>> = favoritesRepository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // Filtered templates derived reactively from search query, category, language, and tag
    val filteredTemplates: StateFlow<List<StatusTemplate>> = combine(
        _templates,
        _searchQuery,
        _selectedCategory,
        _selectedLanguage,
        _selectedTag
    ) { _, query, category, language, tag ->
        repository.searchTemplates(query, category, language, tag)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Editor state
    private val _currentCustomization = MutableStateFlow(TemplateCustomization())
    val currentCustomization: StateFlow<TemplateCustomization> = _currentCustomization.asStateFlow()

    init {
        loadTemplates()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val loaded = repository.getAllTemplates()
                _templates.value = loaded
                _featuredTemplates.value = repository.getFeaturedTemplates()
                if (loaded.isEmpty()) {
                    _errorMessage.value = "No templates found in catalog."
                } else {
                    prewarmThumbnails(loaded.take(24))
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load catalog: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun prewarmThumbnails(templatesToWarm: List<StatusTemplate>) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            templatesToWarm.forEach { template ->
                val cacheKey = "${template.id}_v${template.version}"
                if (com.sky.storystatus.export.ThumbnailCache.get(cacheKey) == null) {
                    try {
                        val rendered = renderer.render(template, TemplateCustomization(), 270, 480)
                        com.sky.storystatus.export.ThumbnailCache.put(cacheKey, rendered)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedLanguage(language: String) {
        _selectedLanguage.value = language
    }

    fun setSelectedTag(tag: String) {
        _selectedTag.value = tag
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = "All"
        _selectedLanguage.value = "All"
        _selectedTag.value = "All"
    }

    fun getTemplateById(id: String): StatusTemplate? {
        return _templates.value.find { it.id == id }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(id)
        }
    }

    fun updateText(layerId: String, newText: String) {
        val currentMap = _currentCustomization.value.textModifications.toMutableMap()
        currentMap[layerId] = newText
        _currentCustomization.value = _currentCustomization.value.copy(textModifications = currentMap)
    }

    fun updateColor(layerId: String, newColor: Long) {
        val currentMap = _currentCustomization.value.colorModifications.toMutableMap()
        currentMap[layerId] = newColor
        _currentCustomization.value = _currentCustomization.value.copy(colorModifications = currentMap)
    }

    fun updateBackground(newBackground: TemplateBackground) {
        _currentCustomization.value = _currentCustomization.value.copy(backgroundModification = newBackground)
    }

    fun updateFont(layerId: String, fontFamily: String) {
        val currentMap = _currentCustomization.value.fontModifications.toMutableMap()
        currentMap[layerId] = fontFamily
        _currentCustomization.value = _currentCustomization.value.copy(fontModifications = currentMap)
    }

    fun resetCustomization() {
        _currentCustomization.value = TemplateCustomization()
    }

    suspend fun renderTemplate(templateId: String): Bitmap? {
        val template = getTemplateById(templateId) ?: return null
        return renderer.render(template, _currentCustomization.value)
    }

    suspend fun saveToGallery(templateId: String): Boolean {
        val bitmap = renderTemplate(templateId) ?: return false
        val template = getTemplateById(templateId) ?: return false
        val result = shareManager.saveToMediaStore(bitmap, template.title)
        return result.isSuccess
    }

    suspend fun shareStatus(templateId: String): Boolean {
        val bitmap = renderTemplate(templateId) ?: return false
        val result = shareManager.shareImage(bitmap)
        return result.isSuccess
    }
}
