package com.rathanak.khmerroman.keyboard.smartbar

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.textservice.SentenceSuggestionsInfo
import android.view.textservice.SpellCheckerSession
import android.view.textservice.SuggestionsInfo
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.marginLeft
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.keyboard.common.KeyData
import kotlinx.android.synthetic.main.smartbar.view.*


class SmartbarManager(
    private val r_2_khmer: R2KhmerService
) : SpellCheckerSession.SpellCheckerSessionListener {
    private var smartbarView: LinearLayout? = null
    private var isComposingEnabled: Boolean = false
    private var isShowBanner: Boolean = true
    private var spellCheckerSession: SpellCheckerSession? = null
    var isTyping: Boolean = false
    
    override fun onGetSentenceSuggestions(p0: Array<out SentenceSuggestionsInfo>?) {
    }

    override fun onGetSuggestions(p0: Array<out SuggestionsInfo>?) {
    }

    fun createSmartbarView(): LinearLayout {
        val smartbarView = View.inflate(r_2_khmer.context, R.layout.smartbar, null) as LinearLayout
        this.smartbarView = smartbarView
        this.smartbarView!!.btnOpenApp.setOnClickListener {
            launchApp()
        }
        this.smartbarView!!.toggleOption!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.ic_mini_logo)
                toggleBarLayOut(true)
                this.smartbarView!!.settingsList.visibility = View.GONE
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_chevron_right)
                toggleBarLayOut(false)
                this.smartbarView!!.settingsList.visibility = View.VISIBLE
            }
        }

        toggleBarLayOut(true)
        handleNumberClick()

        return smartbarView
    }

    fun toggleBarLayOut(show: Boolean) {
        if (this.smartbarView == null) {
            return
        }

        this.smartbarView!!.toggleOption!!.isChecked = show
        if(isTyping) {
            return
        }

        var suggestionCount = this.smartbarView!!.candidatesList.childCount
        this.smartbarView!!.settingsList.visibility = View.GONE
        if (show) {
            if (r_2_khmer.currentInputPassword) {
                this.smartbarView!!.numbersList!!.visibility = View.VISIBLE
                this.smartbarView!!.bannerContainer.visibility = View.GONE
                this.smartbarView!!.candidatesContainer.visibility = View.GONE
            } else if (suggestionCount > 0) {
                this.smartbarView!!.candidatesContainer.visibility = View.VISIBLE
                this.smartbarView!!.numbersList!!.visibility = View.GONE
                this.smartbarView!!.bannerContainer.visibility = View.GONE
            } else if(isShowBanner) {
                this.smartbarView!!.bannerContainer!!.visibility = View.VISIBLE
                this.smartbarView!!.candidatesContainer.visibility = View.GONE
                this.smartbarView!!.numbersList!!.visibility = View.GONE
            }
            // else if has text for suggestion show suggestion
        } else {
            this.smartbarView!!.bannerContainer!!.visibility = View.GONE
            this.smartbarView!!.candidatesContainer.visibility = View.GONE
            this.smartbarView!!.numbersList!!.visibility = View.GONE
        }
    }

    fun onStartInputView(isComposingEnabled: Boolean) {
        this.isComposingEnabled = isComposingEnabled
        if(isComposingEnabled) {
//            val tsm = r_2_khmer.getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE) as TextServicesManager
//            spellCheckerSession = tsm.newSpellCheckerSession(null, null, this, true)
        }

    }

    fun onFinishInputView() {
        spellCheckerSession?.close()
    }

    fun generateCandidatesFromComposing(composingText: String?) {
        if(isTyping) {
            return
        }

        if (this.smartbarView == null) {
            return
        }

        if (composingText == null) {
            this.smartbarView!!.candidatesList.removeAllViews()
        } else {
            this.smartbarView!!.candidatesList.removeAllViews()
            var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.rightMargin = 10

            for (i in 1..5) {
                val btnSuggestion = Button(r_2_khmer.context)
                btnSuggestion.layoutParams =layoutParams
                btnSuggestion.text = composingText + i.toString()
                this.smartbarView!!.candidatesList.addView(btnSuggestion)
            }
        }

        toggleBarLayOut(true)
    }

    private fun launchApp() {
        val pm = r_2_khmer.context.packageManager
        val intent:Intent? = pm.getLaunchIntentForPackage(r_2_khmer.context.packageName)
        intent?.addCategory(Intent.CATEGORY_LAUNCHER)
        if(intent!=null){
            r_2_khmer.context.startActivity(intent)
        }
    }

    private fun handleNumberClick() {
        for (numberButton in this.smartbarView!!.numbersList.children) {
            if (numberButton is Button) {
                numberButton.setOnClickListener(numberButtonOnClickListener)
            }
        }
    }

    private val numberButtonOnClickListener = View.OnClickListener { v ->
        val keyData = when (v.id) {
            R.id.btnNum0 -> KeyData(48, "0")
            R.id.btnNum1 -> KeyData(49, "1")
            R.id.btnNum2 -> KeyData(50, "2")
            R.id.btnNum3 -> KeyData(51, "3")
            R.id.btnNum4 -> KeyData(52, "4")
            R.id.btnNum5 -> KeyData(53, "5")
            R.id.btnNum6 -> KeyData(54, "6")
            R.id.btnNum7 -> KeyData(55, "7")
            R.id.btnNum8 -> KeyData(56, "8")
            R.id.btnNum9 -> KeyData(57, "9")
            else -> KeyData(0)
        }
        r_2_khmer.sendKeyPress(keyData)
    }
}