package ru.debajo.staggeredlazycolumn.visibleitems

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.IntSize

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

@Stable
internal data class StaggeredLazyColumnLayoutInfoImmutable(
    override val totalItemsCount: Int,
    override val viewportEndOffset: Int,
    override val viewportStartOffset: Int,
    override val visibleItemsInfo: List<LazyListItemInfo>,
    override val afterContentPadding: Int,
    override val beforeContentPadding: Int,
    override val viewportSize: IntSize
) : LazyListLayoutInfo {

    constructor(mutable: StaggeredLazyColumnLayoutInfo) : this(
        totalItemsCount = mutable.totalItemsCount,
        viewportEndOffset = mutable.viewportEndOffset,
        viewportStartOffset = mutable.viewportStartOffset,
        visibleItemsInfo = mutable.visibleItemsInfo.map { it.asImmutable() },
        afterContentPadding = mutable.afterContentPadding,
        beforeContentPadding = mutable.beforeContentPadding,
        viewportSize = mutable.viewportSize,
    )

    override val orientation: Orientation = Orientation.Vertical
    override val reverseLayout: Boolean = false
}

internal fun LazyListLayoutInfo.asImmutable(): LazyListLayoutInfo {
    this as StaggeredLazyColumnLayoutInfo
    return StaggeredLazyColumnLayoutInfoImmutable(this)
}