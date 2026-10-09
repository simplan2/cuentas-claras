package com.simplan.cuentasclaras.ui.screen.accountdetail

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.domain.entity.Entry
import com.simplan.cuentasclaras.domain.usecase.account.AccountUseCases
import com.simplan.cuentasclaras.domain.usecase.entry.EntryUseCases
import com.simplan.cuentasclaras.domain.usecase.settings.SettingsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val entryUseCases: EntryUseCases,
    private val settingsUseCases: SettingsUseCases,
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
        val wasDefault = _account.value?.isDefault == true
        val result = accountUseCases.deleteAccount(currentAccountId)
        if (result.isSuccess()) {
            if (wasDefault) {
                val nextDefault = accountUseCases.getAllAccounts().getOrNull()
                    ?.firstOrNull { it.id != currentAccountId && it.isOpen }
                if (nextDefault != null) {
                    accountUseCases.setDefaultAccount(nextDefault.id)
                } else {
                    settingsUseCases.getSettings().getOrNull()?.let {
                        settingsUseCases.saveSettings(it.copyWith(defaultAccountId = 0L))
                    }
                }
            }
        } else {
            _error.value = result.errorMessage ?: "Error al eliminar la cuenta"
        }
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
                append(if (entry.isIncome) "Entrada" else "Salida")
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

    suspend fun exportAccountPdf(): Result<Uri> {
        _isLoading.value = true
        val result = withContext(Dispatchers.IO) { buildExportPdfFile() }
        _isLoading.value = false
        if (result.isError()) {
            _error.value = result.errorMessage ?: "Error al exportar en PDF"
        }
        return result
    }

    private suspend fun buildExportPdfFile(): Result<Uri> {
        val account = _account.value ?: return Result.error("Cuenta no encontrada")

        val entriesResult = entryUseCases.getEntries(account.id)
        if (entriesResult.isError()) {
            return Result.error(entriesResult.errorMessage ?: "Error al leer los movimientos")
        }
        val entries = entriesResult.getOrThrow().sortedBy { it.date }

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val safeName = account.name
            .replace(Regex("[^A-Za-z0-9]+"), "_")
            .trim('_')
            .ifEmpty { "cuenta" }
        val file = File(dir, "${safeName}_$timestamp.pdf")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40
        val colDate = margin + 10
        val colType = margin + 110
        val colAmount = margin + 230
        val colNote = margin + 340
        val rowHeight = 20

        val pdf = PdfDocument()
        try {
            var pageIndex = 1
            var page = pdf.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
            )
            var y = (margin + 30).toFloat()

            val titlePaint = Paint().apply {
                color = android.graphics.Color.rgb(102, 80, 164)
                textSize = 20f
                isFakeBoldText = true
            }
            val labelPaint = Paint().apply {
                color = android.graphics.Color.rgb(90, 90, 90)
                textSize = 11f
            }
            val textPaint = Paint().apply {
                color = android.graphics.Color.rgb(40, 40, 40)
                textSize = 11f
            }
            val headerPaint = Paint().apply {
                color = android.graphics.Color.rgb(230, 230, 230)
            }

            page.canvas.drawText("Cuentas Claras", margin.toFloat(), y, titlePaint)
            y += 18f
            page.canvas.drawText("Cuenta: ${account.name}", margin.toFloat(), y, labelPaint)
            y += 14f
            page.canvas.drawText(
                "Exportado: ${dateFormat.format(Date())}",
                margin.toFloat(),
                y,
                labelPaint
            )
            y += 12f

            page.canvas.drawRect(
                margin.toFloat(),
                y,
                (pageWidth - margin).toFloat(),
                y + rowHeight.toFloat(),
                headerPaint
            )
            page.canvas.drawText("Fecha", colDate.toFloat(), y + 14f, textPaint)
            page.canvas.drawText("Tipo", colType.toFloat(), y + 14f, textPaint)
            page.canvas.drawText("Monto", colAmount.toFloat(), y + 14f, textPaint)
            page.canvas.drawText("Nota", colNote.toFloat(), y + 14f, textPaint)
            y += (rowHeight + 6).toFloat()

            entries.forEach { entry ->
                if (y > (pageHeight - margin).toFloat()) {
                    pdf.finishPage(page)
                    pageIndex++
                    page = pdf.startPage(
                        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
                    )
                    y = (margin + 30).toFloat()
                }
                val canvas = page.canvas
                val type = if (entry.isIncome) "Entrada" else "Salida"
                val amountText = (if (entry.isIncome) "+" else "-") +
                    String.format(Locale.US, "%.2f", entry.amountCents / 100.0)
                canvas.drawText(
                    dateFormat.format(Date(entry.date)),
                    colDate.toFloat(),
                    y,
                    textPaint
                )
                canvas.drawText(type, colType.toFloat(), y, textPaint)
                canvas.drawText(amountText, colAmount.toFloat(), y, textPaint)
                val note = entry.note.ifBlank { "Sin concepto" }
                val clipped = if (note.length > 34) note.substring(0, 33) + "…" else note
                canvas.drawText(clipped, colNote.toFloat(), y, textPaint)
                y += rowHeight.toFloat()
            }
            pdf.finishPage(page)

            FileOutputStream(file).use { pdf.writeTo(it) }
        } catch (e: Exception) {
            return Result.error("No se pudo generar el PDF")
        } finally {
            pdf.close()
        }

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