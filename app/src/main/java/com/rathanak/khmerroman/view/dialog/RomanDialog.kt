package com.rathanak.khmerroman.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.data.Ngram
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Realm
import kotlinx.android.synthetic.main.roman_dialog.*
import kotlinx.android.synthetic.main.roman_dialog.view.*

class RomanDialog(var txtKhmer: String, var txtRoman: String, var count: Int, val appCon: Context) : DialogFragment() {
    private var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
    private lateinit var rkDialogView: View
    private var isValid: Boolean = false

    private fun createRecord(keyword: String, roman: String) {
        if (keyword.isNotEmpty() && roman.isNotEmpty()) {
            var nextId =  DataLoader.getNextKey()
            realm.beginTransaction()
                val ngramData: Ngram = realm.createObject(Ngram::class.java, nextId)
                ngramData.keyword = keyword
                ngramData.other = roman
                ngramData.lang = KhmerLangApp.LANG_KH
                ngramData.gram = KhmerLangApp.ONE_GRAM
                ngramData.count = count
                ngramData.custom = true
                realm.insert(ngramData)
            realm.commitTransaction()
            R2KhmerService.spellingCorrector.addKhmerWord(keyword, roman)
            Toast.makeText(appCon, R.string.item_created, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(appCon, R.string.item_empty, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            rkDialogView = inflater.inflate(R.layout.roman_dialog, null)
            rkDialogView.edit_khmer.setText(txtKhmer)
            rkDialogView.edit_roman.setText(txtRoman)
            builder.setView(rkDialogView)
            isValid = false
            rkDialogView.edit_khmer.addTextChangedListener {
                validateKhmer()
            }
            rkDialogView.edit_roman.addTextChangedListener {
                validateRoman()
            }

            rkDialogView.btnSubmit.setOnClickListener {
                if (isValid) {
                    var keyword = rkDialogView.edit_khmer.text.toString()
                    var roman = rkDialogView.edit_roman.text.toString()
                    createRecord(keyword, roman)
                    dismiss()
                }
            }
            rkDialogView.btnCancel.setOnClickListener {
                dismiss()
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun isENLetters(string: String): Boolean {
        return string.none { it !in 'A'..'Z' && it !in 'a'..'z' }
    }

    private fun iskHLetters(string: String): Boolean {
//        return string.none { it !in 'ក'..'៹'}
        val arrSymbol = arrayOf('?', '​', '!')
        return string.none { it !in 'ក'..'៹' && it !in arrSymbol }
    }

    private fun validateKhmer() {
        val khmerText = rkDialogView.edit_khmer.text.toString()
        if (khmerText.isEmpty()) {
            rkDialogView.error_edit_khmer_msg.setText(R.string.error_must_not_empty)
        } else if (!iskHLetters(khmerText)) {
            rkDialogView.error_edit_khmer_msg.setText(R.string.error_must_khmer)
        } else {
            rkDialogView.error_edit_khmer_msg.text = ""
        }

        validateButtonSubmit()
    }

    private fun validateRoman() {
        val romanText = rkDialogView.edit_roman.text.toString()
        if (romanText.isEmpty()) {
            rkDialogView.error_edit_roman_msg.setText(R.string.error_must_not_empty)
        } else if (!isENLetters(romanText)) {
            rkDialogView.error_edit_roman_msg.setText(R.string.error_must_roman)
        } else {
            rkDialogView.error_edit_roman_msg.text = ""
        }

        validateButtonSubmit()
    }

    private fun validateButtonSubmit() {
        val khmerText = rkDialogView.edit_khmer.text.toString()
        val romanText = rkDialogView.edit_roman.text.toString()
        val romanError = rkDialogView.error_edit_roman_msg.text
        val khmerError = rkDialogView.error_edit_khmer_msg.text
        isValid = (khmerText.isNotEmpty() && romanText.isNotEmpty()) && (romanError.isEmpty() && khmerError.isEmpty())
        if (isValid) {
            rkDialogView.btnSubmit.setBackgroundResource(R.drawable.button)
        } else {
            rkDialogView.btnSubmit.setBackgroundResource(R.drawable.button_disable)
        }
    }
}