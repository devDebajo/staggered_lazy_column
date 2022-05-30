package ru.debajo.staggeredlazycolumn

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.layout.LazyLayoutPrefetchState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

@Stable
@OptIn(ExperimentalFoundationApi::class)
class StaggeredLazyColumnScrollState internal constructor(
    internal val firstVisibleItemIndexInner: Int = 0,
    internal val firstVisibleItemScrollOffsetInner: Int = 0,
) : ScrollableState {

    internal var firstHandled: Boolean = false

    var scroll: Int by mutableStateOf(0, structuralEqualityPolicy())
        private set

    var maxValue: Int
        get() = _maxValueState.value
        internal set(newMax) {
            _maxValueState.value = newMax
            if (scroll > newMax) {
                scroll = newMax
            }
        }

    var firstVisibleItemIndex: Int by mutableStateOf(0)
        internal set

    var firstVisibleItemScrollOffset: Int by mutableStateOf(0)
        internal set

    val interactionSource: InteractionSource get() = internalInteractionSource

    /**
     * Doesn't use observable compose [State]
     */
    val layoutInfo: LazyListLayoutInfo
        get() = visibleItemsController.snapshot()

    internal val prefetchState: LazyLayoutPrefetchState = LazyLayoutPrefetchState()
    internal val visibleItemsController = StaggeredLazyColumnVisibleItemsController(this)
    internal var columnsInfo: StaggeredColumnsInfo = StaggeredColumnsInfo()
    internal val internalInteractionSource: MutableInteractionSource = MutableInteractionSource()

    private var _maxValueState = mutableStateOf(Int.MAX_VALUE, structuralEqualityPolicy())
    private var accumulator: Float = 0f

    private val scrollableState = ScrollableState {
        val absolute = (scroll + it + accumulator)
        val newValue = absolute.coerceIn(0f, maxValue.toFloat())
        val changed = absolute != newValue
        val consumed = newValue - scroll
        val consumedInt = consumed.roundToInt()
        scroll += consumedInt
        accumulator = consumed - consumedInt
        if (changed) consumed else it
    }

    init {
        firstHandled = firstVisibleItemIndexInner + firstVisibleItemScrollOffsetInner == 0
    }

    fun observeLayoutInfo(): Flow<LazyListLayoutInfo> = visibleItemsController.observe()

    fun canScroll(direction: ScrollDirection): Boolean {
        return when (direction) {
            ScrollDirection.DOWN -> scroll > 0
            ScrollDirection.UP -> scroll < maxValue
        }
    }

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ): Unit = scrollableState.scroll(scrollPriority, block)

    override fun dispatchRawDelta(delta: Float): Float =
        scrollableState.dispatchRawDelta(delta)

    override val isScrollInProgress: Boolean
        get() = scrollableState.isScrollInProgress

    suspend fun animateScrollToOffset(offset: Int, animationSpec: AnimationSpec<Float> = spring()) {
        animateScrollBy(calculateScrollToOffsetDelta(offset), animationSpec)
    }

    suspend fun scrollToOffset(offset: Int) {
        scrollBy(calculateScrollToOffsetDelta(offset))
    }

    suspend fun animateScrollToItem(
        index: Int,
        scrollOffset: Int = 0,
        scrollSpeed: ScrollSpeed = ScrollSpeed.SLOW,
    ) {
        val itemInfo = columnsInfo.items[index]
        if (itemInfo != null) {
            val offset = itemInfo.top + scrollOffset
            if (offset != 0) {
                animateScrollToOffset(offset, tween(scrollSpeed.iterationDuration, easing = LinearEasing))
            }
            return
        }

        val iterationPixels = 2000f
        val iterationDuration = scrollSpeed.iterationDuration
        scroll {
            val anim = AnimationState(initialValue = 0f)
            runCatching {
                while (true) {
                    try {
                        var prevValue = 0f
                        anim.copy(value = 0f).animateTo(
                            targetValue = iterationPixels,
                            animationSpec = tween(iterationDuration, easing = LinearEasing),
                            sequentialAnimation = (anim.velocity != 0f),
                        ) {
                            val targetItem = columnsInfo.items[index]
                            if (targetItem == null) {
                                val delta = value - prevValue
                                if (scrollBy(delta) == 0f) {
                                    if (delta != 0f) {
                                        throw CancellationException()
                                    }
                                }
                                prevValue = value
                            } else {
                                throw ItemFoundSignal(targetItem)
                            }
                        }
                    } catch (itemFoundSignal: ItemFoundSignal) {
                        val delta = itemFoundSignal.targetItem.top - scrollOffset - scroll
                        var prevValue = 0f
                        val durationPerPixel = iterationDuration / iterationPixels
                        anim.copy(value = 0f).animateTo(
                            targetValue = delta.toFloat(),
                            animationSpec = tween((durationPerPixel * delta).roundToInt(), easing = LinearEasing),
                            sequentialAnimation = (anim.velocity != 0f),
                            block = {
                                val scrollDelta = value - prevValue
                                if (scrollBy(scrollDelta) == 0f) {
                                    if (scrollDelta != 0f) {
                                        throw CancellationException()
                                    }
                                }
                                prevValue = value
                            }
                        )
                        break
                    }
                }
            }
        }
    }

    internal fun setScroll(scroll: Int) {
        this.scroll = scroll.coerceIn(0, maxValue)
    }

    private fun calculateScrollToOffsetDelta(offset: Int): Float {
        return when {
            offset == scroll -> return 0f
            offset <= 0f -> -scroll.toFloat()
            offset >= maxValue -> maxValue - scroll.toFloat()
            else -> offset - scroll.toFloat()
        }
    }

    enum class ScrollDirection { DOWN, UP }

    enum class ScrollSpeed(val iterationDuration: Int) {
        FAST(10),
        MEDIUM(40),
        SLOW(100)
    }

    private class ItemFoundSignal(val targetItem: StaggeredPlacement) : CancellationException()

    companion object {
        val Saver: Saver<StaggeredLazyColumnScrollState, *> = Saver(
            save = { listOf(it.firstVisibleItemIndex, it.firstVisibleItemScrollOffset) },
            restore = {
                StaggeredLazyColumnScrollState(
                    firstVisibleItemIndexInner = it[0],
                    firstVisibleItemScrollOffsetInner = it[1]
                )
            }
        )
    }
}

@Composable
fun rememberStaggeredLazyColumnState(
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0
): StaggeredLazyColumnScrollState {
    return rememberSaveable(saver = StaggeredLazyColumnScrollState.Saver) {
        StaggeredLazyColumnScrollState(
            firstVisibleItemIndexInner = initialFirstVisibleItemIndex,
            firstVisibleItemScrollOffsetInner = initialFirstVisibleItemScrollOffset,
        )
    }
}
