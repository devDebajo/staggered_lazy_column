package ru.debajo.staggeredlazycolumn

internal class StaggeredColumnsInfo(
    val columns: List<StaggeredColumnInfo>,
    var measuredItems: Int = 0,
    val items: MutableMap<Int, StaggeredPlacement> = mutableMapOf(),
) {
    fun getFirstVisible(y: Int): StaggeredPlacement? {
        return items.values.filter { item -> y in item.top..item.bottom }.minByOrNull { it.top }
    }

    fun nextPlaceColumn(): Int {
        return columns.withIndex().minByOrNull { it.value.height }?.index ?: 0
    }

    fun columnHeight(column: Int): Int = columns[column].height

    fun add(column: Int, staggeredPlacement: StaggeredPlacement) {
        columns[column].items.add(staggeredPlacement)
        val height = staggeredPlacement.bottom - columns[column].height
        columns[column].height += height
        items[staggeredPlacement.index] = staggeredPlacement
    }

    fun minHeight(): Int = columns.minByOrNull { it.height }?.height ?: 0

    fun maxHeight(): Int = columns.maxByOrNull { it.height }?.height ?: 0
}
