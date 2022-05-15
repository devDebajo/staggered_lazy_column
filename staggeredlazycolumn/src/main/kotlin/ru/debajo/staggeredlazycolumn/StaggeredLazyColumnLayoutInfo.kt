package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.IntSize

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
