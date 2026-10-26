package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val idKey: String, // e.g. "QURAN_2_255" or "HADITH_101"
    val itemType: String, // "QURAN" or "HADITH"
    val title: String,
    val subtitle: String,
    val contentUthmani: String,
    val secondaryInfo: String,
    val surahNumber: Int = 0,
    val ayahNumber: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_notes")
data class CalendarNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gregorianDateKey: String, // YYYY-MM-DD
    val hijriDateDisplay: String,
    val title: String,
    val noteDetails: String,
    val category: String, // "عبادة", "صيام", "ورد قرآني", "تذكير عام"
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface IslamicDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE idKey = :idKey")
    suspend fun deleteBookmarkByKey(idKey: String)

    @Query("SELECT * FROM calendar_notes ORDER BY gregorianDateKey ASC, timestamp DESC")
    fun getAllCalendarNotes(): Flow<List<CalendarNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarNote(note: CalendarNoteEntity)

    @Query("DELETE FROM calendar_notes WHERE id = :noteId")
    suspend fun deleteCalendarNote(noteId: Int)
}

@Database(
    entities = [BookmarkEntity::class, CalendarNoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun islamicDao(): IslamicDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "almishkat_islamic_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
