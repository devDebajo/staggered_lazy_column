package ru.debajo.staggeredlazycolumn.visibleitems

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.debajo.staggeredlazycolumn.calculation.StaggeredPlacement
import ru.debajo.staggeredlazycolumn.state.StaggeredLazyColumnScrollState
import java.util.*
import kotlin.coroutines.CoroutineContext

@Stable
@ExperimentalFoundationApi
internal class StaggeredLazyColumnVisibleItemsController(
    private val state: StaggeredLazyColumnScrollState,
) {
    private val cache = StaggeredLazyColumnItemInfoCache()
    private val lazyListLayoutInfo: StaggeredLazyColumnLayoutInfo = StaggeredLazyColumnLayoutInfo()
    private val onMeasureEndListeners: MutableSet<() -> Unit> = Collections.synchronizedSet(mutableSetOf())

    fun observe(
        context: CoroutineContext = Default
    ): Flow<LazyListLayoutInfo> {
        return callbackFlow {
            val listener = OnMeasureEndListener(context, this, ::snapshot)
            onMeasureEndListeners.add(listener)
            awaitClose { onMeasureEndListeners.remove(listener) }
        }.flowOn(context)
    }

    fun onStartMeasure() {
        cache.free()
        lazyListLayoutInfo.visibleItemsInfoMutable.clear()
    }

    fun addVisibleItem(placeableAt: StaggeredPlacement, provider: LazyLayoutItemProvider) {
        val itemInfo = cache.get(
            index = placeableAt.index,
            key = provider.getKey(placeableAt.index),
            offset = placeableAt.top,
            size = placeableAt.bottom - placeableAt.top
        )
        lazyListLayoutInfo.visibleItemsInfoMutable.add(itemInfo)
    }

    fun onEndMeasure(
        viewportWidth: Int,
        viewportHeight: Int,
        provider: LazyLayoutItemProvider,
        afterContentPadding: Int,
        beforeContentPadding: Int,
    ) {
        with(lazyListLayoutInfo) {
            viewportStartOffset = state.scroll
            viewportEndOffset = state.scroll + viewportHeight
            totalItemsCount = provider.itemCount
            this.afterContentPadding = afterContentPadding
            this.beforeContentPadding = beforeContentPadding
            viewportSize = IntSize(viewportWidth, viewportHeight)
        }
        onMeasureEndListeners.forEach { it() }
    }

    internal fun snapshot(): LazyListLayoutInfo = lazyListLayoutInfo.asImmutable()

    private class OnMeasureEndListener(
        private val context: CoroutineContext,
        private val producerScope: ProducerScope<LazyListLayoutInfo>,
        private val getSnapshot: () -> LazyListLayoutInfo,
    ) : () -> Unit {
        private var job: Job? = null

        override fun invoke() {
            job?.cancel()
            job = producerScope.launch(context) {
                producerScope.send(getSnapshot())
            }
        }
    }
}
