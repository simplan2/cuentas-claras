package com.simplan.cuentasclaras.ui.screen.accountmanager

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplan.cuentasclaras.common.Currency
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.ui.Icons
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagerScreen(
    onNavigateToAccountDetail: (Long) -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AccountManagerViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle(emptyList())
    val defaultAccountId by viewModel.defaultAccountId.collectAsStateWithLifecycle(0L)
    val maxOpenAccounts by viewModel.maxOpenAccounts.collectAsStateWithLifecycle(10)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val error by viewModel.error.collectAsStateWithLifecycle(null)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var accountToClose by remember { mutableStateOf<Account?>(null) }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val openAccounts = accounts.filter { it.isOpen }
    val closedAccounts = accounts.filter { !it.isOpen }
    val canCreate = !isLoading && openAccounts.size < maxOpenAccounts

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar cuentas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(
                            imageVector = Icons.arrowBack(),
                            contentDescription = "Volver al inicio"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- Resumen y creación ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cuentas abiertas",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${openAccounts.size} de $maxOpenAccounts",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        enabled = canCreate,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.add(), contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Crear cuenta")
                    }
                    if (!canCreate && !isLoading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Alcanzaste el tope de $maxOpenAccounts cuentas abiertas. " +
                                "Cierra o elimina una cuenta para crear otra.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // ---------- Cuentas abiertas ----------
            Text(
                text = "Cuentas abiertas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (openAccounts.isEmpty()) {
                Text(
                    text = "Sin cuentas registradas",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            openAccounts.forEach { account ->
                val isCurrent = account.id == defaultAccountId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAccountDetail(account.id) },
                    colors = if (isCurrent) {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    } else {
                        CardDefaults.cardColors()
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = Currency.getByCode(account.currencyCode).let {
                                        "${it.name} (${it.symbol})"
                                    },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isCurrent) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "En uso",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                onNavigateToAccountDetail(account.id)
                            }) {
                                Text("Administrar")
                            }
                            if (!isCurrent) {
                                TextButton(onClick = {
                                    scope.launch { viewModel.setDefaultAccount(account.id) }
                                }) {
                                    Text("Usar esta cuenta")
                                }
                            }
                            TextButton(onClick = { accountToClose = account }) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }

            // ---------- Cuentas cerradas ----------
            if (closedAccounts.isNotEmpty()) {
                Text(
                    text = "Cuentas cerradas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                closedAccounts.forEach { account ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = Currency.getByCode(account.currencyCode).code,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = {
                                scope.launch { viewModel.openAccount(account.id) }
                            }) {
                                Text("Reabrir")
                            }
                            TextButton(onClick = {
                                onNavigateToAccountDetail(account.id)
                            }) {
                                Text("Detalle")
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------- Diálogo crear cuenta ----------
    if (showCreateDialog) {
        CreateAccountDialog(
            canCreate = canCreate,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, currencyCode ->
                scope.launch {
                    val result = viewModel.createAccount(
                        Account(name = name, currencyCode = currencyCode)
                    )
                    if (result.isSuccess()) {
                        showCreateDialog = false
                    }
                }
            }
        )
    }

    // ---------- Confirmar cierre de cuenta ----------
    accountToClose?.let { account ->
        AlertDialog(
            onDismissRequest = { accountToClose = null },
            title = { Text("Cerrar cuenta") },
            text = {
                Text("¿Cerrar \"${account.name}\"? Podrás reabrirlo desde la lista de cuentas cerradas.")
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { viewModel.closeAccount(account.id) }
                    accountToClose = null
                }) {
                    Text("Cerrar cuenta")
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToClose = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CreateAccountDialog(
    canCreate: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, currencyCode: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var currencyCode by remember { mutableStateOf("PEN") }
    var expanded by remember { mutableStateOf(false) }

    val selectedCurrency = Currency.all.find { it.code == currencyCode } ?: Currency.PEN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva cuenta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la cuenta") },
                    placeholder = { Text("Ej: Cuenta principal, Ahorros...") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = "${selectedCurrency.name} (${selectedCurrency.symbol})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Moneda") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.expandMore(),
                                contentDescription = "Seleccionar moneda",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable { expanded = true }
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
                        Currency.all.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text("${currency.name} (${currency.symbol})") },
                                onClick = {
                                    currencyCode = currency.code
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name.trim(), currencyCode) },
                enabled = canCreate && name.trim().isNotEmpty()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
