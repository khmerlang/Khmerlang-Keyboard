package com.rathanak.khmerroman.data

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class Ngram : RealmObject() {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    @Required
    @Index
    var keyword: String = ""
    @Index
    var roman: String? = ""
    var lang: Int = 0
    var gram: Int = 1
    var count: Int = 0
    var is_custom: Boolean = false
}