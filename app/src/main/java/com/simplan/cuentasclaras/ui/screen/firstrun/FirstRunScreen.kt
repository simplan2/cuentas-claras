package com.simplan.cuentasclaras.ui.screen.firstrun

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplan.cuentasclaras.common.Currency
import com.simplan.cuentasclaras.ui.Icons
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstRunScreen(
    onAccountCreated: () -> Unit,
    viewModel: FirstRunViewModel = hiltViewModel()
) {
    val currencyCode by viewModel.currencyCode.collectAsStateWithLifecycle("PEN")
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val error by viewModel.error.collectAsStateWithLifecycle(null)

    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var nameText by remember { mutableStateOf("") }
    val currencies = Currency.all

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido a Cuentas Claras",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crea tu primera cuenta para empezar",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { newText ->
                            nameText = newText
                            viewModel.setName(newText)
                        },
                        label = { Text("Nombre de la cuenta") },
                        placeholder = { Text("Ej: Cuenta principal, Ahorros, etc.") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val selectedCurrency =
                        currencies.find { it.code == currencyCode } ?: Currency.PEN

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
                                        viewModel.setCurrencyCode(currency.code)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            error?.let { errorMsg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMsg,
                    fontSize = 14.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!isLoading) {
                        scope.launch {
                            val result = viewModel.createAccount()
                            // Si createAccount() devuelve kotlin.Result<T>, usa result.isSuccess (sin paréntesis)
                            if (result.isSuccess()) {
                                onAccountCreated()
                            }
                        }
                    }
                },
                enabled = !isLoading && nameText.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear cuenta", fontSize = 16.sp)
                }
            }
        }
    }
}