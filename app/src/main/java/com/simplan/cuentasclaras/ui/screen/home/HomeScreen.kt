package com.simplan.cuentasclaras.ui.screen.home

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplan.cuentasclaras.common.Currency
import com.simplan.cuentasclaras.common.Money
import com.simplan.cuentasclaras.ui.Icons
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToAccountManager: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val total by viewModel.total.collectAsStateWithLifecycle(0L)
    val incomeTotal by viewModel.incomeTotal.collectAsStateWithLifecycle(0L)
    val expenseTotal by viewModel.expenseTotal.collectAsStateWithLifecycle(0L)
    val quickAmounts by viewModel.quickAmounts.collectAsStateWithLifecycle(emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val error by viewModel.error.collectAsStateWithLifecycle(null)
    val amountText by viewModel.amountText.collectAsStateWithLifecycle("")
    val conceptText by viewModel.conceptText.collectAsStateWithLifecycle("")
    val isIncome by viewModel.isIncome.collectAsStateWithLifecycle(true)

    val scope = rememberCoroutineScope()

    val currency = account?.currency ?: Currency.PEN
    val formattedTotal = Money.format(total, currency)
    val formattedIncome = Money.format(incomeTotal, currency)
    val formattedExpense = Money.format(expenseTotal, currency)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuentas Claras") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.settings(), contentDescription = "Ajustes")
                    }
                    IconButton(onClick = onNavigateToAccountManager) {
                        Icon(Icons.swapHoriz(), contentDescription = "Administrar cuentas")
                    }
                }
            )
        }
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
                // ---------- Tarjeta resumen de cuenta ----------
                account?.let { acc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = acc.name,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = formattedTotal,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.trendingUp(),
                                        contentDescription = "Ingresos",
                                        tint = Color.Green
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formattedIncome,
                                        fontSize = 14.sp,
                                        color = Color.Green,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Ingresos",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.trendingDown(),
                                        contentDescription = "Gastos",
                                        tint = Color.Red
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formattedExpense,
                                        fontSize = 14.sp,
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Gastos",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // ---------- Tarjeta de registro ----------
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isIncome) "Ingresar monto" else "Sacar monto",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (isIncome) "Ingreso" else "Gasto", fontSize = 16.sp)
                            Switch(
                                checked = isIncome,
                                onCheckedChange = { viewModel.setIsIncome(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = if (isIncome) Color.Green else Color.Red,
                                    checkedTrackColor = if (isIncome)
                                        Color.Green.copy(alpha = 0.3f)
                                    else
                                        Color.Red.copy(alpha = 0.3f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { viewModel.setAmountText(it) },
                            label = { Text("Monto") },
                            placeholder = { Text("0,00") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = conceptText,
                            onValueChange = { viewModel.setConceptText(it) },
                            label = { Text("Concepto") },
                            placeholder = { Text("Ej: Compras, Sueldo, Transporte...") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Montos rápidos:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Normalizamos la lista de montos rápidos para evitar duplicados
                        val defaultAmounts = listOf(500L, 1000L, 2000L, 5000L)
                        val quickCents = quickAmounts.map { it.amountCents }.toSet()
                        val extraDefaults = defaultAmounts.filter { it !in quickCents }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickAmounts.forEach { qa ->
                                val qaFormatted = Money.format(qa.amountCents, currency)
                                Button(
                                    onClick = { viewModel.onQuickAmountClick(qa.amountCents) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    contentPadding = ButtonDefaults.ContentPadding,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        text = qa.label.ifEmpty { qaFormatted },
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            extraDefaults.forEach { amountCents ->
                                val formatted = Money.format(amountCents, currency)
                                Button(
                                    onClick = { viewModel.onQuickAmountClick(amountCents) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    contentPadding = ButtonDefaults.ContentPadding,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(formatted, fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (!isLoading) {
                                    scope.launch {
                                        viewModel.addEntry()
                                        // Si addEntry() devuelve Result<*>, puedes hacer:
                                        // val result = viewModel.addEntry()
                                        // if (result.isFailure) { /* opcional */ }
                                    }
                                }
                            },
                            enabled = !isLoading &&
                                    amountText.trim().isNotEmpty() &&
                                    conceptText.trim().isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIncome) Color.Green else Color.Red,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = if (isIncome) "Agregar ingreso" else "Registrar gasto",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ---------- Error ----------
                error?.let { errorMsg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = errorMsg,
                                color = Color.Red,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    Icons.close(),
                                    contentDescription = "Cerrar",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}