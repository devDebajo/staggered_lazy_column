package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

internal fun PaddingValues.calculateHorizontalPadding(layoutDirection: LayoutDirection = LayoutDirection.Ltr): Dp {
    return calculateLeftPadding(layoutDirection) + calculateRightPadding(layoutDirection)
}
