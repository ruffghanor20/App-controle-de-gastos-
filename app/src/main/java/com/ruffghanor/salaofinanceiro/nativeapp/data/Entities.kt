package com.ruffghanor.salaofinanceiro.nativeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val salonName: String = "Salão Financeiro",
    val themeColorHex: String = "#6D28D9",
    val username: String = "admin",
    val password: String = "admin123",
    val pinHash: String = "",
    val logoUri: String = "",
    val createdAt: String = nowIso(),
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey val id: String = newId(),
    val name: String,
    val category: String = "",
    val price: Double,
    val commissionPercent: Double,
    val notes: String = "",
)

@Entity(tableName = "collaborators")
data class CollaboratorEntity(
    @PrimaryKey val id: String = newId(),
    val name: String,
    val role: String,
    val defaultCommission: Double,
    val contact: String = "",
    val status: String = "ativo",
)

@Entity(tableName = "attendances")
data class AttendanceEntity(
    @PrimaryKey val id: String = newId(),
    val date: String,
    val client: String = "",
    val serviceId: String,
    val collaboratorId: String,
    val amount: Double,
    val paymentMethod: String = "PIX",
    val commission: Double,
    val salonValue: Double,
    val notes: String = "",
    val attachmentUri: String = "",
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String = newId(),
    val date: String,
    val category: String,
    val description: String,
    val amount: Double,
    val paymentMethod: String = "PIX",
    val vendor: String = "",
    val attachmentUri: String = "",
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String = newId(),
    val date: String,
    val name: String,
    val category: String = "",
    val quantity: Int,
    val unitCost: Double,
    val totalCost: Double,
    val vendor: String = "",
    val notes: String = "",
)

fun newId(): String = "${System.currentTimeMillis()}_${(1000..9999).random()}"

fun todayIso(): String = java.time.LocalDate.now().toString()
fun currentMonthIso(): String = java.time.YearMonth.now().toString()
fun nowIso(): String = java.time.OffsetDateTime.now().toString()
