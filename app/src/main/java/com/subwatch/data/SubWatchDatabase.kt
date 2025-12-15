package com.subwatch.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Subscription::class],
    version = 1,
    exportSchema = false
)
abstract class SubWatchDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile private var INSTANCE: SubWatchDatabase? = null

        fun init(context: Context) {
            if (INSTANCE != null) return
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                SubWatchDatabase::class.java,
                "subwatch.db"
            ).build()
        }

        fun get(): SubWatchDatabase =
            INSTANCE ?: throw IllegalStateException("Database not initialized")
    }
}
