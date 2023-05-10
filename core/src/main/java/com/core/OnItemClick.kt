package com.core

fun interface OnItemClick<T : Any> {
    fun onItemClickListener(item: T, position: Int)
}