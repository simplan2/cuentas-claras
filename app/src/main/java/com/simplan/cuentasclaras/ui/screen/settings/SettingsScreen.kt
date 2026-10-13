package com.simplan.cuentasclaras.ui.screen.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplan.cuentasclaras.BuildConfig
import com.simplan.cuentasclaras.R
import com.simplan.cuentasclaras.common.Currency
import com.simplan.cuentasclaras.common.Money
import com.simplan.cuentasclaras.domain.entity.QuickAmount
import com.simplan.cuentasclaras.domain.entity.Settings
import com.simplan.cuentasclaras.ui.Icons
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val quickAmounts by viewModel.quickAmounts.collectAsStateWithLifecycle(emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val error by viewModel.error.collectAsStateWithLifecycle(null)
    val message by viewModel.message.collectAsStateWithLifecycle(null)
    val showQuickAmounts by viewModel.showQuickAmounts.collectAsStateWithLifecycle(false)
    val hasPin by viewModel.hasPin.collectAsStateWithLifecycle(false)
    val biometricAvailable by viewModel.biometricAvailable.collectAsStateWithLifecycle(false)
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle(false)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showAddQuickAmountDialog by remember { mutableStateOf(false) }
    var newQuickAmountLabel by remember { mutableStateOf("") }
    var newQuickAmountValue by remember { mutableStateOf("") }
    var editingQuickAmount by remember { mutableStateOf<QuickAmount?>(null) }
    var editLabel by remember { mutableStateOf("") }
    var editValue by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf<QuickAmount?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinDialogError by remember { mutableStateOf<String?>(null) }
    var showRemovePinConfirm by remember { mutableStateOf(false) }
    var showMaxAccountsDialog by remember { mutableStateOf(false) }
    var maxAccountsInput by remember { mutableStateOf("10") }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAutoLockDelayDialog by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showRecoveryCodeDialog by remember { mutableStateOf(false) }
    var recoveryCodeToShow by remember { mutableStateOf<String?>(null) }

    val currency = settings?.currency ?: Currency.PEN
    val themeMode = settings?.themeMode ?: Settings.ThemeMode.SYSTEM

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch { viewModel.exportBackupTo(uri) }
        }
    }
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    // Mostrar errores por snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Mostrar mensajes de éxito por snackbar
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---------- General ----------
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("General", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCurrencyDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Moneda predeterminada", fontSize = 16.sp)
                                Text(
                                    text = "${currency.name} (${currency.symbol})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.chevronRight(),
                                contentDescription = "Cambiar moneda",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tope de cuentas abiertas", fontSize = 16.sp)
                                Text(
                                    text = "Máximo de cuentas que puedes tener abiertas a la vez",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                maxAccountsInput = settings?.maxOpenAccounts?.toString() ?: "10"
                                showMaxAccountsDialog = true
                            }) {
                                Text(
                                    text = settings?.maxOpenAccounts?.toString() ?: "10",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ---------- Apariencia ----------
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Apariencia", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showThemeDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tema", fontSize = 16.sp)
                                Text(
                                    text = themeModeLabel(themeMode),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.chevronRight(),
                                contentDescription = "Cambiar tema",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ---------- Seguridad ----------
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Seguridad", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "PIN y biometría",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.lock(),
                                contentDescription = "Seguridad",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("PIN de inicio", fontSize = 16.sp)
                                Text(
                                    text = if (hasPin)
                                        "Configurado. Se pide al abrir la app"
                                    else
                                        "No configurado",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (hasPin) {
                                TextButton(onClick = {
                                    newPin = ""
                                    confirmPin = ""
                                    pinDialogError = null
                                    showPinDialog = true
                                }) {
                                    Text("Cambiar")
                                }
                                TextButton(onClick = { showRemovePinConfirm = true }) {
                                    Text("Quitar")
                                }
                            } else {
                                TextButton(onClick = {
                                    newPin = ""
                                    confirmPin = ""
                                    pinDialogError = null
                                    showPinDialog = true
                                }) {
                                    Text("Configurar")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Desbloqueo con huella", fontSize = 16.sp)
                                Text(
                                    text = when {
                                        !hasPin -> "Configura un PIN primero"
                                        !biometricAvailable -> "No disponible en el dispositivo"
                                        else -> "Usa tu huella para desbloquear"
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { enabled ->
                                    scope.launch { viewModel.setBiometricEnabled(enabled) }
                                },
                                enabled = hasPin && biometricAvailable
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bloqueo automático", fontSize = 16.sp)
                                Text(
                                    text = if (hasPin)
                                        "Pide el PIN al volver a la app"
                                    else
                                        "Configura un PIN primero",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings?.autoLockEnabled == true,
                                onCheckedChange = { enabled ->
                                    scope.launch { viewModel.updateAutoLockEnabled(enabled) }
                                },
                                enabled = hasPin
                            )
                        }

                        if (settings?.autoLockEnabled == true) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAutoLockDelayDialog = true },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Bloquear tras", fontSize = 16.sp)
                                    Text(
                                        text = autoLockDelayLabel(settings?.autoLockDelayMinutes ?: 0),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.chevronRight(),
                                    contentDescription = "Cambiar tiempo de bloqueo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ---------- Montos rápidos ----------
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Montos rápidos",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Valores predefinidos para entrada/salida rápido",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.toggleQuickAmounts() }) {
                                Icon(
                                    Icons.flashOn(),
                                    contentDescription = "Montos rápidos",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        if (showQuickAmounts) {
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            if (quickAmounts.isEmpty()) {
                                Text(
                                    text = "No hay montos rápidos configurados",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    quickAmounts.forEachIndexed { index, qa ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = qa.label.ifEmpty {
                                                            Money.format(qa.amountCents, currency)
                                                        },
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = Money.format(qa.amountCents, currency),
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(onClick = {
                                                        editingQuickAmount = qa
                                                        editLabel = qa.label
                                                        editValue = Money.format(qa.amountCents, currency)
                                                    }) {
                                                        Icon(Icons.edit(), contentDescription = "Editar")
                                                    }
                                                    IconButton(onClick = { showDeleteConfirm = qa }) {
                                                        Icon(
                                                            Icons.delete(),
                                                            contentDescription = "Eliminar",
                                                            tint = Color.Red
                                                        )
                                                    }
                                                    if (index > 0) {
                                                        IconButton(onClick = {
                                                            val ids = quickAmounts.map { it.id }.toMutableList()
                                                            ids.removeAt(index)
                                                            ids.add(index - 1, qa.id)
                                                            scope.launch { viewModel.reorderQuickAmounts(ids) }
                                                        }) {
                                                            Icon(
                                                                Icons.keyboardArrowUp(),
                                                                contentDescription = "Subir"
                                                            )
                                                        }
                                                    }
                                                    if (index < quickAmounts.size - 1) {
                                                        IconButton(onClick = {
                                                            val ids = quickAmounts.map { it.id }.toMutableList()
                                                            ids.removeAt(index)
                                                            ids.add(index + 1, qa.id)
                                                            scope.launch { viewModel.reorderQuickAmounts(ids) }
                                                        }) {
                                                            Icon(
                                                                Icons.keyboardArrowDown(),
                                                                contentDescription = "Bajar"
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Botón agregar SIEMPRE visible cuando showQuickAmounts = true
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    newQuickAmountLabel = ""
                                    newQuickAmountValue = ""
                                    showAddQuickAmountDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.add(), contentDescription = "Agregar")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Agregar monto rápido")
                                }
                            }
                        }
                    }
                }

                // ---------- Respaldo ----------
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Respaldo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Guarda o restaura todas tus cuentas y movimientos en un archivo JSON",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                exportBackupLauncher.launch(viewModel.suggestedBackupFileName())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("Exportar respaldo")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                importBackupLauncher.launch(arrayOf("application/json"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("Importar respaldo")
                        }
                    }
                }

                // ---------- Acerca de ----------
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Acerca de", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Cuentas Claras",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Versión ${BuildConfig.VERSION_NAME} " +
                                        "(build ${BuildConfig.VERSION_CODE})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Registra tus ingresos y gastos de forma simple. " +
                                "Tus datos se guardan solo en este dispositivo.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "© 2026 Cuentas Claras",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // ---------- Diálogo agregar ----------
    if (showAddQuickAmountDialog) {
        AlertDialog(
            onDismissRequest = { showAddQuickAmountDialog = false },
            title = { Text("Nuevo monto rápido") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newQuickAmountLabel,
                        onValueChange = { newQuickAmountLabel = it },
                        label = { Text("Etiqueta (opcional)") },
                        placeholder = { Text("Ej: Café, Transporte") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newQuickAmountValue,
                        onValueChange = { newQuickAmountValue = it },
                        label = { Text("Monto") },
                        placeholder = { Text("0,00") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newQuickAmountValue.isNotEmpty()) {
                            val amountCents = Money.parse(newQuickAmountValue, currency)
                            if (amountCents > 0) {
                                val quickAmount = QuickAmount(
                                    amountCents = amountCents,
                                    label = newQuickAmountLabel
                                )
                                scope.launch {
                                    val result = viewModel.addQuickAmount(quickAmount)
                                    if (result.isSuccess()) {
                                        showAddQuickAmountDialog = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = newQuickAmountValue.isNotEmpty()
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                Button(onClick = { showAddQuickAmountDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo editar ----------
    if (editingQuickAmount != null) {
        AlertDialog(
            onDismissRequest = { editingQuickAmount = null },
            title = { Text("Editar monto rápido") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editLabel,
                        onValueChange = { editLabel = it },
                        label = { Text("Etiqueta (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = { editValue = it },
                        label = { Text("Monto") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val current = editingQuickAmount ?: return@Button
                        if (editValue.isNotEmpty()) {
                            val amountCents = Money.parse(editValue, currency)
                            if (amountCents > 0) {
                                val updated = current.copy(
                                    amountCents = amountCents,
                                    label = editLabel
                                )
                                scope.launch {
                                    val result = viewModel.updateQuickAmount(updated)
                                    if (result.isSuccess()) {
                                        editingQuickAmount = null
                                    }
                                }
                            }
                        }
                    },
                    enabled = editValue.isNotEmpty()
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { editingQuickAmount = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo eliminar ----------
    showDeleteConfirm?.let { qaToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Eliminar monto rápido") },
            text = {
                Text(
                    "¿Eliminar \"${
                        qaToDelete.label.ifEmpty {
                            Money.format(qaToDelete.amountCents, currency)
                        }
                    }\"?",
                    modifier = Modifier.padding(8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.deleteQuickAmount(qaToDelete.id)
                            showDeleteConfirm = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirm = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo configurar/cambiar PIN ----------
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(if (hasPin) "Cambiar PIN" else "Configurar PIN") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Usa de 4 a 6 dígitos. El PIN se pedirá cada vez que abras la app.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { input ->
                            newPin = input.filter { it.isDigit() }.take(6)
                            pinDialogError = null
                        },
                        label = { Text("Nuevo PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        isError = pinDialogError != null
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { input ->
                            confirmPin = input.filter { it.isDigit() }.take(6)
                            pinDialogError = null
                        },
                        label = { Text("Confirmar PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        isError = pinDialogError != null
                    )
                    pinDialogError?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            newPin.length < 4 -> pinDialogError = "El PIN debe tener al menos 4 dígitos"
                            newPin.length > 6 -> pinDialogError = "El PIN no puede tener más de 6 dígitos"
                            newPin != confirmPin -> pinDialogError = "Los PIN no coinciden"
                            else -> {
                                scope.launch {
                                    val result = viewModel.setPin(newPin)
                                    if (result.isSuccess()) {
                                        showPinDialog = false
                                        recoveryCodeToShow = result.getOrNull()
                                        showRecoveryCodeDialog = true
                                    }
                                }
                            }
                        }
                    },
                    enabled = newPin.isNotEmpty() && confirmPin.isNotEmpty() && !isLoading
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { showPinDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo: código de recuperación (se muestra una sola vez) ----------
    if (showRecoveryCodeDialog) {
        val clipboard = LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { showRecoveryCodeDialog = false },
            title = { Text("Código de recuperación") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Guarda este código en un lugar seguro. Es la única forma de " +
                            "recuperar el acceso si olvidas tu PIN. No se volverá a mostrar.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = recoveryCodeToShow.orEmpty(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(
                        onClick = {
                            recoveryCodeToShow?.let {
                                clipboard.setText(AnnotatedString(it))
                            }
                        }
                    ) {
                        Text("Copiar código")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showRecoveryCodeDialog = false }) {
                    Text("Ya lo guardé")
                }
            }
        )
    }

    // ---------- Confirmar eliminar PIN ----------
    if (showRemovePinConfirm) {
        AlertDialog(
            onDismissRequest = { showRemovePinConfirm = false },
            title = { Text("Eliminar PIN") },
            text = {
                Text(
                    "La app ya no pedirá PIN al abrirse.",
                    modifier = Modifier.padding(8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.removePin()
                            showRemovePinConfirm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showRemovePinConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo tope de cuentas abiertas ----------
    if (showMaxAccountsDialog) {
        AlertDialog(
            onDismissRequest = { showMaxAccountsDialog = false },
            title = { Text("Tope de cuentas abiertas") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Indica cuántas cuentas pueden estar abiertas a la vez. " +
                            "Al alcanzar el tope deberás cerrar una para crear otra.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = maxAccountsInput,
                        onValueChange = { input ->
                            maxAccountsInput = input.filter { it.isDigit() }.take(2)
                        },
                        label = { Text("Máximo") },
                        placeholder = { Text("10") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val value = maxAccountsInput.toIntOrNull()
                        if (value != null && value >= 1) {
                            scope.launch {
                                viewModel.updateMaxOpenAccounts(value)
                                showMaxAccountsDialog = false
                            }
                        }
                    },
                    enabled = maxAccountsInput.toIntOrNull()?.let { it >= 1 } == true && !isLoading
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { showMaxAccountsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo moneda predeterminada ----------
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Moneda predeterminada") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Se usará en los montos rápidos y como sugerencia al crear cuentas.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Currency.all.forEach { c ->
                        val selected = c.code == currency.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        viewModel.updateCurrencyCode(c.code)
                                        showCurrencyDialog = false
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${c.name} (${c.symbol})",
                                    fontSize = 15.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (selected) {
                                Icon(
                                    Icons.check(),
                                    contentDescription = "Seleccionada",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo tema ----------
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Tema") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Settings.ThemeMode.values().forEach { mode ->
                        val selected = mode == themeMode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        viewModel.updateThemeMode(mode)
                                        showThemeDialog = false
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = themeModeLabel(mode),
                                    fontSize = 15.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (selected) {
                                Icon(
                                    Icons.check(),
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo tiempo de bloqueo ----------
    if (showAutoLockDelayDialog) {
        val options = listOf(0 to "Inmediato", 1 to "1 minuto", 5 to "5 minutos", 15 to "15 minutos", 30 to "30 minutos")
        val current = settings?.autoLockDelayMinutes ?: 0
        AlertDialog(
            onDismissRequest = { showAutoLockDelayDialog = false },
            title = { Text("Bloquear tras") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    options.forEach { (minutes, label) ->
                        val selected = minutes == current
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        viewModel.updateAutoLockDelay(minutes)
                                        showAutoLockDelayDialog = false
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    fontSize = 15.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (selected) {
                                Icon(
                                    Icons.check(),
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAutoLockDelayDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Confirmar importación de respaldo ----------
    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirm = false
                pendingImportUri = null
            },
            title = { Text("Importar respaldo") },
            text = {
                Text(
                    "Se reemplazarán TODAS tus cuentas y movimientos actuales por los del " +
                        "archivo seleccionado. Esta acción no se puede deshacer.",
                    modifier = Modifier.padding(8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingImportUri
                        showImportConfirm = false
                        pendingImportUri = null
                        if (uri != null) {
                            scope.launch { viewModel.importBackupFrom(uri) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Reemplazar", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = {
                    showImportConfirm = false
                    pendingImportUri = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun autoLockDelayLabel(minutes: Int): String = when (minutes) {
    0 -> "Inmediato"
    1 -> "1 minuto"
    60 -> "1 hora"
    else -> "$minutes minutos"
}

private fun themeModeLabel(mode: Settings.ThemeMode): String = when (mode) {
    Settings.ThemeMode.LIGHT -> "Claro"
    Settings.ThemeMode.DARK -> "Oscuro"
    Settings.ThemeMode.SYSTEM -> "Sistema"
}