package com.simplan.cuentasclaras.domain.entity

import com.simplan.cuentasclaras.common.Currency
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val primaryColor: Long = 0xFF2196F3.toLong(),
    val accentColor: Long = 0xFF03DAC6.toLong(),
    val maxOpenAccounts: Int = 10,
    val quickEntryEnabled: Boolean = true,
    val autoLockEnabled: Boolean = false,
    val autoLockDelayMinutes: Int = 5,
    val biometricEnabled: Boolean = false,
    val pinHash: String = "",
    val currencyCode: String = "PEN",
    val language: String = "system",
    val backupEnabled: Boolean = false,
    val backupFrequency: BackupFrequency = BackupFrequency.DAILY,
    val lastBackupAt: Long = 0L,
    val exportFormat: ExportFormat = ExportFormat.CSV,
    val showAmountInList: Boolean = true,
    val groupByDate: Boolean = true,
    val defaultAccountId: Long = 0L
) {
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }

    enum class BackupFrequency {
        NEVER, DAILY, WEEKLY, MONTHLY
    }

    enum class ExportFormat {
        CSV, PDF, IMAGE
    }

    val currency: Currency
        get() = Currency.getByCode(currencyCode)

    fun copyWith(
        themeMode: ThemeMode = this.themeMode,
        primaryColor: Long = this.primaryColor,
        accentColor: Long = this.accentColor,
        maxOpenAccounts: Int = this.maxOpenAccounts,
        quickEntryEnabled: Boolean = this.quickEntryEnabled,
        autoLockEnabled: Boolean = this.autoLockEnabled,
        autoLockDelayMinutes: Int = this.autoLockDelayMinutes,
        biometricEnabled: Boolean = this.biometricEnabled,
        pinHash: String = this.pinHash,
        currencyCode: String = this.currencyCode,
        language: String = this.language,
        backupEnabled: Boolean = this.backupEnabled,
        backupFrequency: BackupFrequency = this.backupFrequency,
        lastBackupAt: Long = this.lastBackupAt,
        exportFormat: ExportFormat = this.exportFormat,
        showAmountInList: Boolean = this.showAmountInList,
        groupByDate: Boolean = this.groupByDate,
        defaultAccountId: Long = this.defaultAccountId
    ): Settings = copy(
        themeMode = themeMode,
        primaryColor = primaryColor,
        accentColor = accentColor,
        maxOpenAccounts = maxOpenAccounts,
        quickEntryEnabled = quickEntryEnabled,
        autoLockEnabled = autoLockEnabled,
        autoLockDelayMinutes = autoLockDelayMinutes,
        biometricEnabled = biometricEnabled,
        pinHash = pinHash,
        currencyCode = currencyCode,
        language = language,
        backupEnabled = backupEnabled,
        backupFrequency = backupFrequency,
        lastBackupAt = lastBackupAt,
        exportFormat = exportFormat,
        showAmountInList = showAmountInList,
        groupByDate = groupByDate,
        defaultAccountId = defaultAccountId
    )
}