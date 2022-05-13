package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun StaggeredLazyColumn(
    modifier: Modifier = Modifier,
    state: StaggeredLazyColumnScrollState = rememberStaggeredLazyColumnState(),
    columns: StaggeredLazyColumnCells = StaggeredLazyColumnCells.Fixed(2),
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: StaggeredLazyColumnScope.() -> Unit
) {
    val latestContent = rememberUpdatedState(content)
    val provider by remember {
        derivedStateOf {
            StaggeredLazyColumnScope().apply(latestContent.value)
        }
    }

    val calculatedColumns = remember { CalculatedColumns() }
    val columnsInfo = remember(provider, calculatedColumns.columns) {
        StaggeredColumnsInfo(columns = (0 until calculatedColumns.columns).map { StaggeredColumnInfo() })
    }
    val result = remember { mutableListOf<Pair<Placeable, StaggeredPlacement>>() }
    LazyLayout(
        modifier = modifier
            .padding(contentPadding)
            .scrollable(state, orientation = Orientation.Vertical, reverseDirection = true)
            .offset { IntOffset(0, -state.value) },
        itemProvider = provider,
        measurePolicy = { constraints ->
            val currentColumnsCount = calculatedColumns.calculateIfNeed(
                this,
                contentPadding,
                horizontalSpacing,
                constraints,
                columns,
            )

            prepareItemsToPlace(
                constraints = constraints,
                columns = currentColumnsCount,
                horizontalSpacing = horizontalSpacing,
                verticalSpacing = verticalSpacing,
                columnsInfos = columnsInfo,
                state = state,
                provider = provider,
                result = result,
            )

            layout(constraints.maxWidth, constraints.maxHeight) {
                result.forEach { (placeable, placeableAt) ->
                    placeable.placeRelative(placeableAt.left, placeableAt.top)
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
class CalculatedColumns {

    var columns by mutableStateOf(2)
        private set

    fun calculateIfNeed(
        lazyLayoutMeasureScope: LazyLayoutMeasureScope,
        contentPadding: PaddingValues,
        horizontalSpacing: Dp,
        constraints: Constraints,
        columns: StaggeredLazyColumnCells,
    ): Int {
        with(lazyLayoutMeasureScope) {
            if (environmentChanged(contentPadding, horizontalSpacing, constraints, columns)) {
                this@CalculatedColumns.columns = calculateColumnsCount(
                    contentPadding = contentPadding,
                    horizontalSpacing = horizontalSpacing,
                    constraints = constraints,
                    columns = columns
                )
            }
        }
        return this@CalculatedColumns.columns
    }

    private var lastContentPadding: PaddingValues? = null
    private var lastHorizontalSpacing: Dp? = null
    private var lastConstraints: Constraints? = null
    private var lastColumns: StaggeredLazyColumnCells? = null

    private fun environmentChanged(
        contentPadding: PaddingValues,
        horizontalSpacing: Dp,
        constraints: Constraints,
        columns: StaggeredLazyColumnCells
    ): Boolean {
        fun saveSnapshot() {
            lastContentPadding = contentPadding
            lastHorizontalSpacing = horizontalSpacing
            lastConstraints = constraints.copy()
            lastColumns = columns
        }

        if (lastContentPadding != contentPadding) {
            saveSnapshot()
            return true
        }

        if (lastHorizontalSpacing != horizontalSpacing) {
            saveSnapshot()
            return true
        }

        if (lastConstraints != constraints) {
            saveSnapshot()
            return true
        }

        if (lastColumns != columns) {
            saveSnapshot()
            return true
        }

        return false
    }
}
