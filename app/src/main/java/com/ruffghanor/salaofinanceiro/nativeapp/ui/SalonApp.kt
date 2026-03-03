package com.ruffghanor.salaofinanceiro.nativeapp.ui

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.ruffghanor.salaofinanceiro.nativeapp.data.AppConfigEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.AttendanceEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.CollaboratorEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.ExpenseEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.ProductEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.ServiceEntity
import com.ruffghanor.salaofinanceiro.nativeapp.data.currentMonthIso
import com.ruffghanor.salaofinanceiro.nativeapp.data.newId
import com.ruffghanor.salaofinanceiro.nativeapp.data.todayIso
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonFinanceiroNativeApp(viewModel: SalonViewModel) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()
    val services by viewModel.services.collectAsState()
    val collaborators by viewModel.collaborators.collectAsState()
    val attendances by viewModel.attendances.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val products by viewModel.products.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val dashboardMonth by viewModel.dashboardMonth.collectAsState()
    val reportMonth by viewModel.reportMonth.collectAsState()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsState()
    val reportMetrics by viewModel.reportMetrics.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = androidx.compose.material3.rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    SalonTheme(config.themeColorHex) {
        Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Background) {
            if (!isLoggedIn) {
                LoginScreen(config = config, onLogin = viewModel::login, onPinLogin = viewModel::loginWithPin)
            } else {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawer(
                            config = config,
                            currentScreen = currentScreen,
                            onScreenSelected = {
                                viewModel.showScreen(it)
                                scope.launch { drawerState.close() }
                            },
                            onLogout = viewModel::logout,
                        )
                    }
                ) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            CenterAlignedTopAppBar(
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = AppColors.Panel,
                                    titleContentColor = AppColors.Text,
                                ),
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = AppColors.Text)
                                    }
                                },
                                title = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(currentScreen.label, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            currentScreen.subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.Muted,
                                        )
                                    }
                                },
                                actions = {
                                    AssistChip(
                                        onClick = {},
                                        enabled = false,
                                        label = { Text("App Android offline") },
                                        colors = AssistChipDefaults.assistChipColors(
                                            disabledContainerColor = AppColors.Success.copy(alpha = 0.16f),
                                            disabledLabelColor = AppColors.SuccessText,
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            )
                        }
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(horizontal = 10.dp)
                        ) {
                            when (currentScreen) {
                                SalonScreen.Dashboard -> DashboardScreen(
                                    month = dashboardMonth,
                                    metrics = dashboardMetrics,
                                    onMonthChange = viewModel::setDashboardMonth,
                                )
                                SalonScreen.Services -> ServicesScreen(
                                    items = services,
                                    onSave = viewModel::saveService,
                                    onDelete = viewModel::deleteService,
                                )
                                SalonScreen.Attendances -> AttendancesScreen(
                                    items = attendances,
                                    services = services,
                                    collaborators = collaborators,
                                    onCompute = viewModel::computeAttendance,
                                    onSave = viewModel::saveAttendance,
                                    onDelete = viewModel::deleteAttendance,
                                )
                                SalonScreen.Expenses -> ExpensesScreen(
                                    items = expenses,
                                    onSave = viewModel::saveExpense,
                                    onDelete = viewModel::deleteExpense,
                                )
                                SalonScreen.Products -> ProductsScreen(
                                    items = products,
                                    onSave = viewModel::saveProduct,
                                    onDelete = viewModel::deleteProduct,
                                )
                                SalonScreen.Collaborators -> CollaboratorsScreen(
                                    items = collaborators,
                                    onSave = viewModel::saveCollaborator,
                                    onDelete = viewModel::deleteCollaborator,
                                )
                                SalonScreen.Report -> ReportScreen(
                                    month = reportMonth,
                                    metrics = reportMetrics,
                                    attendances = attendances.filter { it.date.startsWith(reportMonth) },
                                    expenses = expenses.filter { it.date.startsWith(reportMonth) },
                                    products = products.filter { it.date.startsWith(reportMonth) },
                                    services = services,
                                    collaborators = collaborators,
                                    monthLabel = viewModel.monthLabel(reportMonth),
                                    onMonthChange = viewModel::setReportMonth,
                                    onExportCsv = { viewModel.exportMonthlyCsv(context) },
                                    onBackup = { viewModel.exportBackup(context) },
                                )
                                SalonScreen.Import -> ImportScreen(
                                    onImportJson = viewModel::importBackupJson,
                                    onSeedDemo = viewModel::loadDemoData,
                                )
                                SalonScreen.Settings -> SettingsScreen(
                                    config = config,
                                    onSave = viewModel::saveSettings,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class DrawerItem(val screen: SalonScreen, val icon: ImageVector)

@Composable
private fun AppDrawer(
    config: AppConfigEntity,
    currentScreen: SalonScreen,
    onScreenSelected: (SalonScreen) -> Unit,
    onLogout: () -> Unit,
) {
    val items = listOf(
        DrawerItem(SalonScreen.Dashboard, Icons.Default.Home),
        DrawerItem(SalonScreen.Services, Icons.Default.MiscellaneousServices),
        DrawerItem(SalonScreen.Attendances, Icons.Default.Payments),
        DrawerItem(SalonScreen.Expenses, Icons.Default.ReceiptLong),
        DrawerItem(SalonScreen.Products, Icons.Default.Inventory2),
        DrawerItem(SalonScreen.Collaborators, Icons.Default.Groups),
        DrawerItem(SalonScreen.Report, Icons.Default.BarChart),
        DrawerItem(SalonScreen.Import, Icons.Default.Restore),
        DrawerItem(SalonScreen.Settings, Icons.Default.Settings),
    )
    ModalDrawerSheet(
        drawerContainerColor = AppColors.Panel,
        drawerContentColor = AppColors.Text,
        modifier = Modifier.width(320.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandBadge(small = true)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(config.salonName, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                    Text("Compose + Room", color = AppColors.Muted, style = MaterialTheme.typography.labelSmall)
                }
                if (config.logoUri.isNotBlank()) {
                    UriThumb(config.logoUri, 34.dp)
                }
            }
            Spacer(Modifier.height(16.dp))
            items.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(item.screen.label) },
                    selected = currentScreen == item.screen,
                    onClick = { onScreenSelected(item.screen) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sair")
            }
        }
    }
}

@Composable
private fun LoginScreen(
    config: AppConfigEntity,
    onLogin: (String, String) -> Unit,
    onPinLogin: (String) -> Unit,
) {
    var username by rememberSaveable { mutableStateOf(config.username) }
    var password by rememberSaveable { mutableStateOf(config.password) }
    var pin by rememberSaveable { mutableStateOf("") }
    var usePin by rememberSaveable(config.pinHash) { mutableStateOf(config.pinHash.isNotBlank()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.Panel),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BrandBadge(small = false)
                if (config.logoUri.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        UriThumb(config.logoUri, 72.dp)
                    }
                }
                Text(config.salonName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Versão nativa em Kotlin + Jetpack Compose + Room, mantendo a mesma linguagem visual.",
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (usePin) {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it.filter(Char::isDigit).take(4) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("PIN (4 dígitos)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                    )
                    Button(onClick = { onPinLogin(pin) }, modifier = Modifier.fillMaxWidth()) { Text("Entrar") }
                    OutlinedButton(onClick = { usePin = false }, modifier = Modifier.fillMaxWidth()) { Text("Usar usuário/senha") }
                } else {
                    OutlinedTextField(value = username, onValueChange = { username = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Usuário") })
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Senha") },
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    Button(onClick = { onLogin(username.trim(), password) }, modifier = Modifier.fillMaxWidth()) { Text("Entrar") }
                    if (config.pinHash.isNotBlank()) {
                        OutlinedButton(onClick = { usePin = true }, modifier = Modifier.fillMaxWidth()) { Text("Usar PIN") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashboardScreen(month: String, metrics: DashboardMetrics, onMonthChange: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AppPanel {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MonthField(value = month, onValueChange = onMonthChange, label = "Mês")
                }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Entradas brutas", brl(metrics.gross))
                MetricCard("Comissões", brl(metrics.commissions))
                MetricCard("Receita do salão", brl(metrics.salonNet))
                MetricCard("Saídas totais", brl(metrics.outTotal))
                MetricCard("Resultado", brl(metrics.result))
                MetricCard("Ticket médio", brl(metrics.ticket))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AppPanel(modifier = Modifier.weight(1f)) {
                    Text("Serviços no período", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    BreakdownList(metrics.serviceBreakdown)
                }
                AppPanel(modifier = Modifier.weight(1f)) {
                    Text("Colaboradores no período", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    BreakdownList(metrics.collaboratorBreakdown)
                }
            }
        }
    }
}

@Composable
private fun ServicesScreen(items: List<ServiceEntity>, onSave: (ServiceEntity) -> Unit, onDelete: (String) -> Unit) {
    var editing by remember { mutableStateOf<ServiceEntity?>(null) }
    var name by rememberSaveable(editing?.id) { mutableStateOf(editing?.name ?: "") }
    var category by rememberSaveable(editing?.id) { mutableStateOf(editing?.category ?: "") }
    var price by rememberSaveable(editing?.id) { mutableStateOf(editing?.price?.formatInputMoney() ?: "") }
    var commission by rememberSaveable(editing?.id) { mutableStateOf(editing?.commissionPercent?.formatInputMoney() ?: "") }
    var notes by rememberSaveable(editing?.id) { mutableStateOf(editing?.notes ?: "") }

    CrudTwoPane(
        form = {
            AppPanel {
                Text("Cadastro de serviços", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Nome") })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Categoria") })
                Spacer(Modifier.height(10.dp))
                MoneyField(label = "Preço padrão", value = price, onValueChange = { price = it })
                Spacer(Modifier.height(10.dp))
                MoneyField(label = "Comissão (%)", value = commission, onValueChange = { commission = it })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Observações") })
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        if (name.isBlank()) return@Button
                        onSave(
                            ServiceEntity(
                                id = editing?.id ?: newId(),
                                name = name.trim(),
                                category = category.trim(),
                                price = parseMoney(price),
                                commissionPercent = parseMoney(commission),
                                notes = notes.trim(),
                            )
                        )
                        editing = null; name = ""; category = ""; price = ""; commission = ""; notes = ""
                    }) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Salvar") }
                    OutlinedButton(onClick = { editing = null; name = ""; category = ""; price = ""; commission = ""; notes = "" }) { Text("Limpar") }
                }
            }
        },
        list = {
            AppPanel {
                Text("Lista de serviços", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                if (items.isEmpty()) EmptyText("Nenhum serviço cadastrado ainda.")
                items.forEach { item ->
                    RecordCard(
                        title = item.name,
                        lines = listOf(
                            "Categoria: ${item.category.ifBlank { "-" }}",
                            "Preço: ${brl(item.price)}",
                            "Comissão: ${item.commissionPercent.formatPercent()}",
                            "Obs: ${item.notes.ifBlank { "-" }}",
                        ),
                        onEdit = { editing = item },
                        onDelete = { onDelete(item.id) },
                    )
                }
            }
        }
    )
}

@Composable
private fun CollaboratorsScreen(items: List<CollaboratorEntity>, onSave: (CollaboratorEntity) -> Unit, onDelete: (String) -> Unit) {
    var editing by remember { mutableStateOf<CollaboratorEntity?>(null) }
    var name by rememberSaveable(editing?.id) { mutableStateOf(editing?.name ?: "") }
    var role by rememberSaveable(editing?.id) { mutableStateOf(editing?.role ?: "") }
    var commission by rememberSaveable(editing?.id) { mutableStateOf(editing?.defaultCommission?.formatInputMoney() ?: "") }
    var contact by rememberSaveable(editing?.id) { mutableStateOf(editing?.contact ?: "") }
    var status by rememberSaveable(editing?.id) { mutableStateOf(editing?.status ?: "ativo") }

    CrudTwoPane(
        form = {
            AppPanel {
                Text("Cadastro de colaboradores", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Nome") })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(role, { role = it }, Modifier.fillMaxWidth(), label = { Text("Função") })
                Spacer(Modifier.height(10.dp))
                MoneyField(label = "Comissão padrão (%)", value = commission, onValueChange = { commission = it })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(contact, { contact = it }, Modifier.fillMaxWidth(), label = { Text("Contato") })
                Spacer(Modifier.height(10.dp))
                ChoiceRow(label = "Status", values = listOf("ativo", "inativo"), selected = status, onSelected = { status = it })
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        if (name.isBlank() || role.isBlank()) return@Button
                        onSave(
                            CollaboratorEntity(
                                id = editing?.id ?: newId(),
                                name = name.trim(),
                                role = role.trim(),
                                defaultCommission = parseMoney(commission),
                                contact = contact.trim(),
                                status = status,
                            )
                        )
                        editing = null; name = ""; role = ""; commission = ""; contact = ""; status = "ativo"
                    }) { Text("Salvar") }
                    OutlinedButton(onClick = { editing = null; name = ""; role = ""; commission = ""; contact = ""; status = "ativo" }) { Text("Limpar") }
                }
            }
        },
        list = {
            AppPanel {
                Text("Lista de colaboradores", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                if (items.isEmpty()) EmptyText("Nenhum colaborador cadastrado ainda.")
                items.forEach { item ->
                    RecordCard(
                        title = item.name,
                        lines = listOf(
                            "Função: ${item.role}",
                            "Comissão padrão: ${item.defaultCommission.formatPercent()}",
                            "Contato: ${item.contact.ifBlank { "-" }}",
                            "Status: ${item.status}",
                        ),
                        onEdit = { editing = item },
                        onDelete = { onDelete(item.id) },
                    )
                }
            }
        }
    )
}

@Composable
private fun AttendancesScreen(
    items: List<AttendanceEntity>,
    services: List<ServiceEntity>,
    collaborators: List<CollaboratorEntity>,
    onCompute: (Double, String, String) -> Pair<Double, Double>,
    onSave: (AttendanceEntity) -> Unit,
    onDelete: (String) -> Unit,
) {
    val context = LocalContext.current
    var editing by remember { mutableStateOf<AttendanceEntity?>(null) }
    var date by rememberSaveable(editing?.id) { mutableStateOf(editing?.date ?: todayIso()) }
    var client by rememberSaveable(editing?.id) { mutableStateOf(editing?.client ?: "") }
    var serviceId by rememberSaveable(editing?.id) { mutableStateOf(editing?.serviceId ?: services.firstOrNull()?.id.orEmpty()) }
    var collaboratorId by rememberSaveable(editing?.id) { mutableStateOf(editing?.collaboratorId ?: collaborators.firstOrNull()?.id.orEmpty()) }
    var amount by rememberSaveable(editing?.id) { mutableStateOf(editing?.amount?.formatInputMoney() ?: "") }
    var paymentMethod by rememberSaveable(editing?.id) { mutableStateOf(editing?.paymentMethod ?: "PIX") }
    var notes by rememberSaveable(editing?.id) { mutableStateOf(editing?.notes ?: "") }
    var attachmentUri by rememberSaveable(editing?.id) { mutableStateOf(editing?.attachmentUri ?: "") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            persistReadPermission(context, it)
            attachmentUri = it.toString()
        }
    }

    LaunchedEffect(services, collaborators) {
        if (serviceId.isBlank() && services.isNotEmpty()) serviceId = services.first().id
        if (collaboratorId.isBlank() && collaborators.isNotEmpty()) collaboratorId = collaborators.first().id
    }

    val computed = remember(amount, serviceId, collaboratorId, services, collaborators) { onCompute(parseMoney(amount), serviceId, collaboratorId) }

    CrudTwoPane(
        form = {
            AppPanel {
                Text("Lançar atendimento", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                DateField(date, { date = it }, "Data")
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(client, { client = it }, Modifier.fillMaxWidth(), label = { Text("Cliente") })
                Spacer(Modifier.height(10.dp))
                ChoiceRow("Serviço", services.map { it.name }, services.firstOrNull { it.id == serviceId }?.name) { label ->
                    serviceId = services.firstOrNull { it.name == label }?.id.orEmpty()
                }
                Spacer(Modifier.height(10.dp))
                ChoiceRow("Colaborador", collaborators.filter { it.status != "inativo" }.map { it.name }, collaborators.firstOrNull { it.id == collaboratorId }?.name) { label ->
                    collaboratorId = collaborators.firstOrNull { it.name == label }?.id.orEmpty()
                }
                Spacer(Modifier.height(10.dp))
                MoneyField("Valor cobrado", amount) { amount = it }
                Spacer(Modifier.height(10.dp))
                ChoiceRow("Pagamento", listOf("PIX", "Dinheiro", "Cartão", "Transferência"), paymentMethod) { paymentMethod = it }
                Spacer(Modifier.height(10.dp))
                ReadOnlyField("Comissão calculada", brl(computed.first))
                Spacer(Modifier.height(10.dp))
                ReadOnlyField("Valor do salão", brl(computed.second))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Observações") })
                Spacer(Modifier.height(10.dp))
                AttachmentRow(attachmentUri = attachmentUri, onPick = { picker.launch(arrayOf("image/*", "application/pdf")) }, label = "Foto/comprovante")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        if (serviceId.isBlank() || collaboratorId.isBlank()) return@Button
                        onSave(
                            AttendanceEntity(
                                id = editing?.id ?: newId(),
                                date = date.ifBlank { todayIso() },
                                client = client.trim(),
                                serviceId = serviceId,
                                collaboratorId = collaboratorId,
                                amount = parseMoney(amount),
                                paymentMethod = paymentMethod,
                                commission = computed.first,
                                salonValue = computed.second,
                                notes = notes.trim(),
                                attachmentUri = attachmentUri,
                            )
                        )
                        editing = null; date = todayIso(); client = ""; amount = ""; paymentMethod = "PIX"; notes = ""; attachmentUri = ""
                    }) { Text("Salvar") }
                    OutlinedButton(onClick = { editing = null; date = todayIso(); client = ""; amount = ""; paymentMethod = "PIX"; notes = ""; attachmentUri = "" }) { Text("Limpar") }
                }
            }
        },
        list = {
            AppPanel {
                Text("Atendimentos", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                if (items.isEmpty()) EmptyText("Nenhum atendimento lançado ainda.")
                val serviceNames = services.associateBy({ it.id }, { it.name })
                val collaboratorNames = collaborators.associateBy({ it.id }, { it.name })
                items.forEach { item ->
                    RecordCard(
                        title = "${if (item.client.isBlank()) "Cliente avulso" else item.client} • ${serviceNames[item.serviceId] ?: "Serviço"}",
                        lines = listOf(
                            "Data: ${item.date}",
                            "Colaborador: ${collaboratorNames[item.collaboratorId] ?: "-"}",
                            "Entrada: ${brl(item.amount)}",
                            "Comissão: ${brl(item.commission)}",
                            "Salão: ${brl(item.salonValue)}",
                            "Pagamento: ${item.paymentMethod}",
                            "Obs: ${item.notes.ifBlank { "-" }}",
                            "Anexo: ${if (item.attachmentUri.isBlank()) "-" else item.attachmentUri.toUri().lastPathSegment ?: "arquivo"}",
                        ),
                        onEdit = { editing = item },
                        onDelete = { onDelete(item.id) },
                    )
                }
            }
        }
    )
}

@Composable
private fun ExpensesScreen(items: List<ExpenseEntity>, onSave: (ExpenseEntity) -> Unit, onDelete: (String) -> Unit) {
    val context = LocalContext.current
    var editing by remember { mutableStateOf<ExpenseEntity?>(null) }
    var date by rememberSaveable(editing?.id) { mutableStateOf(editing?.date ?: todayIso()) }
    var category by rememberSaveable(editing?.id) { mutableStateOf(editing?.category ?: "") }
    var description by rememberSaveable(editing?.id) { mutableStateOf(editing?.description ?: "") }
    var amount by rememberSaveable(editing?.id) { mutableStateOf(editing?.amount?.formatInputMoney() ?: "") }
    var paymentMethod by rememberSaveable(editing?.id) { mutableStateOf(editing?.paymentMethod ?: "PIX") }
    var vendor by rememberSaveable(editing?.id) { mutableStateOf(editing?.vendor ?: "") }
    var attachmentUri by rememberSaveable(editing?.id) { mutableStateOf(editing?.attachmentUri ?: "") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { persistReadPermission(context, it); attachmentUri = it.toString() }
    }

    CrudTwoPane(
        form = {
            AppPanel {
                Text("Lançar despesa", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                DateField(date, { date = it }, "Data")
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Categoria") })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Descrição") })
                Spacer(Modifier.height(10.dp))
                MoneyField("Valor", amount) { amount = it }
                Spacer(Modifier.height(10.dp))
                ChoiceRow("Pagamento", listOf("PIX", "Dinheiro", "Cartão", "Boleto"), paymentMethod) { paymentMethod = it }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(vendor, { vendor = it }, Modifier.fillMaxWidth(), label = { Text("Fornecedor") })
                Spacer(Modifier.height(10.dp))
                AttachmentRow(attachmentUri, { picker.launch(arrayOf("image/*", "application/pdf")) }, "Comprovante")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        if (category.isBlank() || description.isBlank()) return@Button
                        onSave(
                            ExpenseEntity(
                                id = editing?.id ?: newId(),
                                date = date.ifBlank { todayIso() },
                                category = category.trim(),
                                description = description.trim(),
                                amount = parseMoney(amount),
                                paymentMethod = paymentMethod,
                                vendor = vendor.trim(),
                                attachmentUri = attachmentUri,
                            )
                        )
                        editing = null; date = todayIso(); category = ""; description = ""; amount = ""; paymentMethod = "PIX"; vendor = ""; attachmentUri = ""
                    }) { Text("Salvar") }
                    OutlinedButton(onClick = { editing = null; date = todayIso(); category = ""; description = ""; amount = ""; paymentMethod = "PIX"; vendor = ""; attachmentUri = "" }) { Text("Limpar") }
                }
            }
        },
        list = {
            AppPanel {
                Text("Despesas", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                if (items.isEmpty()) EmptyText("Nenhuma despesa lançada ainda.")
                items.forEach { item ->
                    RecordCard(
                        title = "${item.description} • ${brl(item.amount)}",
                        lines = listOf(
                            "Data: ${item.date}",
                            "Categoria: ${item.category}",
                            "Pagamento: ${item.paymentMethod}",
                            "Fornecedor: ${item.vendor.ifBlank { "-" }}",
                            "Comprovante: ${if (item.attachmentUri.isBlank()) "-" else item.attachmentUri.toUri().lastPathSegment ?: "arquivo"}",
                        ),
                        onEdit = { editing = item },
                        onDelete = { onDelete(item.id) },
                    )
                }
            }
        }
    )
}

