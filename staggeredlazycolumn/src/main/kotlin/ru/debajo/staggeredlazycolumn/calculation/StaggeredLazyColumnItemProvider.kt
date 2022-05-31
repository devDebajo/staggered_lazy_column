package ru.debajo.staggeredlazycolumn.calculation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import ru.debajo.staggeredlazycolumn.StaggeredLazyColumnScope

@Stable
@ExperimentalFoundationApi
internal class StaggeredLazyColumnItemProvider(
    private val intervals: State<List<StaggeredLazyColumnScope.Interval>>
) : LazyLayoutItemProvider {

    private val keyToIndexMapInternal = mutableMapOf<Any, Int>()

    override val keyToIndexMap: Map<Any, Int> = keyToIndexMapInternal

    override fun getKey(index: Int): Any {
        val (interval, intervalItemIndex) = findInterval(index) ?: return super.getKey(index)
        val key = interval.key?.invoke(intervalItemIndex) ?: super.getKey(index)
        keyToIndexMapInternal[key] = index
        return key
    }

    override fun getContentType(index: Int): Any? {
        val (interval, intervalItemIndex) = findInterval(index) ?: return null
        return interval.contentType(intervalItemIndex)
    }

    override val itemCount: Int
        get() = intervals.value.sumOf { it.count }

    @Composable
    override fun Item(index: Int) {
        val (interval, intervalItemIndex) = findInterval(index) ?: return
        interval.itemContent(intervalItemIndex)
    }

    private fun findInterval(fullIndex: Int): Pair<StaggeredLazyColumnScope.Interval, Int>? {
        for (interval in intervals.value) {
            if (fullIndex in interval.startIndex..interval.lastIndex) {
                val intervalItemIndex = fullIndex - interval.startIndex
                return interval to intervalItemIndex
            }
        }
        return null
    }
}
