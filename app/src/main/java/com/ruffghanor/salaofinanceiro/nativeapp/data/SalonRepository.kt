package com.ruffghanor.salaofinanceiro.nativeapp.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class SalonRepository(private val db: AppDatabase) {
    val config: Flow<AppConfigEntity> = db.configDao().observe().map { it ?: AppConfigEntity() }
    val services: Flow<List<ServiceEntity>> = db.serviceDao().observeAll()
    val collaborators: Flow<List<CollaboratorEntity>> = db.collaboratorDao().observeAll()
    val attendances: Flow<List<AttendanceEntity>> = db.attendanceDao().observeAll()
    val expenses: Flow<List<ExpenseEntity>> = db.expenseDao().observeAll()
    val products: Flow<List<ProductEntity>> = db.productDao().observeAll()

    suspend fun ensureSeeded() {
        val existing = db.configDao().observe().first()
        if (existing == null) {
            db.configDao().upsert(AppConfigEntity())
        }
    }

    suspend fun upsertConfig(config: AppConfigEntity) = db.configDao().upsert(config)
    suspend fun upsertService(item: ServiceEntity) = db.serviceDao().upsert(item)
    suspend fun deleteService(id: String) = db.serviceDao().delete(id)

    suspend fun upsertCollaborator(item: CollaboratorEntity) = db.collaboratorDao().upsert(item)
    suspend fun deleteCollaborator(id: String) = db.collaboratorDao().delete(id)

    suspend fun upsertAttendance(item: AttendanceEntity) = db.attendanceDao().upsert(item)
    suspend fun deleteAttendance(id: String) = db.attendanceDao().delete(id)

    suspend fun upsertExpense(item: ExpenseEntity) = db.expenseDao().upsert(item)
    suspend fun deleteExpense(id: String) = db.expenseDao().delete(id)

    suspend fun upsertProduct(item: ProductEntity) = db.productDao().upsert(item)
    suspend fun deleteProduct(id: String) = db.productDao().delete(id)

    suspend fun seedDemoData() {
        val config = AppConfigEntity()
        db.configDao().upsert(config)
        db.serviceDao().clear()
        db.collaboratorDao().clear()
        db.attendanceDao().clear()
        db.expenseDao().clear()
        db.productDao().clear()

        val s1 = ServiceEntity(name = "Corte feminino", category = "Cabelo", price = 75.0, commissionPercent = 40.0)
        val s2 = ServiceEntity(name = "Escova", category = "Cabelo", price = 55.0, commissionPercent = 35.0)
        val s3 = ServiceEntity(name = "Pé e mão", category = "Unhas", price = 50.0, commissionPercent = 45.0)
        listOf(s1, s2, s3).forEach { db.serviceDao().upsert(it) }

        val c1 = CollaboratorEntity(name = "Camila", role = "Cabeleireira", defaultCommission = 40.0)
        val c2 = CollaboratorEntity(name = "Rafaela", role = "Manicure", defaultCommission = 45.0)
        listOf(c1, c2).forEach { db.collaboratorDao().upsert(it) }

        val month = currentMonthIso()
        db.attendanceDao().upsert(
            AttendanceEntity(
                date = "$month-05",
                client = "Ana",
                serviceId = s1.id,
                collaboratorId = c1.id,
                amount = 75.0,
                paymentMethod = "PIX",
                commission = 30.0,
                salonValue = 45.0,
            )
        )
        db.attendanceDao().upsert(
            AttendanceEntity(
                date = "$month-06",
                client = "Bianca",
                serviceId = s3.id,
                collaboratorId = c2.id,
                amount = 50.0,
                paymentMethod = "Dinheiro",
                commission = 22.5,
                salonValue = 27.5,
            )
        )
        db.expenseDao().upsert(
            ExpenseEntity(
                date = "$month-03",
                category = "Aluguel",
                description = "Aluguel do espaço",
                amount = 900.0,
                paymentMethod = "PIX",
                vendor = "Imobiliária",
            )
        )
        db.productDao().upsert(
            ProductEntity(
                date = "$month-02",
                name = "Tintura",
                category = "Cabelo",
                quantity = 4,
                unitCost = 28.0,
                totalCost = 112.0,
                vendor = "Distribuidora",
            )
        )
    }

    suspend fun exportBackupJson(context: Context): Result<Uri> = runCatching {
        val payload = JSONObject().apply {
            put("meta", config.firstSync().toJson())
            put("services", JSONArray(services.firstSync().map { it.toJson() }))
            put("collaborators", JSONArray(collaborators.firstSync().map { it.toJson() }))
            put("attendances", JSONArray(attendances.firstSync().map { it.toJson() }))
            put("expenses", JSONArray(expenses.firstSync().map { it.toJson() }))
            put("products", JSONArray(products.firstSync().map { it.toJson() }))
        }.toString(2)
        saveTextToDownloads(
            context = context,
            fileName = "backup_salao_financeiro_${System.currentTimeMillis()}.json",
            mimeType = "application/json",
            text = payload,
        )
    }

    suspend fun exportMonthlyCsv(context: Context, yearMonth: String): Result<Uri> = runCatching {
        val csv = buildCsvForMonth(
            yearMonth = yearMonth,
            services = services.firstSync(),
            collaborators = collaborators.firstSync(),
            attendances = attendances.firstSync(),
            expenses = expenses.firstSync(),
            products = products.firstSync(),
        )
        saveTextToDownloads(
            context = context,
            fileName = "salao_financeiro_${yearMonth}.csv",
            mimeType = "text/csv",
            text = csv,
        )
    }

    suspend fun importBackupJson(raw: String) {
        val root = JSONObject(raw)
        val meta = root.optJSONObject("meta") ?: JSONObject()
        db.configDao().upsert(meta.toConfig())

        db.serviceDao().clear()
        db.collaboratorDao().clear()
        db.attendanceDao().clear()
        db.expenseDao().clear()
        db.productDao().clear()

        root.optJSONArray("services")?.forEachObject { db.serviceDao().upsert(it.toService()) }
        root.optJSONArray("collaborators")?.forEachObject { db.collaboratorDao().upsert(it.toCollaborator()) }
        root.optJSONArray("attendances")?.forEachObject { db.attendanceDao().upsert(it.toAttendance()) }
        root.optJSONArray("expenses")?.forEachObject { db.expenseDao().upsert(it.toExpense()) }
        root.optJSONArray("products")?.forEachObject { db.productDao().upsert(it.toProduct()) }
    }

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

}

