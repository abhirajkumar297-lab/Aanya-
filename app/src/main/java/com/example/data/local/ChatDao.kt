package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
  @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
  fun observeSessions(): Flow<List<ChatSessionEntity>>

  @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC LIMIT 1")
  suspend fun getMostRecentSession(): ChatSessionEntity?

  @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC, id ASC")
  fun observeMessages(sessionId: Long): Flow<List<ChatMessageEntity>>

  @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC, id ASC")
  suspend fun getMessagesForSession(sessionId: Long): List<ChatMessageEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: ChatSessionEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessageEntity): Long

  @Query("UPDATE chat_sessions SET title = :title, updatedAt = :updatedAt WHERE id = :sessionId")
  suspend fun updateSessionTitleAndTime(sessionId: Long, title: String, updatedAt: Long)

  @Query("UPDATE chat_sessions SET updatedAt = :updatedAt WHERE id = :sessionId")
  suspend fun updateSessionTime(sessionId: Long, updatedAt: Long)

  @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
  suspend fun deleteSession(sessionId: Long)

  @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
  suspend fun deleteMessagesForSession(sessionId: Long)

  @Query("DELETE FROM chat_messages WHERE id = :messageId")
  suspend fun deleteMessageById(messageId: Long)
}
