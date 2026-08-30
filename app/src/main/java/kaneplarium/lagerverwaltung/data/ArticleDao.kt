package kaneplarium.lagerverwaltung.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :articleId")
    suspend fun getArticleById(articleId: String): Article?

    @Query("SELECT * FROM articles WHERE id = :articleId")
    fun getArticleByIdFlow(articleId: String): Flow<List<Article>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article)

    @Query("DELETE FROM articles WHERE id = :articleId")
    suspend fun deleteArticleById(articleId: String)

    @Query("UPDATE articles SET isLocked = :isLocked WHERE id = :articleId")
    suspend fun updateArticleLockStatus(articleId: String, isLocked: Boolean)

    @Query("SELECT * FROM articles WHERE id LIKE '%' || :searchQuery || '%' OR shelfNumber LIKE '%' || :searchQuery || '%' OR compartmentNumber LIKE '%' || :searchQuery || '%'")
    fun searchArticles(searchQuery: String): Flow<List<Article>>
}