private suspend fun <T> Flow<T>.firstSync(): T = first()

private fun saveTextToDownloads(context: Context, fileName: String, mimeType: String, text: String): Uri {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Não foi possível criar o arquivo em Downloads")
        resolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(text) }
            ?: error("Não foi possível escrever o arquivo")
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }

    @Suppress("DEPRECATION")
    val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!downloads.exists()) downloads.mkdirs()
    val file = File(downloads, fileName)
    FileOutputStream(file).bufferedWriter(Charsets.UTF_8).use { it.write(text) }
    return Uri.fromFile(file)
}

private fun buildCsvForMonth(
    yearMonth: String,
    services: List<ServiceEntity>,
    collaborators: List<CollaboratorEntity>,
    attendances: List<AttendanceEntity>,
    expenses: List<ExpenseEntity>,
    products: List<ProductEntity>,
): String {
    val filteredAttendances = attendances.filter { it.date.startsWith(yearMonth) }
    val filteredExpenses = expenses.filter { it.date.startsWith(yearMonth) }
    val filteredProducts = products.filter { it.date.startsWith(yearMonth) }

    val servicesById = services.associateBy { it.id }
    val collaboratorsById = collaborators.associateBy { it.id }

    val gross = filteredAttendances.sumOf { it.amount }
    val commissions = filteredAttendances.sumOf { it.commission }
    val salonNet = filteredAttendances.sumOf { it.salonValue }
    val expenseTotal = filteredExpenses.sumOf { it.amount }
    val productTotal = filteredProducts.sumOf { it.totalCost }
    val result = salonNet - expenseTotal - productTotal

    val lines = mutableListOf<String>()
    lines += "Resumo do período"
    lines += "Período;$yearMonth"
    lines += "Entradas brutas;${gross.toCsvMoney()}"
    lines += "Comissões;${commissions.toCsvMoney()}"
    lines += "Receita do salão;${salonNet.toCsvMoney()}"
    lines += "Despesas;${expenseTotal.toCsvMoney()}"
    lines += "Produtos;${productTotal.toCsvMoney()}"
    lines += "Resultado;${result.toCsvMoney()}"
    lines += ""

    lines += "Atendimentos"
    lines += "Data;Cliente;Serviço;Colaborador;Valor;Comissão;Salão;Pagamento"
    filteredAttendances.forEach { item ->
        lines += listOf(
            item.date,
            item.client.cleanCsv(),
            servicesById[item.serviceId]?.name.cleanCsv(),
            collaboratorsById[item.collaboratorId]?.name.cleanCsv(),
            item.amount.toCsvMoney(),
            item.commission.toCsvMoney(),
            item.salonValue.toCsvMoney(),
            item.paymentMethod.cleanCsv(),
        ).joinToString(";")
    }
    lines += ""

    lines += "Saídas"
    lines += "Tipo;Data;Categoria;Descrição/Nome;Valor total"
    filteredExpenses.forEach { item ->
        lines += listOf("Despesa", item.date, item.category.cleanCsv(), item.description.cleanCsv(), item.amount.toCsvMoney()).joinToString(";")
    }
    filteredProducts.forEach { item ->
        lines += listOf("Produto", item.date, item.category.cleanCsv(), item.name.cleanCsv(), item.totalCost.toCsvMoney()).joinToString(";")
    }

    return lines.joinToString("\n")
}

