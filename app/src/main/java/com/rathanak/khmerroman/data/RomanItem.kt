package com.rathanak.khmerroman.data

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class RomanItem : RealmObject() {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var roman: String? = ""
    var khmer: String? = ""
    var custom: Boolean? = false
}