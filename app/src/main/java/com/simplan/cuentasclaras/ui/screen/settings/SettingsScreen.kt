package com.simplan.cuentasclaras.ui.screen.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplan.cuentasclaras.common.Currency
import com.simplan.cuentasclaras.common.Money
import com.simplan.cuentasclaras.domain.entity.QuickAmount
import com.simplan.cuentasclaras.ui.Icons
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val quickAmounts by viewModel.quickAmounts.collectAsStateWithLifecycle(emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val error by viewModel.error.collectAsStateWithLifecycle(null)
    val showQuickAmounts by viewModel.showQuickAmounts.collectAsStateWithLifecycle(false)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddQuickAmountDialog by remember { mutableStateOf(false) }
    var newQuickAmountLabel by remember { mutableStateOf("") }
    var newQuickAmountValue by remember { mutableStateOf("") }
    var editingQuickAmount by remember { mutableStateOf<QuickAmount?>(null) }
    var editLabel by remember { mutableStateOf("") }
    var editValue by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf<QuickAmount?>(null) }

    val currency = settings?.currency ?: Currency.PEN

    // Mostrar errores por snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---------- General ----------
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("General", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Moneda predeterminada", fontSize = 16.sp)
                                Text(
                                    text = "${currency.name} (${currency.symbol})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                            Column {
                                Text("Idioma", fontSize = 16.sp)
                                Text(
                                    text = "Sistema",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.language(),
                                contentDescription = "Idioma",
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
                            Column {
                                Text("PIN de inicio", fontSize = 16.sp)
                                Text(
                                    text = if (settings?.pinHash?.isNotBlank() == true)
                                        "Configurado" else "No configurado",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { /* TODO: Navigate to PIN setup */ }) {
                                Text(
                                    if (settings?.pinHash?.isNotBlank() == true)
                                        "Cambiar" else "Configurar"
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
                                    text = "Valores predefinidos para ingreso/salida rápido",
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
}