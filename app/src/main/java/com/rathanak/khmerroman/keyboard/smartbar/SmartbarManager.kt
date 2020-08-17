package com.rathanak.khmerroman.keyboard.smartbar

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.core.view.children
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.keyboard.common.KeyData
import com.rathanak.khmerroman.spelling_corrector.getEditDistance
import com.rathanak.khmerroman.view.Roman2KhmerApp
import kotlinx.android.synthetic.main.smartbar.view.*

private const val MODEL_ORDER = 5
class SmartbarManager(private val r_2_khmer: R2KhmerService) {
    private var smartbarView: LinearLayout? = null
    private var isComposingEnabled: Boolean = false
    private var isShowBanner: Boolean = true
    var isTyping: Boolean = false
    private var prevComposingText = ""

    fun createSmartbarView(): LinearLayout {
        val smartbarView = View.inflate(r_2_khmer.context, R.layout.smartbar, null) as LinearLayout
        this.smartbarView = smartbarView
        this.smartbarView!!.btnOpenApp.setOnClickListener {
            launchApp()
        }
        this.smartbarView!!.toggleOption!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.ic_home_logo_btn)
                toggleBarLayOut(true)
                this.smartbarView!!.settingsList.visibility = View.GONE
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_home_btn)
                toggleBarLayOut(false)
                this.smartbarView!!.settingsList.visibility = View.VISIBLE
            }
        }
        initToggleButton()
        toggleBarLayOut(true)
        handleNumberClick()

        return smartbarView
    }

    private fun initToggleButton() {
        this.smartbarView!!.btnToggleShowLabel.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.ic_info_on_btn)
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_info_btn)
            }
            Roman2KhmerApp.preferences?.putBoolean(KeyboardPreferences.KEY_SHOW_KEY_LABEL_VIEW, isChecked)
            r_2_khmer.reRenderKeylayout()
        }
        val isShowLabel = Roman2KhmerApp.preferences?.getBoolean(KeyboardPreferences.KEY_SHOW_KEY_LABEL_VIEW, false)
        this.smartbarView!!.btnToggleShowLabel.isChecked = isShowLabel!!

        this.smartbarView!!.btnToggleRMCorrection.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.ic_roman_sug_on_btn)
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_roman_sug_btn)
            }
            Roman2KhmerApp.preferences?.putBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, isChecked)
        }
        val isRMChecked = Roman2KhmerApp.preferences?.getBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, true)
        this.smartbarView!!.btnToggleRMCorrection.isChecked = isRMChecked!!

        this.smartbarView!!.btnToggleKHCorrection.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.ic_khmer_sug_on_btn)
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_khmer_sug_btn)
            }
            Roman2KhmerApp.preferences?.putBoolean(KeyboardPreferences.KEY_KM_CORRECTION_MODE, isChecked)
        }
        val isKMChecked = Roman2KhmerApp.preferences?.getBoolean(KeyboardPreferences.KEY_KM_CORRECTION_MODE, true)
        this.smartbarView!!.btnToggleKHCorrection.isChecked = isKMChecked!!

        this.smartbarView!!.btnToggleENCorrection.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.ic_english_sug_on_btn)
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_english_sug_btn)
            }
            Roman2KhmerApp.preferences?.putBoolean(KeyboardPreferences.KEY_EN_CORRECTION_MODE, isChecked)
        }
        val isENChecked = Roman2KhmerApp.preferences?.getBoolean(KeyboardPreferences.KEY_EN_CORRECTION_MODE, false)
        this.smartbarView!!.btnToggleENCorrection.isChecked = isENChecked!!
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
        }
    }

    fun onFinishInputView() {

    }

    fun generateCandidatesFromComposing(inputText: String, previousWord: String, composingText: String?) {
//        if (composingText == null) {
//            return
//        }
//
//        if (composingText!! == prevComposingText) {
//            return
//        }
//        prevComposingText = composingText!!

        if(isTyping) {
            return
        }

        if (this.smartbarView == null) {
            return
        }

        if (inputText.isEmpty()) {
            this.smartbarView!!.candidatesList.removeAllViews()
        } else {
            this.smartbarView!!.candidatesList.removeAllViews()
            var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            if (!composingText.isNullOrEmpty()) {
                var result = r_2_khmer.spellingCorrector.correct(previousWord, composingText)
                if (!result.isNullOrEmpty()) for(word in result) {
                    val btnSuggestion = Button(r_2_khmer.context)
                    btnSuggestion.layoutParams =layoutParams
                    btnSuggestion.text = word.toString()
                    btnSuggestion.setBackgroundColor(Color.TRANSPARENT)
                    this.smartbarView!!.candidatesList.addView(btnSuggestion)
                    btnSuggestion.setOnClickListener(candidateViewOnClickListener)
                    btnSuggestion.setOnLongClickListener(candidateViewOnLongClickListener)
                    btnSuggestion.setPadding(10,1,10,1)
                } else {
//                    if (composingText != null) {
//                        val words = r_2_khmer.segmentation.forwardSegment(composingText)
//                        if(words.size > 1) {
//                            val endWord = words.last()
//                            val toEndWord = words.take(words.size - 1).joinToString("")
//                            result = r_2_khmer.spellingCorrector.correct(words.last())
//                            if (!result.isNullOrEmpty()) for(word in result) {
//                                val btnSuggestion = Button(r_2_khmer.context)
//                                btnSuggestion.layoutParams =layoutParams
//                                btnSuggestion.text = toEndWord + word.toString()
//
//                                btnSuggestion.setBackgroundColor(Color.TRANSPARENT)
//                                this.smartbarView!!.candidatesList.addView(btnSuggestion)
//                                btnSuggestion.setOnClickListener(candidateViewOnClickListener)
//                                btnSuggestion.setOnLongClickListener(candidateViewOnLongClickListener)
//                            }
//                        }
//                    }
                }
            }
            this.smartbarView!!.candidatesScrollContainer.fullScroll(HorizontalScrollView.FOCUS_LEFT)
        }

        toggleBarLayOut(true)
    }

    fun setTypeing(typing: Boolean) {
        isTyping = typing
//        if(!isTyping) {
//
//        }
    }

    private val candidateViewOnLongClickListener = View.OnLongClickListener { v ->
        true
    }

    private val candidateViewOnClickListener = View.OnClickListener { v ->
        val view = v as Button
        val text = view.text.toString()
        if (text.isNotEmpty()) {
//            r_2_khmer.commitCandidate(text + " ")
            r_2_khmer.commitCandidate(text)
        }
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
//                prevComposingText = ""
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