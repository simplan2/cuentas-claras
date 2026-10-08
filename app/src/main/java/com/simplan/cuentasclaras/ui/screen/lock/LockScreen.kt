package com.simplan.cuentasclaras.ui.screen.lock

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplan.cuentasclaras.ui.Icons
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    viewModel: LockViewModel = hiltViewModel()
) {
    val pin by viewModel.pin.collectAsStateWithLifecycle("")
    val error by viewModel.error.collectAsStateWithLifecycle(null)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val showBiometric by viewModel.showBiometric.collectAsStateWithLifecycle(false)
    val failedAttempts by viewModel.failedAttempts.collectAsStateWithLifecycle(0)

    val scope = rememberCoroutineScope()

    // Desbloqueo automático cuando el PIN es correcto
    var unlocked by remember { mutableStateOf(false) }

    // Escuchar cambios de pin para desbloquear cuando el VM lo indique
    // (Si tu VM expone un StateFlow<Boolean> isUnlocked, úsalo aquí)
    // Por ahora, asumimos que el VM llama a onUnlocked vía authenticateWithBiometric o similar.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ---------- Icono + textos ----------
                    Icon(
                        imageVector = Icons.lock(),
                        contentDescription = "Bloqueado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Cuentas Claras",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingresa tu PIN para continuar",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---------- Puntos del PIN (6) ----------
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        (1..6).forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (index <= pin.length) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        }
                                    )
                            )
                        }
                    }

                    // ---------- Error ----------
                    error?.let { errorMsg ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMsg,
                            fontSize = 14.sp,
                            color = Color.Red
                        )
                    }

                    // ---------- Intentos fallidos ----------
                    if (failedAttempts > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Intentos fallidos: $failedAttempts",
                            fontSize = 12.sp,
                            color = Color.Red.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---------- Teclado numérico 3x4 ----------
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Fila 1: 1 2 3
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PinButton("1") { viewModel.addDigit("1") }
                            PinButton("2") { viewModel.addDigit("2") }
                            PinButton("3") { viewModel.addDigit("3") }
                        }
                        // Fila 2: 4 5 6
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PinButton("4") { viewModel.addDigit("4") }
                            PinButton("5") { viewModel.addDigit("5") }
                            PinButton("6") { viewModel.addDigit("6") }
                        }
                        // Fila 3: 7 8 9
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PinButton("7") { viewModel.addDigit("7") }
                            PinButton("8") { viewModel.addDigit("8") }
                            PinButton("9") { viewModel.addDigit("9") }
                        }
                        // Fila 4: ⌫ 0 C
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PinButton(
                                icon = { Icon(Icons.backspace(), contentDescription = "Borrar") },
                                onClick = { viewModel.removeDigit() }
                            )
                            PinButton("0") { viewModel.addDigit("0") }
                            PinButton("C") { viewModel.clearPin() }
                        }
                    }

                    // ---------- Botón biométrico ----------
                    if (showBiometric) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.authenticateWithBiometric(onUnlocked)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.fingerprint(), contentDescription = "Huella")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Usar huella dactilar")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Botón reutilizable del PIN pad.
 * Acepta texto o un icono.
 */
@Composable
private fun PinButton(
    text: String? = null,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        contentPadding = ButtonDefaults.ContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        when {
            icon != null -> icon()
            text != null -> Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}