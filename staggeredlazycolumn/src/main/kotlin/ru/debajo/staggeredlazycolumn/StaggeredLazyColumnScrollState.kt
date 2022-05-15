package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlin.math.roundToInt

@Stable
class StaggeredLazyColumnScrollState(initial: Int) : ScrollableState {

    internal val visibleItemsController = StaggeredLazyColumnVisibleItemsController(this)

    var value: Int by mutableStateOf(initial, structuralEqualityPolicy())
        private set

    var maxValue: Int
        get() = _maxValueState.value
        internal set(newMax) {
            _maxValueState.value = newMax
            if (value > newMax) {
                value = newMax
            }
        }

    val layoutInfo: LazyListLayoutInfo get() = visibleItemsController.value

    val interactionSource: InteractionSource get() = internalInteractionSource

    internal val internalInteractionSource: MutableInteractionSource = MutableInteractionSource()

    private var _maxValueState = mutableStateOf(Int.MAX_VALUE, structuralEqualityPolicy())

    private var accumulator: Float = 0f

    private val scrollableState = ScrollableState {
        val absolute = (value + it + accumulator)
        val newValue = absolute.coerceIn(0f, maxValue.toFloat())
        val changed = absolute != newValue
        val consumed = newValue - value
        val consumedInt = consumed.roundToInt()
        value += consumedInt
        accumulator = consumed - consumedInt
        if (changed) consumed else it
    }

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ): Unit = scrollableState.scroll(scrollPriority, block)

    override fun dispatchRawDelta(delta: Float): Float =
        scrollableState.dispatchRawDelta(delta)

    override val isScrollInProgress: Boolean
        get() = scrollableState.isScrollInProgress

    companion object {
        val Saver: Saver<StaggeredLazyColumnScrollState, *> = Saver(
            save = { it.value },
            restore = { StaggeredLazyColumnScrollState(it) }
        )
    }
}

@Composable
fun rememberStaggeredLazyColumnState(): StaggeredLazyColumnScrollState {
    return rememberSaveable(saver = StaggeredLazyColumnScrollState.Saver) {
        StaggeredLazyColumnScrollState(initial = 0)
    }
}