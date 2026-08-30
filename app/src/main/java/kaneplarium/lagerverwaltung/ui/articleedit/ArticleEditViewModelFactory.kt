package kaneplarium.lagerverwaltung.ui.articleedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kaneplarium.lagerverwaltung.data.ArticleDao

class ArticleEditViewModelFactory(
    private val articleDao: ArticleDao,
    private val articleId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticleEditViewModel(articleDao, articleId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
