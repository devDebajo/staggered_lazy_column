package ru.debajo.staggeredlazycolumn.calculation

internal data class StaggeredColumnInfo(
    val items: MutableList<StaggeredPlacement> = mutableListOf(),
    var height: Int = 0,
)
