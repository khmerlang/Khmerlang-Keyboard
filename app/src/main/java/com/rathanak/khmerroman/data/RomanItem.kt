package com.rathanak.khmerroman.data

import io.realm.RealmObject
open class RomanItem : RealmObject() {
    var roman: String? = ""
    var khmer: String? = ""
    var custom: Boolean? = false
}