package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.ui.unit.IntSize

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
        valueMutable.value.viewportStartOffset = state.scroll
        valueMutable.value.viewportEndOffset = state.scroll + viewportHeight
        valueMutable.value.totalItemsCount = provider.itemCount
        valueMutable.value.afterContentPadding = afterContentPadding
        valueMutable.value.beforeContentPadding = beforeContentPadding
        valueMutable.value.viewportSize = IntSize(viewportWidth, viewportHeight)

        valueMutable.value = valueMutable.value // invalidate observable state
    }
}
