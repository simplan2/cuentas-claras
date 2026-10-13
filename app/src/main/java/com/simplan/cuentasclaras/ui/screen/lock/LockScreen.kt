package com.simplan.cuentasclaras.ui.screen.lock

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
    val lockoutRemaining by viewModel.lockoutRemainingMillis.collectAsStateWithLifecycle(0L)
    val locked = lockoutRemaining > 0L

    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current as? FragmentActivity

    // ---------- Estado de recuperación ----------
    var showForgotDialog by remember { mutableStateOf(false) }
    var showRecoveryCodeDialog by remember { mutableStateOf(false) }
    var recoveryCodeInput by remember { mutableStateOf("") }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showWipeConfirm by remember { mutableStateOf(false) }
    var wipeConfirmText by remember { mutableStateOf("") }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirm = true
        }
    }

    // Verificar automáticamente cuando se completa el PIN
    LaunchedEffect(pin, pinLength) {
        if (!locked && pin.isNotEmpty() && pin.length >= pinLength) {
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

                    // ---------- Error / Bloqueo ----------
                    if (locked) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Demasiados intentos. Espera ${(lockoutRemaining + 999) / 1000}s",
                            fontSize = 14.sp,
                            color = Color.Red
                        )
                    } else {
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
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---------- Teclado numérico 3x4 ----------
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Fila 1: 1 2 3
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PinButton("1", enabled = !locked) { viewModel.addDigit("1") }
                            PinButton("2", enabled = !locked) { viewModel.addDigit("2") }
                            PinButton("3", enabled = !locked) { viewModel.addDigit("3") }
                        }
                        // Fila 2: 4 5 6
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PinButton("4", enabled = !locked) { viewModel.addDigit("4") }
                            PinButton("5", enabled = !locked) { viewModel.addDigit("5") }
                            PinButton("6", enabled = !locked) { viewModel.addDigit("6") }
                        }
                        // Fila 3: 7 8 9
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PinButton("7", enabled = !locked) { viewModel.addDigit("7") }
                            PinButton("8", enabled = !locked) { viewModel.addDigit("8") }
                            PinButton("9", enabled = !locked) { viewModel.addDigit("9") }
                        }
                        // Fila 4: ⌫ 0 C
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PinButton(
                                icon = { Icon(Icons.backspace(), contentDescription = "Borrar") },
                                enabled = !locked,
                                onClick = { viewModel.removeDigit() }
                            )
                            PinButton("0", enabled = !locked) { viewModel.addDigit("0") }
                            PinButton("C", enabled = !locked) { viewModel.clearPin() }
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

                    // ---------- ¿Olvidé mi PIN? ----------
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { showForgotDialog = true },
                        enabled = !isLoading
                    ) {
                        Text("¿Olvidé mi PIN?")
                    }
                }
            }
        }
    }

    // ---------- Diálogo: opciones de recuperación ----------
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Recuperar acceso") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Elige cómo quieres recuperar el acceso. Como olvidaste tu PIN, " +
                            "se eliminará y deberás configurar uno nuevo en Ajustes.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            showForgotDialog = false
                            recoveryCodeInput = ""
                            viewModel.clearError()
                            showRecoveryCodeDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Usar código de recuperación")
                    }
                    Button(
                        onClick = {
                            showForgotDialog = false
                            restoreLauncher.launch(arrayOf("application/json"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restaurar desde un respaldo")
                    }
                    Button(
                        onClick = {
                            showForgotDialog = false
                            wipeConfirmText = ""
                            viewModel.clearError()
                            showWipeConfirm = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Borrar todos los datos", color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo: código de recuperación ----------
    if (showRecoveryCodeDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryCodeDialog = false },
            title = { Text("Código de recuperación") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ingresa el código que guardaste al configurar el PIN " +
                            "(formato XXXX-XXXX-XXXX).",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = recoveryCodeInput,
                        onValueChange = {
                            recoveryCodeInput = it.uppercase().take(14)
                            viewModel.clearError()
                        },
                        label = { Text("Código") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )
                    error?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.recoverWithCode(recoveryCodeInput) {
                                showRecoveryCodeDialog = false
                                onUnlocked()
                            }
                        }
                    },
                    enabled = recoveryCodeInput.isNotBlank() && !isLoading
                ) {
                    Text("Desbloquear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecoveryCodeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo: confirmar restauración ----------
    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirm = false
                pendingRestoreUri = null
            },
            title = { Text("Restaurar respaldo") },
            text = {
                Text(
                    "Se reemplazarán TODOS los datos actuales por los del respaldo. " +
                        "Esta acción no se puede deshacer.",
                    modifier = Modifier.padding(8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingRestoreUri
                        showRestoreConfirm = false
                        pendingRestoreUri = null
                        if (uri != null) {
                            scope.launch {
                                viewModel.restoreFromBackup(uri) { onUnlocked() }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Restaurar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreConfirm = false
                    pendingRestoreUri = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ---------- Diálogo: confirmar borrado total ----------
    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            title = { Text("Borrar todos los datos") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Se borrarán todas tus cuentas, movimientos y el PIN. " +
                            "Esta acción no se puede deshacer. Escribe BORRAR para confirmar.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = wipeConfirmText,
                        onValueChange = { wipeConfirmText = it.take(10) },
                        label = { Text("Escribe BORRAR") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.wipeEverything {
                                showWipeConfirm = false
                                onUnlocked()
                            }
                        }
                    },
                    enabled = wipeConfirmText.trim().equals("BORRAR", ignoreCase = true) && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Borrar todo", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
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
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
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
