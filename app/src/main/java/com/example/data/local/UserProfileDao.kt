package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE isGuest = 0 ORDER BY id ASC")
    fun getAllRegisteredUsers(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveUser(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveUserSync(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE isGuest = 1 LIMIT 1")
    suspend fun getGuestUser(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfileEntity): Long

    @Update
    suspend fun updateUser(user: UserProfileEntity)

    @Delete
    suspend fun deleteUser(user: UserProfileEntity)

    @Query("UPDATE user_profiles SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE user_profiles SET isActive = 1 WHERE id = :userId")
    suspend fun setActiveUser(userId: Long)

    @Query("SELECT COUNT(*) FROM user_profiles WHERE isGuest = 0")
    suspend fun getRegisteredUserCount(): Int

    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM user_profiles WHERE isGuest = 0 ORDER BY id ASC LIMIT 1")
    suspend fun getFirstUser(): UserProfileEntity?

    @Query("DELETE FROM user_profiles WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)
}

