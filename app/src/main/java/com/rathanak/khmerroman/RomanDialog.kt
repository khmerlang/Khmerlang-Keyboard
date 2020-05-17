package com.rathanak.khmerroman

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.rathanak.khmerroman.data.RomanItem
import io.realm.Realm
import kotlinx.android.synthetic.main.roman_dialog.*
import kotlinx.android.synthetic.main.roman_dialog.view.*
import java.util.*

class RomanDialog(txtK: String, txtR: String, appContext: Context) : DialogFragment() {
    private var appCon: Context
    private var txtRoman: String
    private var txtKhmer: String
    private var realm: Realm
    init {
        realm = Realm.getDefaultInstance()
        appCon = appContext
        txtRoman = txtR
        txtKhmer = txtK
    }

    private fun updateRecord(txtK: String, txtR: String) {
        // TODO create, update table
        realm.beginTransaction()
            val romanKhmer: RomanItem = realm.createObject(RomanItem::class.java, UUID.randomUUID().toString())
            romanKhmer.khmer = txtK
            romanKhmer.roman = txtR
            romanKhmer.custom = true
            realm.insert(romanKhmer)
        realm.commitTransaction()
        Toast.makeText(appCon,"record  created",Toast.LENGTH_LONG).show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val rkDialogView = inflater.inflate(R.layout.roman_dialog, null)
            rkDialogView.edit_khmer.setText(txtKhmer)
            rkDialogView.edit_roman.setText(txtRoman)
            builder.setView(rkDialogView)
                .setPositiveButton(R.string.submit,
                    DialogInterface.OnClickListener { dialog, id ->
                        var editKhmer = rkDialogView.edit_khmer.text.toString()
                        var editRomanr = rkDialogView.edit_roman.text.toString()
                        updateRecord(editKhmer, editRomanr)
                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}