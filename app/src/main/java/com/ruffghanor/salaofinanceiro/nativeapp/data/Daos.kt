package com.ruffghanor.salaofinanceiro.nativeapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1")
    fun observe(): Flow<AppConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: AppConfigEntity)
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ServiceEntity)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM services")
    suspend fun clear()
}

@Dao
interface CollaboratorDao {
    @Query("SELECT * FROM collaborators ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<CollaboratorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CollaboratorEntity)

    @Query("DELETE FROM collaborators WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM collaborators")
    suspend fun clear()
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendances ORDER BY date DESC")
    fun observeAll(): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AttendanceEntity)

    @Query("DELETE FROM attendances WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM attendances")
    suspend fun clear()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM expenses")
    suspend fun clear()
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY date DESC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM products")
    suspend fun clear()
}
