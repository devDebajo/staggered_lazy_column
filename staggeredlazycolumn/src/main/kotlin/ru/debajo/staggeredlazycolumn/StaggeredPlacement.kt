package ru.debajo.staggeredlazycolumn

internal class StaggeredPlacement(
    val index: Int,
    val top: Int,
    val left: Int,
    val bottom: Int,
) {
    fun inViewPort(
        spacingPx: Int,
        viewportTop: Int,
        viewportBottom: Int,
    ): Boolean {
        val topWithSpacing = top - spacingPx
        val bottomWithSpacing = bottom + spacingPx
        val inViewPort = topWithSpacing in viewportTop..viewportBottom ||
                bottomWithSpacing in viewportTop..viewportBottom ||
                topWithSpacing <= viewportTop && bottomWithSpacing >= viewportBottom
        return inViewPort
    }
}
