package com.simplan.cuentasclaras.data.backup

import android.content.Context
import android.net.Uri
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.BackupPayload
import com.simplan.cuentasclaras.domain.usecase.backup.BackupUseCases
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupFileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupUseCases: BackupUseCases
) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    fun suggestedFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "cuentasclaras_backup_$stamp.json"
    }

    suspend fun exportTo(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = backupUseCases.export()
            if (payload.isError()) {
                return@withContext Result.error(payload.errorMessage ?: "Error al preparar el respaldo")
            }
            val content = json.encodeToString(BackupPayload.serializer(), payload.getOrThrow())
            context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
                output.write(content.toByteArray(Charsets.UTF_8))
                output.flush()
            } ?: return@withContext Result.error("No se pudo abrir el archivo destino")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e, "No se pudo exportar el respaldo")
        }
    }

    suspend fun importFrom(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            } ?: return@withContext Result.error("No se pudo leer el archivo de respaldo")
            val payload = json.decodeFromString(BackupPayload.serializer(), content)
            val imported = backupUseCases.import(payload)
            if (imported.isError()) {
                Result.error(imported.errorMessage ?: "Error al importar el respaldo")
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e, "El archivo no es un respaldo válido")
        }
    }
}
