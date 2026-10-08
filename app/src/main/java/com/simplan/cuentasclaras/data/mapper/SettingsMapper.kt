package com.simplan.cuentasclaras.data.mapper

import com.simplan.cuentasclaras.data.entity.SettingsEntity
import com.simplan.cuentasclaras.domain.entity.Settings

fun SettingsEntity.toDomain(): Settings = Settings(
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

fun Settings.toEntity(): SettingsEntity = SettingsEntity(
    id = 1,
    themeMode = themeMode.name,
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
    backupFrequency = backupFrequency.name,
    lastBackupAt = lastBackupAt,
    exportFormat = exportFormat.name,
    showAmountInList = showAmountInList,
    groupByDate = groupByDate,
    defaultAccountId = defaultAccountId
)