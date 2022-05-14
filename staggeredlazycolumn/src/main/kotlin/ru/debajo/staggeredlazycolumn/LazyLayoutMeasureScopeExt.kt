package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyLayoutMeasureScope.prepareItemsToPlace(
    constraints: Constraints,
    columns: Int,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    columnsInfos: StaggeredColumnsInfo,
    state: StaggeredLazyColumnScrollState,
    provider: LazyLayoutItemProvider,
    result: MutableList<Pair<Placeable, StaggeredPlacement>>,
) {
    result.clear()
    val itemWidth = ((constraints.maxWidth - horizontalSpacing.toPx() * (columns - 1)) / columns).roundToInt()
    val itemConstraints = Constraints(
        minWidth = 0,
        maxWidth = itemWidth,
        minHeight = 0,
        maxHeight = Constraints.Infinity
    )
    val viewportTop = state.value
    val viewportBottom = viewportTop + constraints.maxHeight

    val firstVisibleItem = columnsInfos.getFirstVisible(viewportTop)
    val startIndex = firstVisibleItem?.index ?: columnsInfos.measuredItems
    var columnsMaxHeight = 0
    for (index in startIndex until provider.itemCount) {
        val item = columnsInfos.items[index]
        if (item == null) {
            val placeable = measure(index, itemConstraints).first()
            val column = columnsInfos.nextPlaceColumn()
            val left = column * itemWidth + column * horizontalSpacing.toPx().toInt()
            var top = columnsInfos.columnHeight(column)
            if (top > 0) {
                top += verticalSpacing.toPx().toInt()
            }
            val placeableAt = StaggeredPlacement(index = index, top = top, left = left, bottom = top + placeable.height)
            result.add(placeable to placeableAt)
            columnsInfos.add(column, placeableAt)
            columnsInfos.measuredItems++
            if (columnsInfos.minHeight() >= viewportBottom) {
                break
            }
        } else {
            val placeable = measure(index, itemConstraints).first()
            result.add(placeable to item)
            columnsMaxHeight = max(columnsMaxHeight, item.top)
            if (columnsMaxHeight >= viewportBottom) {
                break
            }
        }
        if (columnsInfos.measuredItems == provider.itemCount) {
            state.maxValue = columnsInfos.maxHeight() - constraints.maxHeight
        }
    }
}

// Should not call every measure
@OptIn(ExperimentalFoundationApi::class)
internal fun LazyLayoutMeasureScope.calculateColumnsCount(
    contentPadding: PaddingValues,
    horizontalSpacing: Dp,
    constraints: Constraints,
    columns: StaggeredLazyColumnCells,
): Int {
    return when (columns) {
        is StaggeredLazyColumnCells.Fixed -> columns.columns
        is StaggeredLazyColumnCells.Adaptive -> {
            var availableSpacePx = constraints.maxWidth - contentPadding.calculateHorizontalPadding(layoutDirection).toPx()
            val itemMinWidthPx = columns.minWidth.toPx().roundToInt()
            val spacingPx = horizontalSpacing.toPx()
            var result = 0
            do {
                availableSpacePx -= itemMinWidthPx
                availableSpacePx -= spacingPx
                result++
            } while (availableSpacePx > 0)
            return min(max(2, result), columns.maxColumns)
        }
    }
}
