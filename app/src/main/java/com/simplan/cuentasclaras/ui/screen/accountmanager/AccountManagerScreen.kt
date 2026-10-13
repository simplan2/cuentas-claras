package com.simplan.cuentasclaras.ui.screen.accountmanager

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplan.cuentasclaras.common.Currency
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.ui.Icons
import com.simplan.cuentasclaras.ui.components.AccountFormFields
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
    val defaultCurrencyCode by viewModel.defaultCurrencyCode.collectAsStateWithLifecycle("PEN")
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val error by viewModel.error.collectAsStateWithLifecycle(null)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showNoAccountsDialog by remember { mutableStateOf(false) }
    var accountToClose by remember { mutableStateOf<Account?>(null) }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Refrescar al volver a la pantalla (p. ej. tras eliminar todas las cuentas y crear una nueva)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    val openAccounts = accounts.filter { it.isOpen }
    val closedAccounts = accounts.filter { !it.isOpen }
    val canCreate = !isLoading && openAccounts.size < maxOpenAccounts

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar cuentas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                        text = "Administración de cuentas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${openAccounts.size} de $maxOpenAccounts cuentas abiertas",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        enabled = canCreate,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.add(), contentDescription = null)
                        Spacer(modifier = Modifier.size(4.dp))
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
                AccountCard(
                    account = account,
                    isCurrent = isCurrent,
                    onClick = { onNavigateToAccountDetail(account.id) },
                    onUse = {
                        if (!isCurrent) {
                            scope.launch { viewModel.setDefaultAccount(account.id) }
                        }
                    },
                    onClose = { accountToClose = account }
                )
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
                    ClosedAccountCard(
                        account = account,
                        onReopen = {
                            scope.launch { viewModel.openAccount(account.id) }
                        },
                        onDetail = { onNavigateToAccountDetail(account.id) }
                    )
                }
            }
        }
    }

    // ---------- Diálogo crear cuenta ----------
    if (showCreateDialog) {
        CreateAccountDialog(
            canCreate = canCreate,
            defaultCurrencyCode = defaultCurrencyCode,
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
                    accountToClose = null
                    scope.launch {
                        val result = viewModel.closeAccount(account.id)
                        if (result.isSuccess()) {
                            val accountsAfter = viewModel.accounts.value
                            if (accountsAfter.isEmpty()) {
                                onNavigateToHome()
                            } else if (accountsAfter.none { it.isOpen }) {
                                showNoAccountsDialog = true
                            }
                        }
                    }
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

    // ---------- Sin cuentas abiertas ----------
    if (showNoAccountsDialog) {
        AlertDialog(
            onDismissRequest = { showNoAccountsDialog = false },
            title = { Text("Sin cuentas abiertas") },
            text = {
                Text("Cerraste la última cuenta abierta. Crea una nueva para seguir " +
                    "registrando movimientos, o reabre una de las cerradas desde la lista.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showNoAccountsDialog = false
                    showCreateDialog = true
                }) {
                    Text("Crear nueva cuenta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoAccountsDialog = false }) {
                    Text("Reabrir después")
                }
            }
        )
    }
}

@Composable
private fun AccountCard(
    account: Account,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onUse: () -> Unit,
    onClose: () -> Unit
) {
    val tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = if (isCurrent) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        colors = if (isCurrent) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        },
        elevation = if (isCurrent) {
            CardDefaults.cardElevation(defaultElevation = 4.dp)
        } else {
            CardDefaults.cardElevation(defaultElevation = 1.dp)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color = tint.copy(alpha = 0.12f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.accountBalanceWallet(),
                            contentDescription = null,
                            tint = tint
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.check(),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "En uso",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.chevronRight(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!isCurrent) {
                        TextButton(onClick = onUse) {
                            Text("Usar esta cuenta")
                        }
                    }
                    TextButton(onClick = onClose) {
                        Text("Cerrar")
                    }
                }
            }
    }
}

@Composable
private fun ClosedAccountCard(
    account: Account,
    onReopen: () -> Unit,
    onDetail: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.accountBalanceWallet(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Cerrada · ${Currency.getByCode(account.currencyCode).code}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onReopen) {
                Text("Reabrir")
            }
            TextButton(onClick = onDetail) {
                Text("Detalle")
            }
        }
    }
}

@Composable
private fun CreateAccountDialog(
    canCreate: Boolean,
    defaultCurrencyCode: String,
    onDismiss: () -> Unit,
    onCreate: (name: String, currencyCode: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var currencyCode by remember { mutableStateOf(defaultCurrencyCode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva cuenta") },
        text = {
            AccountFormFields(
                name = name,
                onNameChange = { name = it },
                currencyCode = currencyCode,
                onCurrencyChange = { currencyCode = it },
                modifier = Modifier.fillMaxWidth()
            )
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