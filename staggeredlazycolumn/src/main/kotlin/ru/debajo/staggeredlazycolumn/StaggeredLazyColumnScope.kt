package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
@OptIn(ExperimentalFoundationApi::class)
class StaggeredLazyColumnScope : LazyLayoutItemProvider {

    private val intervals = mutableListOf<Interval>()
    private val keyToIndexMapInternal = mutableMapOf<Any, Int>()
    override val keyToIndexMap: Map<Any, Int> = keyToIndexMapInternal

    fun item(
        key: Any? = null,
        contentType: Any? = null,
        content: @Composable () -> Unit
    ) {
        items(
            count = 1,
            key = if (key == null) {
                null
            } else {
                { key }
            },
            contentType = { contentType },
            itemContent = { content() },
        )
    }

    fun items(
        count: Int,
        key: ((index: Int) -> Any)? = null,
        contentType: (Int) -> Any? = { null },
        itemContent: @Composable (index: Int) -> Unit
    ) {
        val startIndex = intervals.lastOrNull()?.lastIndex?.let { it + 1 } ?: 0
        val lastIndex = startIndex + count - 1
        val interval = Interval(
            startIndex = startIndex,
            lastIndex = lastIndex,
            count = count,
            key = key,
            contentType = contentType,
            itemContent = itemContent,
        )
        intervals.add(interval)
    }

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
        get() = intervals.sumOf { it.count }

    @Composable
    override fun Item(index: Int) {
        val (interval, intervalItemIndex) = findInterval(index) ?: return
        interval.itemContent(intervalItemIndex)
    }

    private class Interval(
        val startIndex: Int,
        val lastIndex: Int,
        val count: Int,
        val key: ((index: Int) -> Any)? = null,
        val contentType: (Int) -> Any? = { null },
        val itemContent: @Composable (index: Int) -> Unit
    )

    private fun findInterval(fullIndex: Int): Pair<Interval, Int>? {
        for (interval in intervals) {
            if (fullIndex in interval.startIndex..interval.lastIndex) {
                val intervalItemIndex = fullIndex - interval.startIndex
                return interval to intervalItemIndex
            }
        }
        return null
    }
}

inline fun <T> StaggeredLazyColumnScope.items(
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable (item: T) -> Unit
) {
    items(
        count = items.size,
        key = if (key != null) { index: Int -> key(items[index]) } else null,
        contentType = { index: Int -> contentType(items[index]) },
        itemContent = { itemContent(items[it]) }
    )
}
