package com.jamia.madinatulilm.ui.dashboard

import androidx.compose.runtime.Composable

@Composable
fun MaktabFinanceScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    ShopScreen(
        category = "MAKTAB", 
        viewModel = viewModel, 
        onNavigateBack = onNavigateBack
    )
}
