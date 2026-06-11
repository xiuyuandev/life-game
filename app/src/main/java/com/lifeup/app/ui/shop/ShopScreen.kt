package com.lifeup.app.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.ItemTier
import com.lifeup.app.data.db.SlotType

private val tierColorMap: Map<ItemTier, Color> = mapOf(
    ItemTier.COMMON to Color(0xFF9E9E9E),
    ItemTier.FINE to Color(0xFF66BB6A),
    ItemTier.RARE to Color(0xFF448AFF),
    ItemTier.EPIC to Color(0xFF7E57C2),
    ItemTier.LEGENDARY to Color(0xFFFFB300)
)

private val tierLabelMap: Map<ItemTier, String> = mapOf(
    ItemTier.COMMON to "普通",
    ItemTier.FINE to "精良",
    ItemTier.RARE to "稀有",
    ItemTier.EPIC to "史诗",
    ItemTier.LEGENDARY to "传说"
)

private val tierEmojiMap: Map<ItemTier, String> = mapOf(
    ItemTier.COMMON to "⚪",
    ItemTier.FINE to "🟢",
    ItemTier.RARE to "🔵",
    ItemTier.EPIC to "🟣",
    ItemTier.LEGENDARY to "🟡"
)

private val slotLabelMap: Map<SlotType, String> = mapOf(
    SlotType.HEAD to "头部",
    SlotType.BODY to "身体",
    SlotType.HANDS to "手部",
    SlotType.FEET to "脚部",
    SlotType.ACCESSORY to "饰品"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onNavigateBack: () -> Unit,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛒 金币商店", fontWeight = FontWeight.Bold)
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFB300).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFFFB300)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${uiState.goldBalance}G",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            ShopContent(
                uiState = uiState,
                onPurchaseClick = { viewModel.showPurchaseDialog(it) }
            )
        }
    }

    // Purchase confirmation dialog
    uiState.pendingPurchase?.let { template ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissPurchaseDialog() },
            title = {
                Text(
                    text = "确认购买",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tierEmojiMap[template.itemTier] ?: "",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = tierColorMap[template.itemTier] ?: MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = template.description)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Text(text = "价格：", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${template.price}G",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(text = "余额：", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${uiState.goldBalance}G",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    uiState.purchaseSuccess?.let { success ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (success) "购买成功！" else if (uiState.goldBalance < template.price) "金币不足" else "购买失败",
                            color = if (success) Color(0xFF66BB6A) else Color(0xFFFF5252),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                if (uiState.purchaseSuccess == null) {
                    Button(
                        onClick = { viewModel.purchaseItem(template) },
                        enabled = uiState.goldBalance >= template.price,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("购买")
                    }
                } else {
                    Button(
                        onClick = { viewModel.dismissPurchaseDialog() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("确定")
                    }
                }
            },
            dismissButton = {
                if (uiState.purchaseSuccess == null) {
                    TextButton(onClick = { viewModel.dismissPurchaseDialog() }) {
                        Text("取消")
                    }
                }
            }
        )
    }
}

@Composable
private fun ShopContent(
    uiState: ShopUiState,
    onPurchaseClick: (ItemTemplate) -> Unit
) {
    var selectedTierIndex by remember { mutableIntStateOf(0) }
    val tierTabs = listOf("全部") + ItemTier.entries.map { tierLabelMap[it] ?: it.name }

    val filteredItems = if (selectedTierIndex == 0) {
        uiState.shopItems
    } else {
        val selectedTier = ItemTier.entries[selectedTierIndex - 1]
        uiState.shopItems.filter { it.template.itemTier == selectedTier }
    }

    ScrollableTabRow(
        selectedTabIndex = selectedTierIndex,
        edgePadding = 0.dp
    ) {
        tierTabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTierIndex == index,
                onClick = { selectedTierIndex = index },
                text = {
                    Text(
                        text = title,
                        fontWeight = if (selectedTierIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (filteredItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "该分类下暂无商品",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = filteredItems,
                key = { it.template.name }
            ) { shopItem ->
                ShopItemCard(
                    shopItem = shopItem,
                    onPurchaseClick = onPurchaseClick
                )
            }

            // Bottom spacing
            item {
                Box(modifier = Modifier.padding(bottom = 72.dp))
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    shopItem: ShopItem,
    onPurchaseClick: (ItemTemplate) -> Unit
) {
    val template = shopItem.template
    val tierColor = tierColorMap[template.itemTier] ?: Color.Gray

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Top accent bar with tier color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                tierColor,
                                tierColor.copy(alpha = 0.2f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Tier badge + Slot label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tierEmojiMap[template.itemTier] ?: "",
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = tierColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tierLabelMap[template.itemTier] ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tierColor,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        text = slotLabelMap[template.slotType] ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Item name
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bonuses
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+${template.attributeBonus} 属性",
                        style = MaterialTheme.typography.bodySmall,
                        color = tierColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+${(template.expBonusContribution * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price + Purchase button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFFB300)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${template.price}G",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )
                    }

                    if (shopItem.isOwned) {
                        Text(
                            text = "已拥有",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    } else {
                        Button(
                            onClick = { onPurchaseClick(template) },
                            enabled = shopItem.canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tierColor,
                                disabledContainerColor = tierColor.copy(alpha = 0.3f)
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            ),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "购买",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
