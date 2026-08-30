package kaneplarium.lagerverwaltung

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kaneplarium.lagerverwaltung.data.AppDatabase
import kaneplarium.lagerverwaltung.data.ArticleDao
import kaneplarium.lagerverwaltung.ui.AppNavKey
import kaneplarium.lagerverwaltung.ui.articleedit.ArticleEditScreen
import kaneplarium.lagerverwaltung.ui.articleedit.ArticleEditViewModel
import kaneplarium.lagerverwaltung.ui.articleedit.ArticleEditViewModelFactory
import kaneplarium.lagerverwaltung.ui.dashboard.DashboardScreen
import kaneplarium.lagerverwaltung.ui.dashboard.DashboardViewModel
import kaneplarium.lagerverwaltung.ui.dashboard.DashboardViewModelFactory
import kaneplarium.lagerverwaltung.ui.theme.LagerverwaltungTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(this)
        val articleDao = database.articleDao()

        setContent {
            LagerverwaltungTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LagerverwaltungApp(articleDao = articleDao)
                }
            }
        }
    }
}

@Composable
fun LagerverwaltungApp(articleDao: ArticleDao) {
    val backStack = rememberNavBackStack(AppNavKey.Dashboard)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is AppNavKey.Dashboard -> {
                    NavEntry(key) {
                        val viewModel: DashboardViewModel = viewModel(
                            factory = DashboardViewModelFactory(articleDao)
                        )
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToEdit = { articleId ->
                                backStack.add(AppNavKey.ArticleEdit(articleId))
                            }
                        )
                    }
                }

                is AppNavKey.ArticleDetail -> {
                    NavEntry(key) {
                        Text(text = "Article Detail for ${key.articleId}")
                    }
                }

                is AppNavKey.ArticleEdit -> {
                    NavEntry(key) {
                        val viewModel: ArticleEditViewModel = viewModel(
                            factory = ArticleEditViewModelFactory(articleDao, key.articleId)
                        )
                        ArticleEditScreen(
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeLastOrNull() }
                        )
                    }
                }

                else -> NavEntry(key) { Text("Unknown") }
            }
        }
    )
}



