package com.simplan.cuentasclaras.ui.screen.accounthistory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplan.cuentasclaras.common.Currency
import com.simplan.cuentasclaras.common.Money
import com.simplan.cuentasclaras.domain.entity.Category
import com.simplan.cuentasclaras.domain.entity.Entry
import com.simplan.cuentasclaras.ui.Icons
import java.text.SimpleDateFormat
import java.util.Date

private val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountHistoryScreen(
    accountId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AccountHistoryViewModel = hiltViewModel()
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val year by viewModel.year.collectAsStateWithLifecycle()
    val monthlyTotals by viewModel.monthlyTotals.collectAsStateWithLifecycle(emptyList())
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle(emptyList())
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle("")
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle(EntryTypeFilter.ALL)
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle(null)
    val filteredEntries by viewModel.filteredEntries.collectAsStateWithLifecycle(emptyList())
    val error by viewModel.error.collectAsStateWithLifecycle(null)

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val currency = account?.currency ?: Currency.PEN

    LaunchedEffect(accountId) {
        viewModel.load(accountId)
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val symbol = currency.symbol
    val totalIncome = monthlyTotals.sumOf { it.income }
    val totalExpense = monthlyTotals.sumOf { it.expense }
    val netTotal = totalIncome - totalExpense

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                account?.let { acc ->
                    // ---------- Encabezado ----------
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
                                Text(
                                    text = acc.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.changeYear(-1) },
                                        enabled = year > 1970
                                    ) {
                                        Icon(
                                            Icons.chevronLeft(),
                                            contentDescription = "Año anterior",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Text(
                                        text = year.toString(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    IconButton(onClick = { viewModel.changeYear(1) }) {
                                        Icon(
                                            Icons.chevronRight(),
                                            contentDescription = "Año siguiente",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Montos en ${currency.code} (${symbol})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ResumenTotal(
                                label = "Entradas",
                                amount = totalIncome,
                                color = MaterialTheme.colorScheme.primary,
                                symbol = symbol,
                                currency = currency
                            )
                            ResumenTotal(
                                label = "Salidas",
                                amount = totalExpense,
                                color = MaterialTheme.colorScheme.error,
                                symbol = symbol,
                                currency = currency
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ResumenTotal(
                                label = "Neto",
                                amount = netTotal,
                                color = if (netTotal >= 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error,
                                symbol = symbol,
                                currency = currency,
                                bold = true
                            )
                        }
                    }

                    // ---------- Resumen mensual ----------
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mes",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Entradas",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Salidas",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Neto",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            HorizontalDivider()
                            MONTH_NAMES.forEachIndexed { index, name ->
                                val month = index + 1
                                val monthly = monthlyTotals.firstOrNull { it.month.toIntOrNull() == month }
                                val monthlyIncome = monthly?.income ?: 0L
                                val monthlyExpense = monthly?.expense ?: 0L
                                val balance = monthlyIncome - monthlyExpense
                                val isSelected = selectedMonth == month

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else
                                                Color.Transparent
                                        )
                                        .clickable {
                                            viewModel.selectMonth(if (isSelected) null else month)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = compactAmount(monthlyIncome, symbol, currency),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = compactAmount(monthlyExpense, symbol, currency),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = signedCompactAmount(balance, symbol, currency),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (balance >= 0)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (index < MONTH_NAMES.size - 1) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }

                    // ---------- Movimientos del mes seleccionado ----------
                    selectedMonth?.let { month ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Movimientos · ${MONTH_NAMES[month - 1]} $year",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    placeholder = { Text("Buscar por concepto") },
                                    leadingIcon = {
                                        Icon(Icons.search(), contentDescription = null)
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = { viewModel.setSearchQuery("") }
                                            ) {
                                                Icon(
                                                    Icons.close(),
                                                    contentDescription = "Limpiar"
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    EntryTypeFilter.entries.forEach { type ->
                                        FilterChip(
                                            selected = typeFilter == type,
                                            onClick = { viewModel.setTypeFilter(type) },
                                            label = { Text(type.label) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = categoryFilter == null,
                                        onClick = { viewModel.setCategoryFilter(null) },
                                        label = { Text("Todas") }
                                    )
                                    Category.selectable.forEach { category ->
                                        FilterChip(
                                            selected = categoryFilter == category.id,
                                            onClick = {
                                                viewModel.setCategoryFilter(category.id)
                                            },
                                            label = { Text(category.label) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                if (filteredEntries.isEmpty()) {
                                    Text(
                                        text = if (entries.isEmpty())
                                            "Sin movimientos en este mes"
                                        else
                                            "Sin resultados para el filtro",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                                filteredEntries.sortedByDescending { it.date }.forEach { entry ->
                                    MovimientoFila(
                                        entry = entry,
                                        dateFormat = dateFormat,
                                        currency = currency,
                                        symbol = symbol
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenTotal(
    label: String,
    amount: Long,
    color: Color,
    symbol: String,
    currency: Currency,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Text(
            text = compactAmount(amount, symbol, currency),
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun MovimientoFila(
    entry: Entry,
    dateFormat: SimpleDateFormat,
    currency: Currency,
    symbol: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateFormat.format(Date(entry.date)),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = entry.note.ifBlank { "Sin concepto" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Category.labelOf(entry.categoryId)?.let { categoryLabel ->
                Text(
                    text = categoryLabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = signedCompactAmount(
                if (entry.isIncome) entry.amountCents else -entry.amountCents,
                symbol,
                currency
            ),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (entry.isIncome)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
    }
}

private fun compactAmount(cents: Long, symbol: String, currency: Currency): String =
    Money.format(cents, currency)
        .replace(symbol, "")
        .trim()
        .ifEmpty { "0" }

private fun signedCompactAmount(cents: Long, symbol: String, currency: Currency): String {
    val sign = when {
        cents > 0 -> "+"
        cents < 0 -> "-"
        else -> ""
    }
    return sign + compactAmount(kotlin.math.abs(cents), symbol, currency)
}