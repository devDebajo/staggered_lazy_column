package ru.debajo.staggeredlazycolumn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalFoundationApi::class)
internal class CalculatedColumns {

    var columns by mutableStateOf(2)
        private set

    fun calculateIfNeed(
        lazyLayoutMeasureScope: LazyLayoutMeasureScope,
        contentPadding: PaddingValues,
        horizontalSpacing: Dp,
        constraints: Constraints,
        columns: StaggeredLazyColumnCells,
    ): Int {
        with(lazyLayoutMeasureScope) {
            if (environmentChanged(contentPadding, horizontalSpacing, constraints, columns)) {
                this@CalculatedColumns.columns = calculateColumnsCount(
                    contentPadding = contentPadding,
                    horizontalSpacing = horizontalSpacing,
                    constraints = constraints,
                    columns = columns
                )
            }
        }
        return this@CalculatedColumns.columns
    }

    private var lastContentPadding: PaddingValues? = null
    private var lastHorizontalSpacing: Dp? = null
    private var lastConstraints: Constraints? = null
    private var lastColumns: StaggeredLazyColumnCells? = null

    private fun environmentChanged(
        contentPadding: PaddingValues,
        horizontalSpacing: Dp,
        constraints: Constraints,
        columns: StaggeredLazyColumnCells
    ): Boolean {
        fun saveSnapshot() {
            lastContentPadding = contentPadding
            lastHorizontalSpacing = horizontalSpacing
            lastConstraints = constraints.copy()
            lastColumns = columns
        }

        if (lastContentPadding != contentPadding) {
            saveSnapshot()
            return true
        }

        if (lastHorizontalSpacing != horizontalSpacing) {
            saveSnapshot()
            return true
        }

        if (lastConstraints != constraints) {
            saveSnapshot()
            return true
        }

        if (lastColumns != columns) {
            saveSnapshot()
            return true
        }

        return false
    }
}
