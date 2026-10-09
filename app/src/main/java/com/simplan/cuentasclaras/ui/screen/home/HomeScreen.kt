package com.simplan.cuentasclaras.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplan.cuentasclaras.common.Currency
import com.simplan.cuentasclaras.common.Money
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.domain.entity.QuickAmount
import com.simplan.cuentasclaras.ui.Icons
import kotlinx.coroutines.launch

private const val SAVE_IDLE = "idle"
private const val SAVE_SAVING = "saving"
private const val SAVE_SUCCESS = "success"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToAccountManager: () -> Unit,
    onNavigateToAccountDetail: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle(emptyList())
    val total by viewModel.total.collectAsStateWithLifecycle(0L)
    val incomeTotal by viewModel.incomeTotal.collectAsStateWithLifecycle(0L)
    val expenseTotal by viewModel.expenseTotal.collectAsStateWithLifecycle(0L)
    val quickAmounts by viewModel.quickAmounts.collectAsStateWithLifecycle(emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val justSaved by viewModel.justSaved.collectAsStateWithLifecycle(false)
    val error by viewModel.error.collectAsStateWithLifecycle(null)
    val amountText by viewModel.amountText.collectAsStateWithLifecycle("")
    val conceptText by viewModel.conceptText.collectAsStateWithLifecycle("")
    val isIncome by viewModel.isIncome.collectAsStateWithLifecycle(true)

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    var accountMenuExpanded by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    val currency = account?.currency ?: Currency.PEN
    val formattedTotal = Money.format(total, currency)
    val formattedIncome = Money.format(incomeTotal, currency)
    val formattedExpense = Money.format(expenseTotal, currency)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.wallet(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Cuentas Claras",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable(onClick = onNavigateToSettings)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.settings(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ajustes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---------- Contexto: saldo de la cuenta ----------
                account?.let { acc ->
                    AccountHeroCard(
                        account = acc,
                        accounts = accounts,
                        formattedTotal = formattedTotal,
                        formattedIncome = formattedIncome,
                        formattedExpense = formattedExpense,
                        menuExpanded = accountMenuExpanded,
                        onMenuExpandedChange = { accountMenuExpanded = it },
                        onSwitchAccount = { viewModel.switchAccount(it) },
                        onOpenAccountDetail = { onNavigateToAccountDetail(acc.id) }
                    )
                }

                // ---------- Acción principal: registrar movimiento ----------
                if (account != null) {
                    EntryCard(
                        currency = account!!.currency,
                        isIncome = isIncome,
                        setIsIncome = { viewModel.setIsIncome(it) },
                        amountText = amountText,
                        onAmountChange = { viewModel.setAmountText(it) },
                        conceptText = conceptText,
                        onConceptChange = { viewModel.setConceptText(it) },
                        quickAmounts = quickAmounts,
                        onQuickAmountClick = { viewModel.onQuickAmountClick(it) }
                    )

                    // ---------- Guardar (píldora igual a "Administrar cuentas") ----------
                    val saveState = when {
                        isLoading -> SAVE_SAVING
                        justSaved -> SAVE_SUCCESS
                        else -> SAVE_IDLE
                    }
                    val pillBackground by animateColorAsState(
                        targetValue = if (isIncome || justSaved) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        label = "pillBackground"
                    )
                    val pillContent = when {
                        isIncome || justSaved -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onError
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(pillBackground)
                            .clickable(enabled = !isLoading) {
                                scope.launch {
                                    val result = viewModel.addEntry()
                                    if (result.isError()) {
                                        snackbarHostState.showSnackbar(
                                            result.errorMessage
                                                ?: "No se pudo guardar el movimiento"
                                        )
                                    } else {
                                        focusManager.clearFocus()
                                        keyboard?.hide()
                                    }
                                }
                            }
                            .padding(vertical = 14.dp)
                    ) {
                        AnimatedContent(
                            targetState = saveState,
                            transitionSpec = {
                                if (targetState == SAVE_SAVING) {
                                    fadeIn() togetherWith fadeOut()
                                } else {
                                    (scaleIn(initialScale = 0.9f) + fadeIn()) togetherWith
                                        (scaleOut(targetScale = 0.9f) + fadeOut())
                                }
                            },
                            label = "estadoGuardar"
                        ) { state ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (state) {
                                    SAVE_SAVING -> CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = pillContent
                                    )

                                    SAVE_SUCCESS -> Icon(
                                        imageVector = Icons.check(),
                                        contentDescription = null,
                                        tint = pillContent,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    else -> Icon(
                                        imageVector = Icons.add(),
                                        contentDescription = null,
                                        tint = pillContent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (state == SAVE_SUCCESS) "Guardado" else "Guardar",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = pillContent
                                )
                            }
                        }
                    }

                    // ---------- Vinculo secundario (misma pildora que "Ajustes") ----------
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable(onClick = onNavigateToAccountManager)
                            .padding(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.wallet(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Administrar cuentas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                } else {
                    // ---------- Sin cuenta activa ----------
                    NoAccountCard(onManageAccounts = onNavigateToAccountManager)
                }

                // ---------- Error ----------
                error?.let { errorMsg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
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
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    Icons.close(),
                                    contentDescription = "Cerrar",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountHeroCard(
    account: Account,
    accounts: List<Account>,
    formattedTotal: String,
    formattedIncome: String,
    formattedExpense: String,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onSwitchAccount: (Long) -> Unit,
    onOpenAccountDetail: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val heroBackground = if (dark) {
        listOf(Color(0xFF0B2F28), Color(0xFF155B46), Color(0xFF1D7B57))
    } else {
        listOf(Color(0xFF0B7A57), Color(0xFF0F9570), Color(0xFF27B383))
    }
    val onHero = Color.White

    var lastTotalKey by remember { mutableStateOf<String?>(null) }
    val totalScale = remember { Animatable(1f) }
    LaunchedEffect(formattedTotal) {
        val prev = lastTotalKey
        lastTotalKey = formattedTotal
        if (prev != null && prev != formattedTotal) {
            totalScale.snapTo(1f)
            totalScale.animateTo(1.05f, tween(150))
            totalScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(heroBackground))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.extraLarge
            )
    ) {
        Icon(
            imageVector = Icons.accountBalanceWallet(),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.05f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(190.dp)
                .offset(x = 46.dp, y = (-42).dp)
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.14f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.accountBalanceWallet(),
                        contentDescription = null,
                        tint = onHero,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = onHero,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable(onClick = onOpenAccountDetail)
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detalles",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.chevronRight(),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (accounts.size > 1) {
                    Box {
                        IconButton(
                            onClick = { onMenuExpandedChange(true) }
                        ) {
                            Icon(
                                imageVector = Icons.swapHoriz(),
                                contentDescription = "Cambiar de cuenta",
                                tint = onHero,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { onMenuExpandedChange(false) }
                        ) {
                            accounts.forEach { other ->
                                val isActive = other.id == account.id
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = other.name,
                                                fontWeight = if (isActive) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Normal
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (other.isOpen) {
                                                    "${other.currency.name} · Abierta"
                                                } else {
                                                    "${other.currency.name} · Cerrada"
                                                },
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (!isActive) {
                                            onSwitchAccount(other.id)
                                        }
                                        onMenuExpandedChange(false)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isActive) {
                                                Icons.checkCircle()
                                            } else {
                                                Icons.accountBalanceWallet()
                                            },
                                            contentDescription = null,
                                            tint = if (isActive) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Saldo total",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formattedTotal,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = onHero,
                modifier = Modifier.graphicsLayer {
                    scaleX = totalScale.value
                    scaleY = totalScale.value
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.trendingUp(),
                    iconColor = Color(0xFFA8FFDC),
                    amount = formattedIncome,
                    label = "Entradas"
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.trendingDown(),
                    iconColor = Color(0xFFFFB4AB),
                    amount = formattedExpense,
                    label = "Salidas"
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    amount: String,
    label: String
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.70f)
            )
            Text(
                text = amount,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EntryCard(
    currency: Currency,
    isIncome: Boolean,
    setIsIncome: (Boolean) -> Unit,
    amountText: String,
    onAmountChange: (String) -> Unit,
    conceptText: String,
    onConceptChange: (String) -> Unit,
    quickAmounts: List<QuickAmount>,
    onQuickAmountClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Registrar movimiento",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isIncome) "Ingreso de dinero" else "Gasto de dinero",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(4.dp)
            ) {
                SegmentButton(
                    modifier = Modifier.weight(1f),
                    text = "Entrada",
                    selected = isIncome,
                    selectedColor = if (isIncome) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    onSelectedColor = if (isIncome) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    onClick = { setIsIncome(true) }
                )
                SegmentButton(
                    modifier = Modifier.weight(1f),
                    text = "Salida",
                    selected = !isIncome,
                    selectedColor = if (!isIncome) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    onSelectedColor = if (!isIncome) {
                        MaterialTheme.colorScheme.onError
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    onClick = { setIsIncome(false) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = onAmountChange,
                label = { Text("Monto") },
                placeholder = { Text("0,00") },
                prefix = { Text("${currency.symbol} ") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                colors = fieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = conceptText,
                onValueChange = onConceptChange,
                label = { Text("Concepto") },
                placeholder = { Text("Ej: Compras, Sueldo, Transporte...") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                colors = fieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Montos rápidos",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            val quickCents = quickAmounts.map { it.amountCents }.toSet()
            val extraDefaults = listOf(500L, 1000L, 2000L, 5000L)
                .filter { it !in quickCents }
                .map { amountCents ->
                    QuickAmount(
                        label = "",
                        amountCents = amountCents,
                        isEnabled = true
                    )
                }
            val shownAmounts = quickAmounts + extraDefaults

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                shownAmounts.chunked(2).forEach { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        chunk.forEach { qa ->
                            val qaFormatted = Money.format(qa.amountCents, currency)
                            QuickAmountButton(
                                modifier = Modifier.weight(1f),
                                label = qa.label.ifEmpty { qaFormatted },
                                onClick = { onQuickAmountClick(qa.amountCents) }
                            )
                        }
                        if (chunk.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun SegmentButton(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    selectedColor: Color,
    onSelectedColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .background(
                color = if (selected) selectedColor else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = if (selected) onSelectedColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickAmountButton(
    modifier: Modifier,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NoAccountCard(onManageAccounts: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.accountBalanceWallet(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay cuenta abierta",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Abre una cuenta cerrada o crea una nueva para " +
                    "empezar a registrar entradas y salidas.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onManageAccounts,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Administrar cuentas")
            }
        }
    }
}