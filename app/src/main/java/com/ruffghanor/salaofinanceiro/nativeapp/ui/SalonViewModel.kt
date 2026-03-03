package com.ruffghanor.salaofinanceiro.nativeapp.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ruffghanor.salaofinanceiro.nativeapp.data.AppConfigEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.AttendanceEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.CollaboratorEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.ExpenseEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.ProductEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.SalonRepository
import com.ruffghanor.salaofinanceiro.nativeapp.data.ServiceEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.currentMonthIso
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class SalonScreen(val label: String, val subtitle: String) {
    Dashboard("Dashboard", "Resumo do mês atual"),
    Services("Serviços", "Cadastro e preços padrão"),
    Attendances("Atendimentos", "Entradas com comissão automática"),
    Expenses("Despesas", "Saídas e comprovantes"),
    Products("Produtos", "Compras e custos"),
    Collaborators("Colaboradores", "Equipe e comissão padrão"),
    Report("Relatório", "Fechamento mensal e exportação"),
    Import("Importar", "Restaurar base local com backup JSON"),
    Settings("Configurações", "Branding e login local"),
}

data class DashboardMetrics(
    val gross: Double = 0.0,
    val commissions: Double = 0.0,
    val salonNet: Double = 0.0,
    val expenseTotal: Double = 0.0,
    val productTotal: Double = 0.0,
    val outTotal: Double = 0.0,
    val result: Double = 0.0,
    val ticket: Double = 0.0,
    val serviceBreakdown: List<Pair<String, Double>> = emptyList(),
    val collaboratorBreakdown: List<Pair<String, Double>> = emptyList(),
)

private data class MetricsInputs(
    val services: List<ServiceEntity>,
    val collaborators: List<CollaboratorEntity>,
    val attendances: List<AttendanceEntity>,
    val expenses: List<ExpenseEntity>,
    val products: List<ProductEntity>,
)

