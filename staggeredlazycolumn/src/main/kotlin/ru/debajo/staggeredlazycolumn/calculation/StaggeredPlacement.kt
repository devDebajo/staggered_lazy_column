package ru.debajo.staggeredlazycolumn.calculation

internal data class StaggeredPlacement(
    val index: Int,
    val top: Int,
    val left: Int,
    val bottom: Int,
    val topOffset: Int,
) {
    fun inViewPort(viewportTop: Int, viewportBottom: Int): Boolean {
        val topWithOffset = top - topOffset
        return topWithOffset in viewportTop..viewportBottom ||
                bottom in viewportTop..viewportBottom ||
                topWithOffset <= viewportTop && bottom >= viewportBottom
    }
}
