package com.rathanak.khmerroman.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.Ngram
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Realm
import kotlinx.android.synthetic.main.roman_dialog.view.*

class RomanDialog(var txtKhmer: String, var txtRoman: String, var count: Int, val appCon: Context) : DialogFragment() {
    private var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)

    private fun createRecord(keyword: String, roman: String) {
        if (keyword.isNotEmpty() && roman.isNotEmpty()) {
            // TODO create, update table
//            var nextId = realm.where(Ngram::class.java).max("id") as Int + 1
            var nextId =  KhmerLangApp.getNextKey()
            realm.beginTransaction()
                val ngramData: Ngram = realm.createObject(Ngram::class.java, nextId)
                ngramData.keyword = keyword
                ngramData.roman = roman
                ngramData.lang = KhmerLangApp.LANG_KH
                ngramData.gram = KhmerLangApp.ONE_GRAM
                ngramData.count = count
                ngramData.is_custom = true
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
                // TODO validation
                var keyword = rkDialogView.edit_khmer.text.toString()
                var roman = rkDialogView.edit_roman.text.toString()
                createRecord(keyword, roman)
                dismiss()
            }
            rkDialogView.btnCancel.setOnClickListener {
                dismiss()
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}