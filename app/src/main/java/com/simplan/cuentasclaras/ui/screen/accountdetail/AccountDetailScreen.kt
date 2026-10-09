package com.simplan.cuentasclaras.ui.screen.accountdetail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplan.cuentasclaras.common.Currency
import com.simplan.cuentasclaras.common.Money
import com.simplan.cuentasclaras.domain.entity.Entry
import com.simplan.cuentasclaras.ui.Icons
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAccountHistory: (Long) -> Unit,
    viewModel: AccountDetailViewModel = hiltViewModel()
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val error by viewModel.error.collectAsStateWithLifecycle(null)
    val entries by viewModel.entries.collectAsStateWithLifecycle(emptyList())

    val context = LocalContext.current
    val appLocale = LocalConfiguration.current.locales[0]
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editCurrency by remember { mutableStateOf("PEN") }
    var editIsOpen by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showExportOptions by remember { mutableStateOf(false) }
    var entryEditing by remember { mutableStateOf<Entry?>(null) }
    var editEntryAmount by remember { mutableStateOf("") }
    var editEntryNote by remember { mutableStateOf("") }
    var editEntryIsIncome by remember { mutableStateOf(true) }
    var entryDeleting by remember { mutableStateOf<Entry?>(null) }

    val currencies = Currency.all

    val incomeGreen = MaterialTheme.colorScheme.primary
    val expenseRed = MaterialTheme.colorScheme.error

    val totalIncome = entries.filter { it.isIncome }.sumOf { it.amountCents }
    val totalExpense = entries.filter { !it.isIncome }.sumOf { it.amountCents }
    val totalNet = totalIncome - totalExpense

    fun startExport(format: String) {
        showExportOptions = false
        scope.launch {
            val result = if (format == "PDF") {
                viewModel.exportAccountPdf()
            } else {
                viewModel.exportAccount()
            }
            if (result.isSuccess()) {
                val uri = result.getOrThrow()
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = if (format == "PDF") "application/pdf" else "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        "Cuenta ${account?.name ?: ""} - Cuentas Claras"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(send, "Compartir exportación")
                )
            }
        }
    }

    // Cargar cuenta al entrar
    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }

    // Mostrar errores con Snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de cuenta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.arrowBack(), contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            account?.let { acc ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ---------- Tarjeta principal ----------
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.accountBalanceWallet(),
                                        contentDescription = "Cuenta",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = acc.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Estado: ",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = if (acc.isOpen) "Abierta" else "Cerrada",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (acc.isOpen) incomeGreen else expenseRed
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Moneda: ",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${acc.currency.name} (${acc.currency.symbol})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Creada: ",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = SimpleDateFormat(
                                        "dd/MM/yyyy HH:mm",
                                        appLocale
                                    ).format(Date(acc.createdAt)),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // ---------- Movimientos ----------
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Movimientos",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = entries.size.toString(),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            if (entries.isEmpty()) {
                                Text(
                                    text = "Sin movimientos todavía",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val entryDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", appLocale)
                            entries.sortedByDescending { it.date }.forEach { entry ->
                                val sign = if (entry.isIncome) "+" else "-"
                                val typeColor = if (entry.isIncome) incomeGreen else expenseRed
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = entryDateFormat.format(Date(entry.date)),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = entry.note.ifBlank { "Sin concepto" },
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 2
                                            )
                                            Text(
                                                text = if (entry.isIncome) "Entrada" else "Salida",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = typeColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$sign ${Money.format(entry.amountCents, acc.currency)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = typeColor
                                        )
                                        IconButton(onClick = {
                                            entryEditing = entry
                                            editEntryAmount =
                                                (entry.amountCents / 100.0).toString()
                                            editEntryNote = entry.note
                                            editEntryIsIncome = entry.isIncome
                                        }) {
                                            Icon(
                                                Icons.edit(),
                                                contentDescription = "Editar movimiento"
                                            )
                                        }
                                        IconButton(onClick = { entryDeleting = entry }) {
                                            Icon(
                                                Icons.delete(),
                                                contentDescription = "Eliminar movimiento",
                                                tint = expenseRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ---------- Acciones ----------
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Acciones",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        editName = acc.name
                                        editCurrency = acc.currency.code
                                        editIsOpen = acc.isOpen
                                        showEditDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.edit(), contentDescription = "Editar")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Editar")
                                }
                                Button(
                                    onClick = { showExportOptions = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(Icons.fileDownload(), contentDescription = "Exportar")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Exportar")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { onNavigateToAccountHistory(acc.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.calendarMonth(), contentDescription = "Historial")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ver historial y estadísticas")
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            if (!acc.isOpen || !acc.isDefault) {
                                Button(
                                    onClick = { showDeleteConfirm = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    )
                                ) {
                                    Icon(Icons.delete(), contentDescription = "Eliminar")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Eliminar cuenta")
                                }
                            } else {
                                Text(
                                    text = "Esta es la cuenta principal y está abierta. " +
                                        "Selecciona otra como principal en Administrar cuentas para poder eliminarla.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        }
                    }

                    // ---------- Resumen ----------
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Resumen",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total de entradas",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "+ ${Money.format(totalIncome, acc.currency)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = incomeGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total de salidas",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "- ${Money.format(totalExpense, acc.currency)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = expenseRed
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Neto de la cuenta",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (totalNet >= 0) {
                                        "+ ${Money.format(totalNet, acc.currency)}"
                                    } else {
                                        Money.format(totalNet, acc.currency)
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalNet >= 0) incomeGreen else expenseRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------- Diálogo editar ----------
    if (showEditDialog && account != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false; expanded = false },
            title = { Text("Editar cuenta") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )

                    // Selector de moneda con Box + DropdownMenu
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val selectedCurrency =
                            currencies.find { it.code == editCurrency } ?: Currency.PEN

                        OutlinedTextField(
                            value = "${selectedCurrency.name} (${selectedCurrency.symbol})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Moneda") },
                            trailingIcon = {
                                Icon(
                                    Icons.expandMore(),
                                    contentDescription = "Seleccionar moneda",
                                    modifier = Modifier.clickable { expanded = !expanded }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true }
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            currencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text("${currency.name} (${currency.symbol})") },
                                    onClick = {
                                        editCurrency = currency.code
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cuenta abierta", fontSize = 16.sp)
                        Switch(
                            checked = editIsOpen,
                            onCheckedChange = { editIsOpen = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = incomeGreen,
                                checkedTrackColor = incomeGreen.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val current = account ?: return@Button
                        val updatedAccount = current.copy(
                            name = editName.trim(),
                            currencyCode = editCurrency,
                            isOpen = editIsOpen
                        )
                        scope.launch {
                            val result = viewModel.updateAccount(updatedAccount)
                            if (result.isSuccess()) {
                                showEditDialog = false
                            }
                        }
                    },
                    enabled = editName.trim().isNotEmpty()
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo eliminar ----------
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar cuenta") },
            text = {
                Text(
                    "¿Estás seguro de que quieres eliminar esta cuenta? Se perderán todos los datos asociados.",
                    modifier = Modifier.padding(8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val result = viewModel.deleteAccount()
                            if (result.isSuccess()) {
                                onNavigateBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo exportar ----------
    if (showExportOptions) {
        AlertDialog(
            onDismissRequest = { showExportOptions = false },
            title = { Text("Exportar cuenta") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Se generará un archivo con los movimientos y se abrirá el menú para compartirlo.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        onClick = { startExport("CSV") },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Icon(
                                Icons.fileDownload(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "CSV (hoja de cálculo)",
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Se abre en Excel o Google Sheets",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Surface(
                        onClick = { startExport("PDF") },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Icon(
                                Icons.fileDownload(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "PDF (documento)",
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Para imprimir o compartir como archivo fijo",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportOptions = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo editar movimiento ----------
    entryEditing?.let { entry ->
        val editingAccount = account ?: return@let
        AlertDialog(
            onDismissRequest = { entryEditing = null },
            title = { Text("Editar movimiento") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (editEntryIsIncome) "Entrada" else "Salida", fontSize = 16.sp)
                        Switch(
                            checked = editEntryIsIncome,
                            onCheckedChange = { editEntryIsIncome = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = incomeGreen,
                                checkedTrackColor = incomeGreen.copy(alpha = 0.3f)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = editEntryAmount,
                        onValueChange = { editEntryAmount = it },
                        label = { Text("Monto") },
                        placeholder = { Text("0,00") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editEntryNote,
                        onValueChange = { editEntryNote = it },
                        label = { Text("Concepto") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedAmount = Money.parse(
                            editEntryAmount,
                            editingAccount.currency
                        )
                        if (parsedAmount <= 0) return@Button
                        val updatedEntry = entry.copyWith(
                            amountCents = parsedAmount,
                            type = if (editEntryIsIncome)
                                Entry.EntryType.INCOME
                            else
                                Entry.EntryType.EXPENSE,
                            note = editEntryNote.trim()
                        )
                        scope.launch {
                            val result = viewModel.updateEntry(updatedEntry)
                            if (result.isSuccess()) {
                                entryEditing = null
                            }
                        }
                    },
                    enabled = editEntryAmount.trim().isNotEmpty()
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { entryEditing = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo eliminar movimiento ----------
    entryDeleting?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryDeleting = null },
            title = { Text("Eliminar movimiento") },
            text = {
                Text(
                    "¿Eliminar el movimiento ${entry.note.ifBlank { "sin concepto" }} " +
                        "por ${Money.format(entry.amountCents, account?.currency ?: Currency.PEN)}?",
                    modifier = Modifier.padding(8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val result = viewModel.deleteEntry(entry.id)
                            if (result.isSuccess()) {
                                entryDeleting = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                Button(onClick = { entryDeleting = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}