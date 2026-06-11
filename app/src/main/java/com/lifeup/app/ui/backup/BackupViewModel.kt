package com.lifeup.app.ui.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.export.CsvExporter
import com.lifeup.app.data.export.DataExporter
import com.lifeup.app.data.preferences.SettingsPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val lastBackupDate: String? = null,
    val totalRecordsCount: Int = 0,
    val databaseSize: Long = 0L,
    val message: String? = null,
    val importSuccess: Boolean? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val dataExporter: DataExporter,
    private val csvExporter: CsvExporter,
    private val settingsPrefs: SettingsPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        loadBackupInfo()
    }

    private fun loadBackupInfo() {
        viewModelScope.launch {
            settingsPrefs.getLastBackupDate().collect { date ->
                _uiState.update { it.copy(lastBackupDate = date) }
            }
        }
        viewModelScope.launch {
            val count = dataExporter.getTotalRecordsCount()
            val size = dataExporter.getDatabaseSize()
            _uiState.update { it.copy(totalRecordsCount = count, databaseSize = size) }
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, message = null) }
            try {
                val file = dataExporter.exportToJson()
                val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault()))
                settingsPrefs.setLastBackupDate(now)
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        message = "JSON导出成功: ${file.absolutePath}",
                        lastBackupDate = now
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false, message = "导出失败: ${e.message}")
                }
            }
        }
    }

    fun exportJsonToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, message = null) }
            try {
                val tempFile = dataExporter.exportToJson()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    tempFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                tempFile.delete()
                val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault()))
                settingsPrefs.setLastBackupDate(now)
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        message = "JSON导出成功",
                        lastBackupDate = now
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false, message = "导出失败: ${e.message}")
                }
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, message = null) }
            try {
                val file = csvExporter.exportCsv()
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        message = "CSV导出成功: ${file.absolutePath}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false, message = "导出失败: ${e.message}")
                }
            }
        }
    }

    fun importJson(file: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, message = null, importSuccess = null) }
            try {
                val success = dataExporter.importFromJson(file)
                if (success) {
                    val count = dataExporter.getTotalRecordsCount()
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importSuccess = true,
                            message = "导入成功，共恢复 $count 条记录",
                            totalRecordsCount = count,
                            databaseSize = dataExporter.getDatabaseSize()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importSuccess = false,
                            message = "导入失败：文件格式不正确"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importSuccess = false,
                        message = "导入失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun importJsonFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, message = null, importSuccess = null) }
            try {
                val tempFile = File.createTempFile("lifeup_import_", ".json", context.cacheDir)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                val success = dataExporter.importFromJson(tempFile)
                tempFile.delete()
                if (success) {
                    val count = dataExporter.getTotalRecordsCount()
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importSuccess = true,
                            message = "导入成功，共恢复 $count 条记录",
                            totalRecordsCount = count,
                            databaseSize = dataExporter.getDatabaseSize()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importSuccess = false,
                            message = "导入失败：文件格式不正确"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importSuccess = false,
                        message = "导入失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, importSuccess = null) }
    }

    fun refreshInfo() {
        loadBackupInfo()
    }

    fun formatDatabaseSize(size: Long): String {
        return when {
            size >= 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
            size >= 1024 -> String.format("%.1f KB", size / 1024.0)
            else -> "$size B"
        }
    }
}
