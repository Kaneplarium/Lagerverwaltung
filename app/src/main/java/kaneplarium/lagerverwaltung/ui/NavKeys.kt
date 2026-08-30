package kaneplarium.lagerverwaltung.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppNavKey : NavKey {
    @Serializable
    data object Dashboard : AppNavKey

    @Serializable
    data class ArticleDetail(val articleId: String) : AppNavKey

    @Serializable
    data class ArticleEdit(val articleId: String? = null) : AppNavKey
}



