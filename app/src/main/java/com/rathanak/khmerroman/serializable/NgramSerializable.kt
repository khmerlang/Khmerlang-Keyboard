package com.rathanak.khmerroman.serializable

import java.io.ObjectInputStream
import java.io.ObjectOutputStream

data class NgramSerializable(var keyword: String = "", var count: Int = 0, var gram: Int = 1, var lang: Int = 0, var oth: Array<String> = emptyArray()): CustomSerializable<NgramSerializable> {
    override fun writeObject(oos: ObjectOutputStream) {
        oos.writeObject(keyword)
        oos.writeInt(count)
        oos.writeInt(gram)
        oos.writeInt(lang)
        oos.writeObject(oth)
    }

    override fun readObject(ois: ObjectInputStream): NgramSerializable {
        this.keyword = ois.readObject() as String
        this.count = ois.readInt()
        this.gram = ois.readInt()
        this.lang = ois.readInt()
        this.oth = ois.readObject() as Array<String>
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NgramSerializable

        if (keyword != other.keyword) return false
        if (count != other.count) return false
        if (gram != other.gram) return false
        if (lang != other.lang) return false
        if (!oth.contentEquals(other.oth)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyword.hashCode()
        result = 31 * result + count
        result = 31 * result + gram
        result = 31 * result + lang
        result = 31 * result + oth.contentHashCode()
        return result
    }
}