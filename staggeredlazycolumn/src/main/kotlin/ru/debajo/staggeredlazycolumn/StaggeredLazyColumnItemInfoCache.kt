package ru.debajo.staggeredlazycolumn

internal class StaggeredLazyColumnItemInfoCache {
    private val cache: MutableList<Wrapper> = mutableListOf()

    fun get(
        index: Int,
        key: Any,
        offset: Int,
        size: Int,
    ): StaggeredLazyColumnItemInfo {
        val freeWrapper = cache.firstOrNull { it.free }
        val res =  if (freeWrapper != null) {
            freeWrapper.free = false
            freeWrapper.info.configure(index, key, offset, size)
            freeWrapper.info
        } else {
            val newWrapper = Wrapper(
                info = StaggeredLazyColumnItemInfo(),
                free = false
            )
            cache.add(newWrapper)
            newWrapper.info
        }

        return res
    }

    fun free() {
        cache.forEach { it.free = true }
    }

    private class Wrapper(
        val info: StaggeredLazyColumnItemInfo,
        var free: Boolean,
    )
}
