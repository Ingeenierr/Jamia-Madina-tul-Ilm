package com.jamia.madinatulilm.ui.dashboard

import androidx.compose.runtime.Composable

@Composable
fun CanteenFinanceScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    ShopScreen(
        category = "CANTEEN", 
        viewModel = viewModel, 
        onNavigateBack = onNavigateBack
    )
}
