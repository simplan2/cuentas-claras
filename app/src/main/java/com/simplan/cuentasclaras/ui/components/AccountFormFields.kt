package com.simplan.cuentasclaras.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.simplan.cuentasclaras.common.Currency
import com.simplan.cuentasclaras.ui.Icons

@Composable
fun AccountFormFields(
    name: String,
    onNameChange: (String) -> Unit,
    currencyCode: String,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCurrency = Currency.all.find { it.code == currencyCode } ?: Currency.PEN

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre de la cuenta") },
            placeholder = { Text("Ej: Cuenta principal, Ahorros...") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
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
                            onCurrencyChange(currency.code)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}