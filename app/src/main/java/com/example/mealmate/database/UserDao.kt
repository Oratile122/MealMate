package com.example.mealmate.database

// UserDao - Data Access Object for the "users" table
// Room generates the implementation of this interface automatically
// Handles all user operations: insert, update, find, and logout

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    // Inserts a new user into the database
    // REPLACE means if a user with the same email exists, it gets overwritten
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Updates an existing user (all fields)
    // Used when saving settings or updating the isLoggedIn flag
    @Update
    suspend fun updateUser(user: UserEntity)

    // Finds a user by email address
    // Returns the user, or null if not found
    // Used during login and to load the user's profile
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    // Returns the currently logged-in user (if any)
    // Only one user can be logged in at a time (isLoggedIn = 1)
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?

    // Marks ALL users as logged out
    // Called before logging in a new user, or when the user taps Logout
    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    // Checks if a user with the given email already exists
    // Returns the count (0 = doesn't exist, 1+ = exists)
    // Used in RegisterActivity to prevent duplicate emails
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun userExists(email: String): Int
}