package com.gamapp.signaturepaper.models

class LinkedItem<T>(var value: T) {
    var previous: LinkedItem<T>? = null
    var next: LinkedItem<T>? = null
}

infix fun <T> LinkedItem<T>?.put(value: LinkedItem<T>) {
    this?.next = value
    value.previous = this
}

class LinkedList<T> : Iterable<T> {

    private var root: LinkedItem<T>? = null

    private var last: LinkedItem<T>? = null


    fun add(v: T) {
        val value = LinkedItem(v)
        if (root == null) {
            root = value
            last = root
        }
        last put value
        last = value
    }

    fun first() = root?.value

    fun last() = last?.value


    override fun iterator(): Iterator<T> {
        TODO("Not yet implemented")
    }


}

interface Playable {

}

fun test() {
    val list = LinkedList<Point>()
}