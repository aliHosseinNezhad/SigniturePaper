package com.gamapp.signaturepaper.models

import java.util.*

/**
 * A object that use to make linked list and have reference to it previous and next object in list
 * */
interface LinkedObject<T> where T : LinkedObject<T> {
    var previous: T?
    var next: T?
}


/**
 * A extension function that returns first linkedObject by looping on [LinkedObject.previous] until previous is null
 * */
fun <T : LinkedObject<T>> T.first(): T {
    var item: T = this
    while (true) {
        item = item.previous ?: return item
    }
}

fun <T : LinkedObject<T>> T.last(): T {
    var item: T = this
    while (true) {
        item = item.next ?: return item
    }
}

/**
 * A extension function that iterate on [LinkedObject.next] .
 * @param block is called on each linkedObject.when return of block is false
 * iteration will be canceled otherwise will be continued to end of linked list
 * */
fun <T : LinkedObject<T>> T.iterateOnNextUntil(block: (T) -> Boolean) {
    var item: T = this
    do {
        if (!block(item)) break
        item = item.next ?: break
    } while (true)
}


fun <T : LinkedObject<T>> T.iterateOnNext(block: (T) -> Unit) {
    var item: T = this
    do {
        block(item)
        item = item.next ?: break
    } while (true)
}