package com.ruffghanor.salaofinanceiro.nativeapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppConfigEntity::class,
        ServiceEntity::class,
        CollaboratorEntity::class,
        AttendanceEntity::class,
        ExpenseEntity::class,
        ProductEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao
    abstract fun serviceDao(): ServiceDao
    abstract fun collaboratorDao(): CollaboratorDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "salao_financeiro_native.db"
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
