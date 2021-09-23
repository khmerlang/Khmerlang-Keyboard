package com.rathanak.khmerroman.serializable

import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class NgramRecordSerializable (var id: Long = -1, var ngram: NgramSerializable = NgramSerializable()): CustomSerializable<NgramRecordSerializable>{
    override fun writeObject(oos: ObjectOutputStream) {
        oos.writeLong(id)
        // IMPORTANT! See How we serialize our classes
        ngram.writeObject(oos)
    }

    override fun readObject(ois: ObjectInputStream): NgramRecordSerializable {
        this.id = ois.readLong()
        // IMPORTANT! See How we deserialize our classes
        this.ngram = NgramSerializable().readObject(ois)
        return this
    }
}