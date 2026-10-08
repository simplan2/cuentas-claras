package com.simplan.cuentasclaras.ui.screen.accountdetail

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.domain.entity.Entry
import com.simplan.cuentasclaras.domain.usecase.account.AccountUseCases
import com.simplan.cuentasclaras.domain.usecase.entry.EntryUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val entryUseCases: EntryUseCases,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _account = MutableStateFlow<Account?>(null)
    val account = _account.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries = _entries.asStateFlow()

    private var entriesJob: Job? = null

    private var currentAccountId: Long = 0L

    private fun loadAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = accountUseCases.getAccount(currentAccountId)
            if (result.isSuccess()) {
                _account.value = result.getOrNull()
            } else {
                _error.value = result.errorMessage ?: "Error al cargar la cuenta"
            }
            _isLoading.value = false
        }
        startObservingEntries()
    }

    suspend fun updateAccount(account: Account): Result<Unit> {
        _isLoading.value = true
        val result = accountUseCases.updateAccount(account)
        if (result.isSuccess()) {
            _account.value = account
        } else {
            _error.value = result.errorMessage ?: "Error al actualizar"
        }
        _isLoading.value = false
        return result
    }

    suspend fun deleteAccount(): Result<Unit> {
        _isLoading.value = true
        val result = accountUseCases.deleteAccount(currentAccountId)
        _isLoading.value = false
        return result
    }

    suspend fun closeAccount(): Result<Unit> {
        _isLoading.value = true
        val result = accountUseCases.closeAccount(currentAccountId)
        if (result.isSuccess()) {
            _account.value = _account.value?.close()
        } else {
            _error.value = result.errorMessage ?: "Error al cerrar"
        }
        _isLoading.value = false
        return result
    }

    fun loadAccount(accountId: Long) {
        viewModelScope.launch {
            if (accountId == currentAccountId) return@launch
            currentAccountId = accountId
            _isLoading.value = true
            val result = accountUseCases.getAccount(currentAccountId)
            if (result.isSuccess()) {
                _account.value = result.getOrNull()
            } else {
                _error.value = result.errorMessage ?: "Error al cargar la cuenta"
            }
            _isLoading.value = false
        }
        startObservingEntries()
    }

    private fun startObservingEntries() {
        entriesJob?.cancel()
        entriesJob = viewModelScope.launch {
            entryUseCases.observeEntries(currentAccountId).collect { result ->
                if (result.isSuccess()) {
                    _entries.value = result.getOrThrow()
                }
            }
        }
    }

    suspend fun updateEntry(entry: Entry): Result<Unit> {
        _isLoading.value = true
        val result = entryUseCases.updateEntry(entry)
        if (result.isError()) {
            _error.value = result.errorMessage ?: "Error al actualizar el movimiento"
        }
        _isLoading.value = false
        return result
    }

    suspend fun deleteEntry(entryId: Long): Result<Unit> {
        _isLoading.value = true
        val result = entryUseCases.deleteEntry(entryId)
        if (result.isError()) {
            _error.value = result.errorMessage ?: "Error al eliminar el movimiento"
        }
        _isLoading.value = false
        return result
    }

    suspend fun exportAccount(): Result<Uri> {
        _isLoading.value = true
        val result = withContext(Dispatchers.IO) { buildExportFile() }
        _isLoading.value = false
        if (result.isError()) {
            _error.value = result.errorMessage ?: "Error al exportar la cuenta"
        }
        return result
    }

    private suspend fun buildExportFile(): Result<Uri> {
        val account = _account.value ?: return Result.error("Cuenta no encontrada")

        val entriesResult = entryUseCases.getEntries(account.id)
        if (entriesResult.isError()) {
            return Result.error(entriesResult.errorMessage ?: "Error al leer los movimientos")
        }
        val entries = entriesResult.getOrThrow()

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val safeName = account.name
            .replace(Regex("[^A-Za-z0-9]+"), "_")
            .trim('_')
            .ifEmpty { "cuenta" }
        val file = File(dir, "${safeName}_$timestamp.csv")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        val content = buildString {
            append('\uFEFF')
            append("Fecha;Tipo;Moneda;Monto;Nota\r\n")
            entries.sortedBy { it.date }.forEach { entry ->
                append(dateFormat.format(Date(entry.date)))
                append(';')
                append(if (entry.isIncome) "Ingreso" else "Egreso")
                append(';')
                append(account.currencyCode)
                append(';')
                append(csvAmount(entry.amountCents))
                append(';')
                append(csvEscape(entry.note))
                append("\r\n")
            }
        }
        file.writeText(content, Charsets.UTF_8)

        return runCatching {
            Result.success(
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            )
        }.getOrElse {
            Result.error("No se pudo preparar el archivo para compartir")
        }
    }

    private fun csvAmount(amountCents: Long): String =
        String.format(Locale.US, "%.2f", amountCents / 100.0).replace('.', ',')

    private fun csvEscape(value: String): String {
        val needsQuotes = value.any { it == ';' || it == '"' || it == '\n' || it == '\r' }
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }

    fun clearError() {
        _error.value = null
    }
}