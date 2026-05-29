package com.employeeapp.di

import com.employeeapp.data.db.AppDatabase
import com.employeeapp.data.db.createDatabase
import com.employeeapp.data.db.getDatabaseBuilder
import org.koin.dsl.module

val androidModule = module {
    single<AppDatabase> {
        createDatabase(getDatabaseBuilder())
    }
}
