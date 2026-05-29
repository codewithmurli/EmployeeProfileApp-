package com.employeeapp.data.db

import androidx.room.RoomDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun createDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .build()
}
