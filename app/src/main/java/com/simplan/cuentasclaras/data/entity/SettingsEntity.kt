package com.simplan.cuentasclaras.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simplan.cuentasclaras.domain.entity.Settings
import kotlinx.serialization.Serializable

@Entity(tableName = "settings")
@Serializable
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val themeMode: String = "SYSTEM",
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
    val backupFrequency: String = "DAILY",
    val lastBackupAt: Long = 0,
    val exportFormat: String = "CSV",
    val showAmountInList: Boolean = true,
    val groupByDate: Boolean = true,
    val defaultAccountId: Long = 0
) {
    fun toDomain(): Settings = Settings(
        themeMode = Settings.ThemeMode.valueOf(themeMode),
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
        backupFrequency = Settings.BackupFrequency.valueOf(backupFrequency),
        lastBackupAt = lastBackupAt,
        exportFormat = Settings.ExportFormat.valueOf(exportFormat),
        showAmountInList = showAmountInList,
        groupByDate = groupByDate,
        defaultAccountId = defaultAccountId
    )

    companion object {
        fun fromDomain(settings: Settings): SettingsEntity = SettingsEntity(
            id = 1,
            themeMode = settings.themeMode.name,
            primaryColor = settings.primaryColor,
            accentColor = settings.accentColor,
            maxOpenAccounts = settings.maxOpenAccounts,
            quickEntryEnabled = settings.quickEntryEnabled,
            autoLockEnabled = settings.autoLockEnabled,
            autoLockDelayMinutes = settings.autoLockDelayMinutes,
            biometricEnabled = settings.biometricEnabled,
            pinHash = settings.pinHash,
            currencyCode = settings.currencyCode,
            language = settings.language,
            backupEnabled = settings.backupEnabled,
            backupFrequency = settings.backupFrequency.name,
            lastBackupAt = settings.lastBackupAt,
            exportFormat = settings.exportFormat.name,
            showAmountInList = settings.showAmountInList,
            groupByDate = settings.groupByDate,
            defaultAccountId = settings.defaultAccountId
        )
    }
}