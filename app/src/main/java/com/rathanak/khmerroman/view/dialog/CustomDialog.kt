package com.rathanak.khmerroman.view.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.rathanak.khmerroman.R
import kotlinx.android.synthetic.main.roman_dialog.view.*

class CustomDialog : DialogFragment() {
    private var listener: Listener? = null
    private var title: String? = null
    private var message: String? = null
    private var positiveText: String? = null
    private var negativeText: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val rkDialogView = inflater.inflate(R.layout.roman_dialog, null)
            builder.setView(rkDialogView)
                .setPositiveButton(
                    R.string.submit,
                    DialogInterface.OnClickListener { dialog, id ->
                    })
                .setNegativeButton(
                    R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    class Builder {
        private val dialog = CustomDialog()

        fun title(t: String): Builder {
            dialog.title = t
            return this
        }

        fun message(msg: String): Builder {
            dialog.message = msg
            return this
        }

        fun listener(l: Listener): Builder {
            dialog.listener = l
            return this
        }

        fun positiveText(t: String): Builder {
            dialog.positiveText = t
            return this
        }

        fun negativeText(t: String): Builder {
            dialog.negativeText = t
            return this
        }

        fun build(): CustomDialog {
            return dialog
        }
    }

    private fun View.isVisible(v: Boolean) {
        visibility = if (v) View.VISIBLE else View.GONE
    }

    interface Listener {
        fun onPositiveSelect(dialog: CustomDialog)
        fun onNegativeSelect(dialog: CustomDialog)
    }

    companion object {
        const val TAG = "custom_dialog_tag"
    }
}