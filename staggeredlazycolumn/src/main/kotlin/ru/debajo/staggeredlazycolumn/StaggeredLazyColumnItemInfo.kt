package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.runtime.Stable

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
}

@Stable
internal data class StaggeredLazyColumnItemInfoImmutable(
    override val index: Int,
    override val key: Any,
    override val offset: Int,
    override val size: Int,
) : LazyListItemInfo {
    constructor(mutable: StaggeredLazyColumnItemInfo) : this(
        index = mutable.index,
        key = mutable.key,
        offset = mutable.offset,
        size = mutable.size,
    )
}

internal fun LazyListItemInfo.asImmutable(): LazyListItemInfo {
    this as StaggeredLazyColumnItemInfo
    return StaggeredLazyColumnItemInfoImmutable(this)
}
