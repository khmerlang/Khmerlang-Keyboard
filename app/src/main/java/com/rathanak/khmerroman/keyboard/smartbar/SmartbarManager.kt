package com.rathanak.khmerroman.keyboard.smartbar

import android.content.Context
import android.content.Intent
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.textservice.SentenceSuggestionsInfo
import android.view.textservice.SpellCheckerSession
import android.view.textservice.SuggestionsInfo
import android.view.textservice.TextServicesManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.children
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.keyboard.common.KeyData
import com.rathanak.khmerroman.keyboard.common.PageType
import kotlinx.android.synthetic.main.smartbar.view.*


class SmartbarManager(
    private val r_2_khmer: R2KhmerService
) : SpellCheckerSession.SpellCheckerSessionListener {
    private var smartbarView: LinearLayout? = null
    private var isComposingEnabled: Boolean = false
    private var spellCheckerSession: SpellCheckerSession? = null
//    private val candidateViewList: MutableList<Button> = mutableListOf()
    
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

        this.smartbarView!!.settingsList.visibility = View.GONE
        if (show) {
            if (r_2_khmer.currentInputPassword) {
                this.smartbarView!!.numbersList!!.visibility = View.VISIBLE
                this.smartbarView!!.bannerContainer.visibility = View.GONE
                this.smartbarView!!.candidatesContainer.visibility = View.GONE
            } else if (true) {
                this.smartbarView!!.candidatesContainer.visibility = View.VISIBLE
                this.smartbarView!!.numbersList!!.visibility = View.GONE
                this.smartbarView!!.bannerContainer.visibility = View.GONE
            } else {
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
        Log.i("hello", composingText.toString())
//        if (composingText == null) {
//            candidateViewList[0].text = "candidate"
//            candidateViewList[1].text = "suggestions"
//            candidateViewList[2].text = "nyi"
//        } else {
//            toggleBarLayOut(true)
//            candidateViewList[0].text = ""
//            candidateViewList[1].text = composingText + "test"
//            candidateViewList[2].text = ""
//        }
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