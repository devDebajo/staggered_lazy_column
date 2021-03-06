package ru.debajo.staggeredlazycolumn.calculation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import ru.debajo.staggeredlazycolumn.StaggeredLazyColumnCells
import ru.debajo.staggeredlazycolumn.state.StaggeredLazyColumnScrollState
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@ExperimentalFoundationApi
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

    val startPadding = contentPadding.calculateStartPadding(layoutDirection).roundToPx()
    val endPadding = contentPadding.calculateEndPadding(layoutDirection).roundToPx()
    val topPadding = contentPadding.calculateTopPadding().roundToPx()
    columnsInfos.topOffsetPx = topPadding
    val bottomPadding = contentPadding.calculateBottomPadding().roundToPx()
    val totalHorizontalPadding = startPadding + endPadding

    val availableWidth = constraints.maxWidth - totalHorizontalPadding
    val itemWidth = ((availableWidth - horizontalSpacing.toPx() * (columns - 1)) / columns).roundToInt()
    val itemConstraints = Constraints(
        minWidth = 0,
        maxWidth = itemWidth,
        minHeight = 0,
        maxHeight = Constraints.Infinity
    )
    var viewportTop: Int = state.scroll
    var viewportBottom: Int = viewportTop + constraints.maxHeight

    val verticalSpacingPx = verticalSpacing.roundToPx()
    val firstVisibleItem = columnsInfos.getFirstVisible(viewportTop, verticalSpacingPx)
    val startIndex = firstVisibleItem?.index ?: columnsInfos.measuredItems
    for (index in startIndex until provider.itemCount) {
        val item = columnsInfos.items[index]
        if (item == null) {
            if (columnsInfos.minHeight() < viewportBottom || !state.firstHandled) {
                val placeable = measure(index, itemConstraints).first()
                val column = columnsInfos.nextPlaceColumn()
                val left = column * itemWidth + column * horizontalSpacing.roundToPx() + startPadding
                var top = columnsInfos.columnHeight(column)
                val itemTopOffset = if (top > 0) verticalSpacingPx else 0
                top += itemTopOffset
                val bottom = top + placeable.height
                val placeableAt = StaggeredPlacement(index = index, top = top, left = left, bottom = bottom, topOffset = itemTopOffset)
                if (
                    placeableAt.inViewPort(
                        viewportTop = viewportTop,
                        viewportBottom = viewportBottom,
                    )
                ) {
                    state.visibleItemsController.addVisibleItem(placeableAt, provider)
                    result.add(placeable to placeableAt)
                }
                columnsInfos.add(column, placeableAt)

                if (!state.firstHandled) {
                    if (placeableAt.index == state.firstVisibleItemIndexInner) {
                        state.firstHandled = true
                        state.setScroll(placeableAt.top - state.firstVisibleItemScrollOffsetInner)
                        viewportTop = state.scroll
                        viewportBottom = viewportTop + constraints.maxHeight
                        continue
                    }
                }

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

    val firstElement = result.minByOrNull { it.second.index }
    if (firstElement != null) {
        state.firstVisibleItemIndex = firstElement.second.index
        state.firstVisibleItemScrollOffset = viewportTop - firstElement.second.top
    } else {
        state.firstVisibleItemIndex = 0
        state.firstVisibleItemScrollOffset = 0
    }

    state.visibleItemsController.onEndMeasure(
        viewportWidth = constraints.maxWidth,
        viewportHeight = constraints.maxHeight,
        provider = provider,
        afterContentPadding = topPadding,
        beforeContentPadding = bottomPadding,
    )

    state.maxValue = when {
        provider.itemCount == 0 -> 0
        columnsInfos.measuredItems == provider.itemCount -> {
            (columnsInfos.maxHeight() - constraints.maxHeight + bottomPadding).coerceAtLeast(0)
        }
        columnsInfos.measuredItems < provider.itemCount -> Int.MAX_VALUE
        else -> state.maxValue
    }
}

// Should not call every measure
@ExperimentalFoundationApi
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
            val itemMinWidthPx = columns.minWidth.roundToPx()
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
