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
    contentPadding: PaddingValues,
    columnsInfos: StaggeredColumnsInfo,
    state: StaggeredLazyColumnScrollState,
    provider: LazyLayoutItemProvider,
    result: MutableList<Pair<Placeable, StaggeredPlacement>>,
) {
    result.clear()
    state.visibleItemsController.onStartMeasure()

    val itemWidth = ((constraints.maxWidth - horizontalSpacing.toPx() * (columns - 1)) / columns).roundToInt()
    val itemConstraints = Constraints(
        minWidth = 0,
        maxWidth = itemWidth,
        minHeight = 0,
        maxHeight = Constraints.Infinity
    )
    val viewportTop: Int = state.value - contentPadding.calculateTopPadding().toPx().roundToInt()
    val viewportBottom: Int = viewportTop + constraints.maxHeight + contentPadding.calculateBottomPadding().toPx().roundToInt()

    val verticalSpacingPx = verticalSpacing.toPx().toInt()
    val firstVisibleItem = columnsInfos.getFirstVisible(viewportTop, verticalSpacingPx)
    val startIndex = firstVisibleItem?.index ?: columnsInfos.measuredItems
    for (index in startIndex until provider.itemCount) {
        val item = columnsInfos.items[index]
        if (item == null) {
            if (columnsInfos.minHeight() < viewportBottom) {
                val placeable = measure(index, itemConstraints).first()
                val column = columnsInfos.nextPlaceColumn()
                val left = column * itemWidth + column * horizontalSpacing.toPx().toInt()
                var top = columnsInfos.columnHeight(column)
                if (top > 0) {
                    top += verticalSpacingPx
                }
                val bottom = top + placeable.height
                val placeableAt = StaggeredPlacement(index = index, top = top, left = left, bottom = bottom)
                if (placeableAt.inViewPort(
                        spacingPx = verticalSpacingPx,
                        viewportTop = viewportTop,
                        viewportBottom = viewportBottom,
                    )
                ) {
                    state.visibleItemsController.addVisibleItem(placeableAt, provider)
                    result.add(placeable to placeableAt)
                }
                columnsInfos.add(column, placeableAt)
                if (top - verticalSpacingPx > viewportBottom) {
                    break
                }
            } else {
                break
            }
        } else {
            val placeable = measure(index, itemConstraints).first()
            if (
                item.inViewPort(
                    spacingPx = verticalSpacingPx,
                    viewportTop = viewportTop,
                    viewportBottom = viewportBottom,
                )
            ) {
                state.visibleItemsController.addVisibleItem(item, provider)
                result.add(placeable to item)
            }
            if (item.top - verticalSpacingPx > viewportBottom) {
                break
            }
        }
    }

    state.visibleItemsController.onEndMeasure(
        viewportWidth = constraints.maxWidth,
        viewportHeight = constraints.maxHeight,
        provider = provider,
        afterContentPadding = contentPadding.calculateTopPadding().toPx().roundToInt(),
        beforeContentPadding = contentPadding.calculateBottomPadding().toPx().roundToInt(),
    )

    if (columnsInfos.measuredItems == provider.itemCount) {
        state.maxValue = columnsInfos.maxHeight() - constraints.maxHeight
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
            return min(max(1, result), columns.maxColumns)
        }
    }
}
