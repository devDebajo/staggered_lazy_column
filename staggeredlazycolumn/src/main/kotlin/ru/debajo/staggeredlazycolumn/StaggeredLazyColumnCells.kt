package ru.debajo.staggeredlazycolumn

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
sealed interface StaggeredLazyColumnCells {
    @JvmInline
    value class Fixed(val columns: Int) : StaggeredLazyColumnCells {
        init {
            check(columns > 1)
        }
    }

    data class Adaptive(val minWidth: Dp, val maxColumns: Int = Int.MAX_VALUE) : StaggeredLazyColumnCells {
        init {
            check(minWidth > 0.dp)
            check(maxColumns > 1)
        }
    }
}
