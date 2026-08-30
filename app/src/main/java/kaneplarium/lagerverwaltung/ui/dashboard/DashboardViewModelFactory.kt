package kaneplarium.lagerverwaltung.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kaneplarium.lagerverwaltung.data.ArticleDao

class DashboardViewModelFactory(
    private val articleDao: ArticleDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(articleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
