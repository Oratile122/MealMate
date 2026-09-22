package com.example.mealmate.database

// AppDatabase - the main Room database for MealMate
// Defines all tables (entities) and provides DAOs to access them
// Uses the singleton pattern so only one instance exists app-wide

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// @Database annotation lists all tables and sets the schema version
// Increment "version" whenever you change any entity
@Database(
    entities = [
        UserEntity::class,           // Users table
        MealEntity::class,           // Meals table
        ShoppingItemEntity::class    // Shopping items table
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Abstract DAOs - Room generates the implementations automatically
    abstract fun userDao(): UserDao
    abstract fun mealDao(): MealDao
    abstract fun shoppingItemDao(): ShoppingItemDao

    // Companion object holds the singleton instance
    companion object {

        // Volatile ensures the instance is always up-to-date across threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Returns the existing database instance, or creates one if it doesn't exist
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mealmate_database"        // Name of the local SQLite file
                )
                    .fallbackToDestructiveMigration()  // Drops and recreates tables if schema changes
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}