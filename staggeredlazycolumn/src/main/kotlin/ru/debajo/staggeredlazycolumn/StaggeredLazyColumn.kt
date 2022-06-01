package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.debajo.staggeredlazycolumn.calculation.*
import ru.debajo.staggeredlazycolumn.state.StaggeredLazyColumnScrollState
import ru.debajo.staggeredlazycolumn.state.rememberStaggeredLazyColumnState

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun StaggeredLazyColumn(
    modifier: Modifier = Modifier,
    state: StaggeredLazyColumnScrollState = rememberStaggeredLazyColumnState(),
    columns: StaggeredLazyColumnCells = StaggeredLazyColumnCells.Fixed(2),
    userScrollEnabled: Boolean = true,
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: StaggeredLazyColumnScope.() -> Unit
) {
    val latestContent = rememberUpdatedState(content)
    val provider = remember {
        StaggeredLazyColumnItemProvider(
            derivedStateOf {
                StaggeredLazyColumnScope()
                    .apply(latestContent.value)
                    .intervals
            }
        )
    }
    val calculatedColumns = remember { CalculatedColumns() }
    val columnsInfo = remember(provider) { StaggeredColumnsInfo().also { state.columnsInfo = it } }
    val result = remember { mutableListOf<Pair<Placeable, StaggeredPlacement>>() }
    val measurePolicy = remember<LazyLayoutMeasureScope.(Constraints) -> MeasureResult>(columnsInfo) {
        { constraints ->
            val currentColumnsCount = with(calculatedColumns) {
                calculateIfNeed(
                    contentPadding = contentPadding,
                    horizontalSpacing = horizontalSpacing,
                    constraints = constraints,
                    columns = columns,
                )
            }

            columnsInfo.onColumnsCalculated(currentColumnsCount)

            prepareItemsToPlace(
                constraints = constraints,
                columns = currentColumnsCount,
                horizontalSpacing = horizontalSpacing,
                verticalSpacing = verticalSpacing,
                columnsInfos = columnsInfo,
                state = state,
                provider = provider,
                result = result,
                contentPadding = contentPadding
            )

            layout(constraints.maxWidth, constraints.maxHeight) {
                val offset = state.scroll
                result.forEach { (placeable, placeableAt) ->
                    placeable.placeRelative(placeableAt.left, placeableAt.top - offset)
                }
            }
        }
    }

    LazyLayout(
        modifier = modifier
            .scrollable(
                state = state,
                orientation = Orientation.Vertical,
                reverseDirection = true,
                interactionSource = state.internalInteractionSource,
                enabled = userScrollEnabled,
            )
            .clipToBounds(),
        prefetchState = state.prefetchState,
        itemProvider = provider,
        measurePolicy = measurePolicy,
    )
}
