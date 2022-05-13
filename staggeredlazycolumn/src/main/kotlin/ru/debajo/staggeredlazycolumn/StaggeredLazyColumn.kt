package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
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

    val columnsCount = (columns as StaggeredLazyColumnCells.Fixed).columns
    val columnsInfo = remember(provider) {
        StaggeredColumnsInfo(columns = (0 until columnsCount).map { StaggeredColumnInfo() })
    }
    val result = remember { mutableListOf<Pair<Placeable, StaggeredPlacement>>() }
    LazyLayout(
        modifier = modifier
            .padding(contentPadding)
            .scrollable(state, orientation = Orientation.Vertical, reverseDirection = true)
            .offset { IntOffset(0, -state.value) },
        itemProvider = provider,
        measurePolicy = { constraints ->
            prepareItemsToPlace(
                constraints = constraints,
                columns = columnsCount,
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
