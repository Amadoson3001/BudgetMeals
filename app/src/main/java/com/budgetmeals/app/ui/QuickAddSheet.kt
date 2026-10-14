package com.budgetmeals.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.budgetmeals.app.ui.icons.AppIcons
import com.budgetmeals.app.ui.theme.Spacing

/** Single row inside the quick-add sheet. */
@Composable
private fun QuickAddRow(
    icon: ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit,
    subtitle: String? = null,
) {
    SoftCard(onClick = onClick, contentPadding = Spacing.md) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, tint)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(AppIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

/** Content of the quick-add sheet opened from the floating button on Home. */
@Composable
fun QuickAddSheetContent(
    onPurchase: () -> Unit,
    onExpense: () -> Unit,
    onShoppingItem: () -> Unit,
    onMeal: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.screen)
            .padding(bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            "What happened?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = Spacing.xs),
        )
        QuickAddRow(
            icon = AppIcons.ShoppingBasket,
            title = "I bought food",
            tint = MaterialTheme.colorScheme.primary,
            onClick = onPurchase,
        )
        QuickAddRow(
            icon = AppIcons.ReceiptLong,
            title = "I spent on something else",
            tint = MaterialTheme.colorScheme.tertiary,
            onClick = onExpense,
        )
        QuickAddRow(
            icon = AppIcons.ShoppingCart,
            title = "Remind me to buy",
            tint = MaterialTheme.colorScheme.secondary,
            onClick = onShoppingItem,
        )
        QuickAddRow(
            icon = AppIcons.Restaurant,
            title = "Create a meal",
            tint = AccentPurple,
            onClick = onMeal,
        )
    }
}
