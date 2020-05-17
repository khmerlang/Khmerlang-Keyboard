package com.rathanak.khmerroman.migration

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration



class MyMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var oldVersion = oldVersion

        val schema = realm.schema
        if (oldVersion == 0L) {
//            schema.create("RomanItem")
//                .addField("roman", String::class.java)
//                .addField("khmer", String::class.java)
//                .addField("custom", Boolean::class.java)
            oldVersion++
        }

        if (oldVersion == 1L) {
//            schema.get("RomanItem")!!
//                .addField("id", String::class.java, FieldAttribute.PRIMARY_KEY)
            oldVersion++
        }
    }
}