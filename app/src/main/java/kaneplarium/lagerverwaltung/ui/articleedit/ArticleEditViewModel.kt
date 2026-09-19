package kaneplarium.lagerverwaltung.ui.articleedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kaneplarium.lagerverwaltung.data.Article
import kaneplarium.lagerverwaltung.data.ArticleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArticleEditViewModel(
    private val articleDao: ArticleDao,
    articleId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleEditUiState())
    val uiState: StateFlow<ArticleEditUiState> = _uiState.asStateFlow()

    init {
        loadArticle(articleId)
    }

    fun loadArticle(id: String?) {
        // Reset state to empty before loading
        _uiState.value = ArticleEditUiState(id = id ?: "")
        
        if (id != null) {
            viewModelScope.launch {
                try {
                    val article = withContext(Dispatchers.IO) {
                        articleDao.getArticleById(id)
                    }
                    if (article != null) {
                        _uiState.value = ArticleEditUiState(
                            id = article.id ?: "",
                            shelfNumber = article.shelfNumber ?: "",
                            compartmentNumber = article.compartmentNumber ?: "",
                            isLocked = article.isLocked,
                            isEditing = true
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(idError = "Error loading article: ${e.message}")
                }
            }
        }
    }

    fun onIdChange(newId: String) {
        _uiState.value = _uiState.value.copy(id = newId, idError = null)
    }

    fun onShelfNumberChange(newShelf: String) {
        _uiState.value = _uiState.value.copy(shelfNumber = newShelf)
    }

    fun onCompartmentNumberChange(newCompartment: String) {
        _uiState.value = _uiState.value.copy(compartmentNumber = newCompartment)
    }

    fun saveArticle(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (!validateId(currentState.id)) {
            _uiState.value = currentState.copy(idError = "Kisten-ID muss aus 6-7 Ziffern bestehen")
            return
        }

        if (currentState.shelfNumber.isBlank() || !currentState.shelfNumber.all { it.isDigit() }) {
            _uiState.value = currentState.copy(idError = "Regal muss eine Nummer sein")
            return
        }

        if (currentState.compartmentNumber.isBlank() || !currentState.compartmentNumber.all { it.isDigit() }) {
            _uiState.value = currentState.copy(idError = "Platznummer muss eine Nummer sein")
            return
        }

        if (currentState.isSaving) return

        _uiState.value = currentState.copy(isSaving = true)

        viewModelScope.launch {
            try {
                // Safety check: if adding a new article, ensure ID doesn't already exist
                if (!currentState.isEditing) {
                    val existing = withContext(Dispatchers.IO) {
                        articleDao.getArticleById(currentState.id)
                    }
                    if (existing != null) {
                        _uiState.value = currentState.copy(
                            idError = "Article with this ID already exists",
                            isSaving = false
                        )
                        return@launch
                    }
                }

                withContext(Dispatchers.IO) {
                    articleDao.insertArticle(
                        Article(
                            id = currentState.id,
                            shelfNumber = currentState.shelfNumber,
                            compartmentNumber = currentState.compartmentNumber,
                            isLocked = currentState.isLocked
                        )
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    idError = "Error saving article: ${e.message}",
                    isSaving = false
                )
            }
        }
    }

    private fun validateId(id: String): Boolean {
        return id.length in 6..7 && id.all { it.isDigit() }
    }
}

data class ArticleEditUiState(
    val id: String = "",
    val shelfNumber: String = "",
    val compartmentNumber: String = "",
    val idError: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isLocked: Boolean = false
)
