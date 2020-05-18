package com.rathanak.khmerroman.view.dialog

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import com.rathanak.khmerroman.R
import kotlinx.android.synthetic.main.enable_keyboard_dialog.view.*

class EnableKeyboardDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val rkDialogView = inflater.inflate(R.layout.enable_keyboard_dialog, null)
            builder.setView(rkDialogView)
                    .setPositiveButton(
                        R.string.ok,
                        DialogInterface.OnClickListener { dialog, id ->
                            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "enable_keyboard_dialog_tag"
    }
}