package com.rathanak.khmerroman.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.data.Ngram
import com.rathanak.khmerroman.databinding.RomanDialogBinding
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Realm

class RomanDialog(var txtKhmer: String, var txtRoman: String, var count: Int, val appCon: Context) : DialogFragment() {
    private var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
    private lateinit var binding: RomanDialogBinding
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
            val inflater = requireActivity().layoutInflater
            binding = RomanDialogBinding.inflate(inflater)
            binding.editKhmer.setText(txtKhmer)
            binding.editRoman.setText(txtRoman)
            builder.setView(binding.root)
            isValid = false
            binding.editKhmer.addTextChangedListener {
                validateKhmer()
            }
            binding.editRoman.addTextChangedListener {
                validateRoman()
            }

            binding.btnSubmit.setOnClickListener {
                if (isValid) {
                    var keyword = binding.editKhmer.text.toString()
                    var roman = binding.editRoman.text.toString()
                    createRecord(keyword, roman)
                    dismiss()
                }
            }
            binding.btnCancel.setOnClickListener {
                dismiss()
            }
            var dialog = builder.create()
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog
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
        val khmerText = binding.editKhmer.text.toString()
        if (khmerText.isEmpty()) {
            binding.errorEditKhmerMsg.setText(R.string.error_must_not_empty)
        } else if (!iskHLetters(khmerText)) {
            binding.errorEditKhmerMsg.setText(R.string.error_must_khmer)
        } else {
            binding.errorEditKhmerMsg.text = ""
        }

        validateButtonSubmit()
    }

    private fun validateRoman() {
        val romanText = binding.editRoman.text.toString()
        if (romanText.isEmpty()) {
            binding.errorEditRomanMsg.setText(R.string.error_must_not_empty)
        } else if (!isENLetters(romanText)) {
            binding.errorEditRomanMsg.setText(R.string.error_must_roman)
        } else {
            binding.errorEditRomanMsg.text = ""
        }

        validateButtonSubmit()
    }

    private fun validateButtonSubmit() {
        val khmerText = binding.editKhmer.text.toString()
        val romanText = binding.editRoman.text.toString()
        val romanError = binding.errorEditRomanMsg.text
        val khmerError = binding.errorEditKhmerMsg.text
        isValid = (khmerText.isNotEmpty() && romanText.isNotEmpty()) && (romanError.isEmpty() && khmerError.isEmpty())
        if (isValid) {
            binding.btnSubmit.setBackgroundResource(R.drawable.button)
        } else {
            binding.btnSubmit.setBackgroundResource(R.drawable.button_disable)
        }
    }
}