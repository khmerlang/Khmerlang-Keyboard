package com.rathanak.khmerroman.keyboard.extensions

import android.util.SparseArray

inline fun <T> SparseArray<T>.forEach(action: (key: Int, value: T) -> Unit) {
    for (index in 0 until size()) {
        action(keyAt(index), valueAt(index))
    }
}

inline operator fun <T> SparseArray<T>.contains(key: Int) = indexOfKey(key) >= 0

