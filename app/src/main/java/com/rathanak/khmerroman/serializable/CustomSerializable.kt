package com.rathanak.khmerroman.serializable

import java.io.ObjectInputStream
import java.io.ObjectOutputStream

interface CustomSerializable<T> {
    fun writeObject(oos: ObjectOutputStream)

    fun readObject(ois: ObjectInputStream): T
}