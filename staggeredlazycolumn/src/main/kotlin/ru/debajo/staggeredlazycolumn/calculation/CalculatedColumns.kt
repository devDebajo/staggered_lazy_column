package ru.debajo.staggeredlazycolumn.calculation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import ru.debajo.staggeredlazycolumn.StaggeredLazyColumnCells

@ExperimentalFoundationApi
internal class CalculatedColumns {

    private var columns: Int = -1

    fun LazyLayoutMeasureScope.calculateIfNeed(
        contentPadding: PaddingValues,
        horizontalSpacing: Dp,
        constraints: Constraints,
        columns: StaggeredLazyColumnCells,
    ): Int {
        if (environmentChanged(contentPadding, horizontalSpacing, constraints, columns)) {
            this@CalculatedColumns.columns = calculateColumnsCount(
                contentPadding = contentPadding,
                horizontalSpacing = horizontalSpacing,
                constraints = constraints,
                columns = columns
            )
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

        if (lastContentPadding?.horizontalChanged(contentPadding) != false) {
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

    private fun PaddingValues.horizontalChanged(newContentPadding: PaddingValues): Boolean {
        val direction = LayoutDirection.Ltr
        return newContentPadding.calculateLeftPadding(direction) != calculateLeftPadding(direction) ||
                newContentPadding.calculateRightPadding(direction) != calculateRightPadding(direction)
    }
}
