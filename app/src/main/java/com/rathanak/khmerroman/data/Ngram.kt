package com.rathanak.khmerroman.data

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*
import io.realm.RealmQuery

open class Ngram : RealmObject() {
    @PrimaryKey
    var id: Int = 0
    @Required
    @Index
    var keyword: String = ""
    @Index
    var lang: Int = 0
    @Index
    var gram: Int = 1

    var other: String? = ""
    var count: Int = 0
    var custom: Boolean = false
}