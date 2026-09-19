package kaneplarium.lagerverwaltung.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class Article(
    @field:PrimaryKey val id: String,
    val shelfNumber: String,
    val compartmentNumber: String,
    val isLocked: Boolean = false
)
