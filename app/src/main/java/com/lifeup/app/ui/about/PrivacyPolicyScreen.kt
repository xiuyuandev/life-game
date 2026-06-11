package com.lifeup.app.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("隐私政策") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "隐私政策",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                PolicySection(
                    title = "数据收集说明",
                    content = "本应用所有数据均存储在本地设备上，不会上传至任何远程服务器。我们尊重您的隐私，不收集任何个人身份信息。"
                )
            }

            item {
                PolicySection(
                    title = "数据存储",
                    content = "本应用使用 Room 数据库进行本地存储，所有数据保存在您的设备中。数据安全由 Android 系统沙盒机制保障。"
                )
            }

            item {
                PolicySection(
                    title = "数据备份",
                    content = "本应用支持 JSON 格式导出数据备份。云端备份功能需要用户主动配置第三方存储服务，本应用不会自动上传任何数据。"
                )
            }

            item {
                PolicySection(
                    title = "第三方服务",
                    content = "本应用不使用任何第三方分析或追踪服务，不集成任何广告 SDK，不会向第三方共享您的任何数据。"
                )
            }

            item {
                PolicySection(
                    title = "权限说明",
                    content = "本应用仅申请前台服务权限，用于在计时器运行时显示通知，确保计时功能在后台持续运行。不申请任何其他敏感权限。"
                )
            }

            item {
                PolicySection(
                    title = "数据删除",
                    content = "卸载本应用将删除所有本地数据。如需在卸载前保留数据，请先使用数据导出功能进行备份。"
                )
            }

            item {
                PolicySection(
                    title = "联系方式",
                    content = "如有任何隐私相关问题，请联系：lifeup@example.com"
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    content: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
}