@Composable
private fun ProductsScreen(items: List<ProductEntity>, onSave: (ProductEntity) -> Unit, onDelete: (String) -> Unit) {
    var editing by remember { mutableStateOf<ProductEntity?>(null) }
    var date by rememberSaveable(editing?.id) { mutableStateOf(editing?.date ?: todayIso()) }
    var name by rememberSaveable(editing?.id) { mutableStateOf(editing?.name ?: "") }
    var category by rememberSaveable(editing?.id) { mutableStateOf(editing?.category ?: "") }
    var quantity by rememberSaveable(editing?.id) { mutableStateOf(editing?.quantity?.toString() ?: "1") }
    var unitCost by rememberSaveable(editing?.id) { mutableStateOf(editing?.unitCost?.formatInputMoney() ?: "") }
    var vendor by rememberSaveable(editing?.id) { mutableStateOf(editing?.vendor ?: "") }
    var notes by rememberSaveable(editing?.id) { mutableStateOf(editing?.notes ?: "") }
    val qty = quantity.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val totalCost = qty * parseMoney(unitCost)

    CrudTwoPane(
        form = {
            AppPanel {
                Text("Registrar produto", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                DateField(date, { date = it }, "Data")
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Produto") })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Categoria") })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(quantity, { quantity = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text("Quantidade") })
                Spacer(Modifier.height(10.dp))
                MoneyField("Custo unitário", unitCost) { unitCost = it }
                Spacer(Modifier.height(10.dp))
                ReadOnlyField("Custo total", brl(totalCost))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(vendor, { vendor = it }, Modifier.fillMaxWidth(), label = { Text("Fornecedor") })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Observações") })
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        if (name.isBlank()) return@Button
                        onSave(
                            ProductEntity(
                                id = editing?.id ?: newId(),
                                date = date.ifBlank { todayIso() },
                                name = name.trim(),
                                category = category.trim(),
                                quantity = qty,
                                unitCost = parseMoney(unitCost),
                                totalCost = totalCost,
                                vendor = vendor.trim(),
                                notes = notes.trim(),
                            )
                        )
                        editing = null; date = todayIso(); name = ""; category = ""; quantity = "1"; unitCost = ""; vendor = ""; notes = ""
                    }) { Text("Salvar") }
                    OutlinedButton(onClick = { editing = null; date = todayIso(); name = ""; category = ""; quantity = "1"; unitCost = ""; vendor = ""; notes = "" }) { Text("Limpar") }
                }
            }
        },
        list = {
            AppPanel {
                Text("Produtos comprados", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                if (items.isEmpty()) EmptyText("Nenhum produto registrado ainda.")
                items.forEach { item ->
                    RecordCard(
                        title = "${item.name} • ${brl(item.totalCost)}",
                        lines = listOf(
                            "Data: ${item.date}",
                            "Categoria: ${item.category.ifBlank { "-" }}",
                            "Quantidade: ${item.quantity}",
                            "Custo unitário: ${brl(item.unitCost)}",
                            "Fornecedor: ${item.vendor.ifBlank { "-" }}",
                            "Obs: ${item.notes.ifBlank { "-" }}",
                        ),
                        onEdit = { editing = item },
                        onDelete = { onDelete(item.id) },
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportScreen(
    month: String,
    metrics: DashboardMetrics,
    attendances: List<AttendanceEntity>,
    expenses: List<ExpenseEntity>,
    products: List<ProductEntity>,
    services: List<ServiceEntity>,
    collaborators: List<CollaboratorEntity>,
    monthLabel: String,
    onMonthChange: (String) -> Unit,
    onExportCsv: () -> Unit,
    onBackup: () -> Unit,
) {
    val serviceNames = services.associateBy({ it.id }, { it.name })
    val collaboratorNames = collaborators.associateBy({ it.id }, { it.name })

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            AppPanel {
                Text("Relatório mensal", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                MonthField(value = month, onValueChange = onMonthChange, label = "Mês")
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onExportCsv) { Text("Exportar CSV") }
                    OutlinedButton(onClick = onBackup) { Text("Backup JSON") }
                }
                Spacer(Modifier.height(12.dp))
                listOf(
                    "Período" to monthLabel,
                    "Entradas brutas" to brl(metrics.gross),
                    "Comissões pagas" to brl(metrics.commissions),
                    "Receita líquida do salão" to brl(metrics.salonNet),
                    "Despesas operacionais" to brl(metrics.expenseTotal),
                    "Produtos" to brl(metrics.productTotal),
                    "Resultado do período" to brl(metrics.result),
                ).forEach { (label, value) ->
                    SummaryLine(label, value)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AppPanel(modifier = Modifier.weight(1f)) {
                    Text("Atendimentos do período", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    if (attendances.isEmpty()) EmptyText("Sem atendimentos no período.")
                    attendances.forEach { item ->
                        RecordCard(
                            title = "${item.date} • ${serviceNames[item.serviceId] ?: "Serviço"}",
                            lines = listOf(
                                "Cliente: ${item.client.ifBlank { "Avulso" }}",
                                "Colaborador: ${collaboratorNames[item.collaboratorId] ?: "-"}",
                                "Entrada: ${brl(item.amount)} | Comissão: ${brl(item.commission)} | Salão: ${brl(item.salonValue)}",
                            ),
                            onEdit = null,
                            onDelete = null,
                        )
                    }
                }
                AppPanel(modifier = Modifier.weight(1f)) {
                    Text("Despesas e produtos", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    if (expenses.isEmpty() && products.isEmpty()) EmptyText("Sem saídas no período.")
                    expenses.forEach { item ->
                        RecordCard(
                            title = "Despesa • ${item.category}",
                            lines = listOf("${item.date} • ${item.description} • ${brl(item.amount)}"),
                            onEdit = null,
                            onDelete = null,
                        )
                    }
                    products.forEach { item ->
                        RecordCard(
                            title = "Produto • ${item.name}",
                            lines = listOf("${item.date} • ${item.quantity}x • ${brl(item.totalCost)}"),
                            onEdit = null,
                            onDelete = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportScreen(onImportJson: (String) -> Unit, onSeedDemo: () -> Unit) {
    val context = LocalContext.current
    var rawJson by rememberSaveable { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            persistReadPermission(context, it)
            readUriText(context, it)?.let(onImportJson)
        }
    }

    AppPanel(modifier = Modifier.padding(top = 4.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text("Importar dados", fontWeight = FontWeight.SemiBold)
            Text("Esta versão nativa já aceita backup JSON, mantendo a mesma lógica da UI híbrida.", color = AppColors.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { launcher.launch(arrayOf("application/json")) }) { Text("Selecionar JSON") }
                OutlinedButton(onClick = onSeedDemo) { Text("Carregar dados demo") }
            }
            OutlinedTextField(
                value = rawJson,
                onValueChange = { rawJson = it },
                modifier = Modifier.fillMaxWidth().height(220.dp),
                label = { Text("Ou cole o JSON manualmente") },
            )
            Button(onClick = { if (rawJson.isNotBlank()) onImportJson(rawJson) }) { Text("Importar backup") }
            Text("Ao importar, a base local atual será substituída.", color = AppColors.Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SettingsScreen(config: AppConfigEntity, onSave: (String, String, String, String, String, String) -> Unit) {
    val context = LocalContext.current
    var salonName by rememberSaveable(config.salonName) { mutableStateOf(config.salonName) }
    var themeColor by rememberSaveable(config.themeColorHex) { mutableStateOf(config.themeColorHex) }
    var username by rememberSaveable(config.username) { mutableStateOf(config.username) }
    var password by rememberSaveable(config.password) { mutableStateOf(config.password) }
    var pin by rememberSaveable { mutableStateOf("") }
    var logoUri by rememberSaveable(config.logoUri) { mutableStateOf(config.logoUri) }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { persistReadPermission(context, it); logoUri = it.toString() }
    }

    AppPanel(modifier = Modifier.padding(top = 4.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text("Configurações locais", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(salonName, { salonName = it }, Modifier.fillMaxWidth(), label = { Text("Nome do salão") })
            OutlinedTextField(themeColor, { themeColor = it.take(7) }, Modifier.fillMaxWidth(), label = { Text("Cor principal (hex)") })
            AttachmentRow(logoUri, { logoPicker.launch(arrayOf("image/*")) }, "Logo do salão")
            OutlinedTextField(
                pin,
                { pin = it.filter(Char::isDigit).take(4) },
                Modifier.fillMaxWidth(),
                label = { Text("Definir/alterar PIN (4 dígitos)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
            )
            OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), label = { Text("Usuário admin") })
            OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Senha admin") }, visualTransformation = PasswordVisualTransformation())
            Button(onClick = { onSave(salonName, themeColor, username, password, pin, logoUri); pin = "" }) { Text("Salvar configurações") }
        }
    }
}

@Composable
private fun CrudTwoPane(form: @Composable () -> Unit, list: @Composable () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { form() }
        item { list() }
    }
}

@Composable
private fun AppPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Panel),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun MetricCard(label: String, value: String) {
    Card(
        modifier = Modifier.width(170.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.PanelAlt),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = AppColors.Muted, style = MaterialTheme.typography.labelMedium)
            Text(value, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun BreakdownList(rows: List<Pair<String, Double>>) {
    if (rows.isEmpty()) {
        EmptyText("Sem dados neste período.")
        return
    }
    val max = rows.maxOfOrNull { it.second } ?: 1.0
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { (label, value) ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = AppColors.Muted, style = MaterialTheme.typography.labelMedium)
                    Text(brl(value), style = MaterialTheme.typography.labelMedium)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppColors.Track)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((value / max).toFloat().coerceIn(0.06f, 1f))
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppColors.Primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = AppColors.Muted)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RecordCard(
    title: String,
    lines: List<String>,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.PanelAlt.copy(alpha = 0.72f)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            lines.forEach { Text(it, color = AppColors.Muted, style = MaterialTheme.typography.bodySmall) }
            if (onEdit != null || onDelete != null) {
                Divider(color = AppColors.Line)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    onEdit?.let { TextButton(onClick = it) { Text("Editar") } }
                    onDelete?.let { TextButton(onClick = it) { Text("Excluir", color = AppColors.Danger) } }
                }
            }
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(text, color = AppColors.Muted, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun MonthField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.take(7)) },
        label = { Text(label) },
        supportingText = { Text("Formato: AAAA-MM") },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DateField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.take(10)) },
        label = { Text(label) },
        supportingText = { Text("Formato: AAAA-MM-DD") },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MoneyField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' }) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
    )
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceRow(label: String, values: List<String>, selected: String?, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = AppColors.Muted, style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { value ->
                val isSelected = value == selected
                AssistChip(
                    onClick = { onSelected(value) },
                    label = { Text(value) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected) AppColors.Primary.copy(alpha = 0.22f) else AppColors.PanelAlt,
                        labelColor = AppColors.Text,
                    )
                )
            }
        }
    }
}

@Composable
private fun AttachmentRow(attachmentUri: String, onPick: () -> Unit, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = AppColors.Muted, style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onPick) {
                Icon(Icons.Default.AttachFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Selecionar")
            }
            if (attachmentUri.isNotBlank()) {
                Text(attachmentUri.toUri().lastPathSegment ?: "arquivo", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BrandBadge(small: Boolean) {
    val size = if (small) 44.dp else 64.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(if (small) 14.dp else 20.dp))
            .background(AppColors.Primary),
        contentAlignment = Alignment.Center,
    ) {
        Text("SF", fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@Composable
private fun UriThumb(uriString: String, size: androidx.compose.ui.unit.Dp) {
    val uri = remember(uriString) { runCatching { uriString.toUri() }.getOrNull() }
    if (uri == null) return
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                clipToOutline = true
            }
        },
        update = { it.setImageURI(uri) },
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
    )
}

private fun persistReadPermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
    }
}

private fun readUriText(context: Context, uri: Uri): String? = runCatching {
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BufferedReader(InputStreamReader(stream)).readText()
    }
}.getOrNull()

private fun brl(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
private fun parseMoney(raw: String): Double = raw.trim().replace("R$", "").replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
private fun Double.formatInputMoney(): String = if (this == 0.0) "" else String.format(Locale.US, "%.2f", this).replace('.', ',')
private fun Double.formatPercent(): String = String.format(Locale.US, "%.2f%%", this)
