package kaneplarium.lagerverwaltung.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kaneplarium.lagerverwaltung.data.Article
import kaneplarium.lagerverwaltung.data.ArticleDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.Scanner


@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val articleDao: ArticleDao
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val articles: StateFlow<List<Article>> = searchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                articleDao.getAllArticles()
            } else {
                articleDao.searchArticles(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun deleteArticle(articleId: String) {
        viewModelScope.launch {
            articleDao.deleteArticleById(articleId)
        }
    }

    fun toggleLockStatus(article: Article) {
        viewModelScope.launch {
            articleDao.updateArticleLockStatus(article.id, !article.isLocked)
        }
    }

    fun exportArticlesToCsv(outputStream: OutputStream) {
        viewModelScope.launch {
            val allArticles = articleDao.getAllArticles().first()
            withContext(Dispatchers.IO) {
                outputStream.use { stream ->
                    val writer = OutputStreamWriter(stream, "UTF-8")
                    // Header: Excel works best with CSV if semicolon is used and encoding is BOM UTF-8 or just UTF-8
                    // We use semicolon as it is common in German Excel
                    writer.write("Kisten-ID;Regal;Platznummer;Gesperrt\n")
                    allArticles.forEach { article ->
                        writer.write("${article.id};${article.shelfNumber};${article.compartmentNumber};${article.isLocked}\n")
                    }
                    writer.flush()
                }
            }
        }
    }

    fun importArticlesFromCsv(inputStream: InputStream) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                inputStream.use { stream ->
                    val scanner = Scanner(stream, "UTF-8")
                    if (scanner.hasNextLine()) scanner.nextLine() // Skip header
                    while (scanner.hasNextLine()) {
                        val line = scanner.nextLine()
                        val parts = line.split(";")
                        if (parts.size >= 3) {
                            val id = parts[0]
                            val shelf = parts[1]
                            val compartment = parts[2]
                            val isLocked = parts.getOrNull(3)?.toBoolean() ?: false
                            articleDao.insertArticle(
                                Article(id, shelf, compartment, isLocked)
                            )
                        }
                    }
                }
            }
        }
    }
}