class SalonViewModel(private val repository: SalonRepository) : ViewModel() {
    val config = repository.config.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppConfigEntity())
    val services = repository.services.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val collaborators = repository.collaborators.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val attendances = repository.attendances.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val expenses = repository.expenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val products = repository.products.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentScreen = MutableStateFlow(SalonScreen.Dashboard)
    val dashboardMonth = MutableStateFlow(currentMonthIso())
    val reportMonth = MutableStateFlow(currentMonthIso())
    val isLoggedIn = MutableStateFlow(false)

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    private val metricsInputs = combine(
        services,
        collaborators,
        attendances,
        expenses,
        products,
    ) { services, collaborators, attendances, expenses, products ->
        MetricsInputs(services, collaborators, attendances, expenses, products)
    }

    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        dashboardMonth,
        metricsInputs,
    ) { ym, inputs ->
        computeMetrics(ym, inputs.services, inputs.collaborators, inputs.attendances, inputs.expenses, inputs.products)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardMetrics())

    val reportMetrics: StateFlow<DashboardMetrics> = combine(
        reportMonth,
        metricsInputs,
    ) { ym, inputs ->
        computeMetrics(ym, inputs.services, inputs.collaborators, inputs.attendances, inputs.expenses, inputs.products)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardMetrics())

    init {
        viewModelScope.launch { repository.ensureSeeded() }
    }

    fun showScreen(screen: SalonScreen) {
        currentScreen.value = screen
    }

    fun login(username: String, password: String) {
        val cfg = config.value
        if (username == cfg.username && password == cfg.password) {
            isLoggedIn.value = true
            postMessage("Sessão iniciada.")
        } else {
            postMessage("Usuário ou senha inválidos.")
        }
    }

    fun loginWithPin(pin: String) {
        val cfg = config.value
        if (cfg.pinHash.isBlank()) {
            postMessage("PIN ainda não configurado.")
            return
        }
        val ok = repository.hashPin(pin) == cfg.pinHash
        if (ok) {
            isLoggedIn.value = true
            postMessage("Sessão iniciada por PIN.")
        } else {
            postMessage("PIN incorreto.")
        }
    }

    fun logout() {
        isLoggedIn.value = false
        postMessage("Sessão encerrada.")
    }

    fun saveSettings(
        salonName: String,
        themeColorHex: String,
        username: String,
        password: String,
        pin: String,
        logoUri: String,
    ) = viewModelScope.launch {
        val current = config.value
        val resolvedPinHash = when {
            pin.isBlank() -> current.pinHash
            pin.matches(Regex("^\\d{4}$")) -> repository.hashPin(pin)
            else -> {
                postMessage("PIN deve ter 4 dígitos.")
                return@launch
            }
        }
        repository.upsertConfig(
            current.copy(
                salonName = salonName.ifBlank { "Salão Financeiro" },
                themeColorHex = themeColorHex.ifBlank { "#6D28D9" },
                username = username.ifBlank { "admin" },
                password = password.ifBlank { "admin123" },
                pinHash = resolvedPinHash,
                logoUri = logoUri,
            )
        )
        postMessage("Configurações salvas.")
    }

    fun saveService(item: ServiceEntity) = viewModelScope.launch {
        repository.upsertService(item)
        postMessage("Serviço salvo.")
    }

    fun deleteService(id: String) = viewModelScope.launch {
        repository.deleteService(id)
        postMessage("Serviço excluído.")
    }

    fun saveCollaborator(item: CollaboratorEntity) = viewModelScope.launch {
        repository.upsertCollaborator(item)
        postMessage("Colaborador salvo.")
    }

    fun deleteCollaborator(id: String) = viewModelScope.launch {
        repository.deleteCollaborator(id)
        postMessage("Colaborador excluído.")
    }

    fun saveAttendance(item: AttendanceEntity) = viewModelScope.launch {
        repository.upsertAttendance(item)
        postMessage("Atendimento salvo.")
    }

    fun deleteAttendance(id: String) = viewModelScope.launch {
        repository.deleteAttendance(id)
        postMessage("Atendimento excluído.")
    }

    fun saveExpense(item: ExpenseEntity) = viewModelScope.launch {
        repository.upsertExpense(item)
        postMessage("Despesa salva.")
    }

    fun deleteExpense(id: String) = viewModelScope.launch {
        repository.deleteExpense(id)
        postMessage("Despesa excluída.")
    }

    fun saveProduct(item: ProductEntity) = viewModelScope.launch {
        repository.upsertProduct(item)
        postMessage("Produto salvo.")
    }

    fun deleteProduct(id: String) = viewModelScope.launch {
        repository.deleteProduct(id)
        postMessage("Produto excluído.")
    }

    fun exportBackup(context: Context) = viewModelScope.launch {
        repository.exportBackupJson(context)
            .onSuccess { postMessage("Backup JSON salvo em Downloads.") }
            .onFailure { postMessage("Falha ao salvar backup: ${it.message ?: "erro"}") }
    }

    fun exportMonthlyCsv(context: Context) = viewModelScope.launch {
        repository.exportMonthlyCsv(context, reportMonth.value)
            .onSuccess { postMessage("CSV mensal salvo em Downloads.") }
            .onFailure { postMessage("Falha ao exportar CSV: ${it.message ?: "erro"}") }
    }

    fun importBackupJson(raw: String) = viewModelScope.launch {
        runCatching { repository.importBackupJson(raw) }
            .onSuccess { postMessage("Backup importado com sucesso.") }
            .onFailure { postMessage("Falha ao importar JSON.") }
    }

    fun loadDemoData() = viewModelScope.launch {
        repository.seedDemoData()
        postMessage("Dados demo carregados.")
    }

    fun setDashboardMonth(value: String) { dashboardMonth.value = value.ifBlank { currentMonthIso() } }
    fun setReportMonth(value: String) { reportMonth.value = value.ifBlank { currentMonthIso() } }

    private fun postMessage(message: String) {
        viewModelScope.launch { _messages.emit(message) }
    }

    fun monthLabel(yearMonth: String): String = runCatching {
        val ym = YearMonth.parse(yearMonth)
        ym.format(DateTimeFormatter.ofPattern("MM/yyyy", Locale("pt", "BR")))
    }.getOrDefault(yearMonth)

    fun computeAttendance(amount: Double, serviceId: String, collaboratorId: String): Pair<Double, Double> {
        val service = services.value.firstOrNull { it.id == serviceId }
        val collaborator = collaborators.value.firstOrNull { it.id == collaboratorId }
        val pct = when {
            service != null && service.commissionPercent > 0 -> service.commissionPercent
            collaborator != null && collaborator.defaultCommission > 0 -> collaborator.defaultCommission
            else -> 0.0
        }
        val commission = amount * (pct / 100.0)
        return commission to (amount - commission).coerceAtLeast(0.0)
    }

    private fun computeMetrics(
        yearMonth: String,
        services: List<ServiceEntity>,
        collaborators: List<CollaboratorEntity>,
        attendances: List<AttendanceEntity>,
        expenses: List<ExpenseEntity>,
        products: List<ProductEntity>,
    ): DashboardMetrics {
        val filteredAttendances = attendances.filter { it.date.startsWith(yearMonth) }
        val filteredExpenses = expenses.filter { it.date.startsWith(yearMonth) }
        val filteredProducts = products.filter { it.date.startsWith(yearMonth) }

        val gross = filteredAttendances.sumOf { it.amount }
        val commissions = filteredAttendances.sumOf { it.commission }
        val salonNet = filteredAttendances.sumOf { it.salonValue }
        val expenseTotal = filteredExpenses.sumOf { it.amount }
        val productTotal = filteredProducts.sumOf { it.totalCost }
        val outTotal = expenseTotal + productTotal
        val result = salonNet - outTotal
        val ticket = if (filteredAttendances.isEmpty()) 0.0 else gross / filteredAttendances.size

        val serviceNames = services.associateBy({ it.id }, { it.name })
        val collaboratorNames = collaborators.associateBy({ it.id }, { it.name })

        val serviceBreakdown = filteredAttendances
            .groupBy { serviceNames[it.serviceId] ?: "Serviço removido" }
            .map { (label, rows) -> label to rows.sumOf { it.amount } }
            .sortedByDescending { it.second }
            .take(6)

        val collaboratorBreakdown = filteredAttendances
            .groupBy { collaboratorNames[it.collaboratorId] ?: "Colaborador removido" }
            .map { (label, rows) -> label to rows.sumOf { it.salonValue } }
            .sortedByDescending { it.second }
            .take(6)

        return DashboardMetrics(
            gross = gross,
            commissions = commissions,
            salonNet = salonNet,
            expenseTotal = expenseTotal,
            productTotal = productTotal,
            outTotal = outTotal,
            result = result,
            ticket = ticket,
            serviceBreakdown = serviceBreakdown,
            collaboratorBreakdown = collaboratorBreakdown,
        )
    }
}

class SalonViewModelFactory(private val repository: SalonRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SalonViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return SalonViewModel(repository) as T
    }
}
