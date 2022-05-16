package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
@OptIn(ExperimentalFoundationApi::class)
class StaggeredLazyColumnScope {

    internal val intervals = mutableListOf<Interval>()

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

    @Stable
    internal class Interval(
        val startIndex: Int,
        val lastIndex: Int,
        val count: Int,
        val key: ((index: Int) -> Any)? = null,
        val contentType: (Int) -> Any? = { null },
        val itemContent: @Composable (index: Int) -> Unit
    )
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
