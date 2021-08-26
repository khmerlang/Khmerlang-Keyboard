package com.rathanak.khmerroman.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.Ngram
import com.rathanak.khmerroman.data.RealmMigrations
import com.rathanak.khmerroman.data.RomanItem
import com.rathanak.khmerroman.view.Roman2KhmerApp
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.roman_dialog.*
import kotlinx.android.synthetic.main.roman_dialog.view.*
import java.io.FileNotFoundException
import java.util.*

class RomanDialog(var txtKhmer: String, var txtRoman: String, var count: Int, val appCon: Context) : DialogFragment() {
    private var realm: Realm = Realm.getInstance(Roman2KhmerApp.dbConfig)

    private fun updateRecord(keyword: String, roman: String) {
        if (keyword.isNotEmpty() && roman.isNotEmpty()) {
            // TODO create, update table
            realm.beginTransaction()
                val ngramData: Ngram = realm.createObject(Ngram::class.java, UUID.randomUUID().toString())
                ngramData.keyword = keyword
                ngramData.roman = roman
                ngramData.count = count
                ngramData.is_custom = true
                ngramData.lang = Roman2KhmerApp.LANG_KH
                ngramData.gram = Roman2KhmerApp.ONE_GRAM
                realm.insert(ngramData)
            realm.commitTransaction()
            Toast.makeText(appCon,"record  created",Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(appCon,"item empty",Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val rkDialogView = inflater.inflate(R.layout.roman_dialog, null)
            rkDialogView.edit_khmer.setText(txtKhmer)
            rkDialogView.edit_roman.setText(txtRoman)
            builder.setView(rkDialogView)
            rkDialogView.btnSubmit.setOnClickListener {
                var keyword = rkDialogView.edit_khmer.text.toString()
                var roman = rkDialogView.edit_roman.text.toString()
                updateRecord(keyword, roman)
                dismiss()
            }
            rkDialogView.btnCancel.setOnClickListener {
                dismiss()
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}