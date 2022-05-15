package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

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
    val provider by remember {
        derivedStateOf {
            StaggeredLazyColumnScope().apply(latestContent.value)
        }
    }

    val calculatedColumns = remember { CalculatedColumns() }
    val columnsInfo = remember(provider) { StaggeredColumnsInfo() }
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
                val offset = state.value
                result.forEach { (placeable, placeableAt) ->
                    placeable.placeRelative(placeableAt.left, placeableAt.top - offset)
                }
            }
        }
    }

    LazyLayout(
        modifier = modifier
            .padding(contentPadding)
            .scrollable(
                state = state,
                orientation = Orientation.Vertical,
                reverseDirection = true,
                interactionSource = state.internalInteractionSource,
                enabled = userScrollEnabled,
            ),
        itemProvider = provider,
        measurePolicy = measurePolicy,
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Stable
internal class StaggeredLazyColumnVisibleItemsController(
    private val state: StaggeredLazyColumnScrollState,
) {
    private val cache = StaggeredLazyColumnItemInfoCache()
    private val valueMutable = mutableStateOf(
        value = StaggeredLazyColumnLayoutInfo(),
        policy = neverEqualPolicy()
    )

    val value: LazyListLayoutInfo get() = valueMutable.value

    fun onStartMeasure() {
        cache.free()
        valueMutable.value.visibleItemsInfoMutable.clear()
    }

    fun addVisibleItem(placeableAt: StaggeredPlacement, provider: LazyLayoutItemProvider) {
        val itemInfo = cache.get(
            index = placeableAt.index,
            key = provider.getKey(placeableAt.index),
            offset = placeableAt.top,
            size = placeableAt.bottom - placeableAt.top
        )
        valueMutable.value.visibleItemsInfoMutable.add(itemInfo)
    }

    fun onEndMeasure(
        viewportWidth: Int,
        viewportHeight: Int,
        provider: LazyLayoutItemProvider,
        afterContentPadding: Int,
        beforeContentPadding: Int
    ) {
        valueMutable.value.viewportStartOffset = state.value
        valueMutable.value.viewportEndOffset = state.value + viewportHeight
        valueMutable.value.totalItemsCount = provider.itemCount
        valueMutable.value.afterContentPadding = afterContentPadding
        valueMutable.value.beforeContentPadding = beforeContentPadding
        valueMutable.value.viewportSize = IntSize(viewportWidth, viewportHeight)

        valueMutable.value = valueMutable.value // invalidate observable state
    }
}

@Stable
internal class StaggeredLazyColumnLayoutInfo : LazyListLayoutInfo {
    val visibleItemsInfoMutable: MutableList<LazyListItemInfo> = mutableListOf()
    override var totalItemsCount: Int = 0
    override var viewportEndOffset: Int = 0
    override var viewportStartOffset: Int = 0
    override val visibleItemsInfo: List<LazyListItemInfo> = visibleItemsInfoMutable
    override val orientation: Orientation = Orientation.Vertical
    override val reverseLayout: Boolean = false
    override var afterContentPadding: Int = 0
    override var beforeContentPadding: Int = 0
    override var viewportSize: IntSize = IntSize.Zero
}

internal class StaggeredLazyColumnItemInfoCache {
    private val cache: MutableList<Wrapper> = mutableListOf()

    fun get(
        index: Int,
        key: Any,
        offset: Int,
        size: Int,
    ): StaggeredLazyColumnItemInfo {
        val freeWrapper = cache.firstOrNull { it.free }
        return if (freeWrapper != null) {
            freeWrapper.free = false
            freeWrapper.info.configure(index, key, offset, size)
            freeWrapper.info
        } else {
            val newWrapper = Wrapper(
                info = StaggeredLazyColumnItemInfo(),
                free = false
            )
            cache.add(newWrapper)
            newWrapper.info
        }
    }

    fun free() {
        cache.forEach { it.free = true }
    }

    private class Wrapper(
        val info: StaggeredLazyColumnItemInfo,
        var free: Boolean,
    )
}

internal class StaggeredLazyColumnItemInfo : LazyListItemInfo {
    override var index: Int = -1
        private set

    override var key: Any = Unit
        private set

    override var offset: Int = 0
        private set

    override var size: Int = 0
        private set

    fun configure(
        index: Int,
        key: Any,
        offset: Int,
        size: Int,
    ) {
        this.index = index
        this.key = key
        this.offset = offset
        this.size = size
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StaggeredLazyColumnItemInfo

        if (index != other.index) return false
        if (key != other.key) return false
        if (offset != other.offset) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + key.hashCode()
        result = 31 * result + offset
        result = 31 * result + size
        return result
    }
}
