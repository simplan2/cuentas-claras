package com.simplan.cuentasclaras.ui.screen.lock

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
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
    val pinLength by viewModel.pinLength.collectAsStateWithLifecycle(4)
    val error by viewModel.error.collectAsStateWithLifecycle(null)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val showBiometric by viewModel.showBiometric.collectAsStateWithLifecycle(false)
    val failedAttempts by viewModel.failedAttempts.collectAsStateWithLifecycle(0)

    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current as? FragmentActivity

    // Verificar automáticamente cuando se completa el PIN
    LaunchedEffect(pin, pinLength) {
        if (pin.isNotEmpty() && pin.length >= pinLength) {
            viewModel.verifyPin(onUnlocked)
        }
    }

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
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ---------- Salir ----------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                activity?.finishAndRemoveTask()
                                activity?.finishAffinity()
                            }
                        ) {
                            Text("Salir")
                        }
                    }

                    // ---------- Icono + textos ----------
                    Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.lock(),
                        contentDescription = "Bloqueado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

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

                    // ---------- Puntos del PIN (según longitud real) ----------
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        (1..pinLength).forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        if (index <= pin.length) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = CircleShape
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
                                if (activity != null) {
                                    val executor = ContextCompat.getMainExecutor(activity)
                                    val prompt = BiometricPrompt(
                                        activity,
                                        executor,
                                        object : BiometricPrompt.AuthenticationCallback() {
                                            override fun onAuthenticationSucceeded(
                                                result: BiometricPrompt.AuthenticationResult
                                            ) {
                                                viewModel.onBiometricSuccess(onUnlocked)
                                            }

                                            override fun onAuthenticationError(
                                                errorCode: Int,
                                                errString: CharSequence
                                            ) {
                                                viewModel.onBiometricError(
                                                    errorCode,
                                                    errString.toString()
                                                )
                                            }
                                        }
                                    )
                                    prompt.authenticate(
                                        BiometricPrompt.PromptInfo.Builder()
                                            .setTitle("Desbloquear Cuentas Claras")
                                            .setSubtitle("Usa tu huella para continuar")
                                            .setNegativeButtonText("Usar PIN")
                                            .setAllowedAuthenticators(
                                                BiometricManager.Authenticators.BIOMETRIC_STRONG
                                            )
                                            .build()
                                    )
                                } else {
                                    scope.launch {
                                        viewModel.authenticateWithBiometric(onUnlocked)
                                    }
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
        modifier = Modifier.size(64.dp),
        shape = CircleShape,
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
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
