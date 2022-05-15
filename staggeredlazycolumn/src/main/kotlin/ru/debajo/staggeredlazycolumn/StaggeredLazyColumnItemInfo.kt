package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.runtime.Stable

@Stable
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