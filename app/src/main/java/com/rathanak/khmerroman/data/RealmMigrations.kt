package com.rathanak.khmerroman.data

import android.util.Log
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import java.util.*

class RealmMigrations : RealmMigration {

    override fun migrate(
        realm: DynamicRealm,
        oldVersion: Long,
        newVersion: Long
    ) {
        // Access the Realm schema in order to create, modify or delete classes and their fields.
        var oldVersion = oldVersion
        val schema = realm.schema

        if (oldVersion == 1L) {
            schema.create("Ngram")
                .addField("id", Int::class.java, FieldAttribute.PRIMARY_KEY)
                .addField("keyword", String::class.java, FieldAttribute.REQUIRED, FieldAttribute.INDEXED)
                .addField("roman", String::class.java, FieldAttribute.INDEXED)
                .addField("lang", Int::class.java)
                .addField("gram", Int::class.java)
                .addField("count", Int::class.java)
                .addField("is_custom", Boolean::class.java)

            oldVersion++
        }

        if (oldVersion == 2L) {
            schema.get("Ngram")!!
                .addIndex("lang")
                .addIndex("gram")
                .addIndex("is_custom")
                .renameField("roman", "other")

            oldVersion++
        }

        if (oldVersion == 3L) {
            schema.get("Ngram")!!
                .removeIndex("is_custom")
                .removeIndex("other")
                .renameField("is_custom", "custom")

            oldVersion++
        }
    }
}