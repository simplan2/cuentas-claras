package com.simplan.cuentasclaras.ui.screen.accountdetail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplan.cuentasclaras.common.Currency
import com.simplan.cuentasclaras.ui.Icons
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AccountDetailViewModel = hiltViewModel()
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val error by viewModel.error.collectAsStateWithLifecycle(null)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editCurrency by remember { mutableStateOf("PEN") }
    var editIsOpen by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showExportConfirm by remember { mutableStateOf(false) }

    val currencies = Currency.all

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
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.accountBalanceWallet(),
                                        contentDescription = "Cuenta",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = acc.name,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                if (acc.isDefault) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "Predeterminada",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

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
                                    text = "Estado: ",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Switch(
                                    checked = acc.isOpen,
                                    onCheckedChange = null,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Green,
                                        checkedTrackColor = Color.Green.copy(alpha = 0.3f)
                                    )
                                )
                                Text(
                                    text = if (acc.isOpen) "Abierta" else "Cerrada",
                                    fontSize = 16.sp,
                                    color = if (acc.isOpen) Color.Green else Color.Red,
                                    fontWeight = FontWeight.Medium
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
                                        Locale.getDefault()
                                    ).format(Date(acc.createdAt)),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
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
                                    onClick = { showExportConfirm = true },
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

                            if (!acc.isDefault) {
                                Button(
                                    onClick = { showDeleteConfirm = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.delete(), contentDescription = "Eliminar")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Eliminar cuenta")
                                }
                            } else {
                                Text(
                                    text = "Esta es la cuenta predeterminada. Selecciona otra como predeterminada para poder eliminarla.",
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

                    // ---------- Info adicional ----------
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Información adicional",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "ID de la cuenta",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "#${acc.id}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Orden",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${acc.sortOrder}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
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
                                checkedThumbColor = Color.Green,
                                checkedTrackColor = Color.Green.copy(alpha = 0.3f)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
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
    if (showExportConfirm) {
        AlertDialog(
            onDismissRequest = { showExportConfirm = false },
            title = { Text("Exportar cuenta") },
            text = {
                Text("Función de exportación en desarrollo.", modifier = Modifier.padding(8.dp))
            },
            confirmButton = {
                Button(onClick = { showExportConfirm = false }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(onClick = { showExportConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}