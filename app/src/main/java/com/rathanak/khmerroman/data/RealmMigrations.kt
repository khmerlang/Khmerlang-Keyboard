package com.rathanak.khmerroman.data

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration

class RealmMigrations : RealmMigration {

    override fun migrate(
        realm: DynamicRealm,
        oldVersion: Long,
        newVersion: Long
    ) {
        // Access the Realm schema in order to create, modify or delete classes and their fields.
        var oldVersion = oldVersion
        val schema = realm.schema

        if (oldVersion == 0L) {
            val romanItemSchema = schema["RomanItem"]
            if (romanItemSchema != null) {
                romanItemSchema
                    .addField("roman", String::class.java, FieldAttribute.REQUIRED)
                    .addField("khmer", String::class.java, FieldAttribute.REQUIRED)
                    .addField("custom", Boolean::class.java)
                    .addField("freq", Int::class.java)
            }
            oldVersion++
        }
    }
}