private fun String?.cleanCsv(): String = (this ?: "").replace(';', ',')
private fun Double.toCsvMoney(): String = "%.2f".format(this).replace('.', ',')

private fun AppConfigEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("salonName", salonName)
    put("themeColorHex", themeColorHex)
    put("username", username)
    put("password", password)
    put("pinHash", pinHash)
    put("logoUri", logoUri)
    put("createdAt", createdAt)
}

private fun ServiceEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("name", name); put("category", category); put("price", price)
    put("commissionPercent", commissionPercent); put("notes", notes)
}

private fun CollaboratorEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("name", name); put("role", role); put("defaultCommission", defaultCommission)
    put("contact", contact); put("status", status)
}

private fun AttendanceEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("date", date); put("client", client); put("serviceId", serviceId)
    put("collaboratorId", collaboratorId); put("amount", amount); put("paymentMethod", paymentMethod)
    put("commission", commission); put("salonValue", salonValue); put("notes", notes); put("attachmentUri", attachmentUri)
}

private fun ExpenseEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("date", date); put("category", category); put("description", description)
    put("amount", amount); put("paymentMethod", paymentMethod); put("vendor", vendor); put("attachmentUri", attachmentUri)
}

private fun ProductEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("date", date); put("name", name); put("category", category)
    put("quantity", quantity); put("unitCost", unitCost); put("totalCost", totalCost); put("vendor", vendor); put("notes", notes)
}

private fun JSONObject.toConfig(): AppConfigEntity = AppConfigEntity(
    id = optInt("id", 1),
    salonName = optString("salonName", "Salão Financeiro"),
    themeColorHex = optString("themeColorHex", "#6D28D9"),
    username = optString("username", "admin"),
    password = optString("password", "admin123"),
    pinHash = optString("pinHash", ""),
    logoUri = optString("logoUri", ""),
    createdAt = optString("createdAt", nowIso()),
)

private fun JSONObject.toService(): ServiceEntity = ServiceEntity(
    id = optString("id", newId()),
    name = optString("name"),
    category = optString("category"),
    price = optDouble("price", 0.0),
    commissionPercent = optDouble("commissionPercent", 0.0),
    notes = optString("notes"),
)

private fun JSONObject.toCollaborator(): CollaboratorEntity = CollaboratorEntity(
    id = optString("id", newId()),
    name = optString("name"),
    role = optString("role"),
    defaultCommission = optDouble("defaultCommission", 0.0),
    contact = optString("contact"),
    status = optString("status", "ativo"),
)

private fun JSONObject.toAttendance(): AttendanceEntity = AttendanceEntity(
    id = optString("id", newId()),
    date = optString("date", todayIso()),
    client = optString("client"),
    serviceId = optString("serviceId"),
    collaboratorId = optString("collaboratorId"),
    amount = optDouble("amount", 0.0),
    paymentMethod = optString("paymentMethod", "PIX"),
    commission = optDouble("commission", 0.0),
    salonValue = optDouble("salonValue", 0.0),
    notes = optString("notes"),
    attachmentUri = optString("attachmentUri"),
)

private fun JSONObject.toExpense(): ExpenseEntity = ExpenseEntity(
    id = optString("id", newId()),
    date = optString("date", todayIso()),
    category = optString("category"),
    description = optString("description"),
    amount = optDouble("amount", 0.0),
    paymentMethod = optString("paymentMethod", "PIX"),
    vendor = optString("vendor"),
    attachmentUri = optString("attachmentUri"),
)

private fun JSONObject.toProduct(): ProductEntity = ProductEntity(
    id = optString("id", newId()),
    date = optString("date", todayIso()),
    name = optString("name"),
    category = optString("category"),
    quantity = optInt("quantity", 1),
    unitCost = optDouble("unitCost", 0.0),
    totalCost = optDouble("totalCost", 0.0),
    vendor = optString("vendor"),
    notes = optString("notes"),
)

private inline fun JSONArray.forEachObject(block: (JSONObject) -> Unit) {
    for (index in 0 until length()) {
        block(getJSONObject(index))
    }
}
