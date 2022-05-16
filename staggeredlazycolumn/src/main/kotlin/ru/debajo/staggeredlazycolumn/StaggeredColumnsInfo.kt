package ru.debajo.staggeredlazycolumn

internal class StaggeredColumnsInfo(
    var columns: List<StaggeredColumnInfo> = emptyList(),
    val items: MutableMap<Int, StaggeredPlacement> = mutableMapOf(),
) {
    val measuredItems: Int
        get() = items.size

    fun onColumnsCalculated(count: Int) {
        if (columns.size != count) {
            columns = (0 until count).map { StaggeredColumnInfo() }
            items.clear()
        }
    }

    fun getFirstVisible(
        y: Int,
        verticalSpacingPx: Int,
    ): StaggeredPlacement? {
        return items.values
            .filter { item -> y in (item.top - item.topOffset)..(item.bottom + verticalSpacingPx) }
            .minByOrNull { it.top }
    }

    fun nextPlaceColumn(): Int {
        if (columns.size == 1) {
            return 0
        }
        return columns.withIndex().minByOrNull { it.value.height }?.index ?: 0
    }

    fun columnHeight(column: Int): Int = columns[column].height

    fun add(column: Int, staggeredPlacement: StaggeredPlacement) {
        columns[column].items.add(staggeredPlacement)
        val height = staggeredPlacement.bottom - columns[column].height
        columns[column].height += height
        items[staggeredPlacement.index] = staggeredPlacement
    }

    fun minHeight(): Int {
        if (columns.size == 1) {
            return columns[0].height
        }
        return columns.minByOrNull { it.height }?.height ?: 0
    }

    fun maxHeight(): Int {
        if (columns.size == 1) {
            return columns[0].height
        }
        return columns.maxByOrNull { it.height }?.height ?: 0
    }
}
