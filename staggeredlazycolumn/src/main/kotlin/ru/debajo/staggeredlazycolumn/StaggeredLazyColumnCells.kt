package ru.debajo.staggeredlazycolumn

import androidx.compose.runtime.Immutable

@Immutable
sealed interface StaggeredLazyColumnCells {
    class Fixed(val columns: Int) : StaggeredLazyColumnCells {
        init {
            check(columns > 1)
        }
    }
}
