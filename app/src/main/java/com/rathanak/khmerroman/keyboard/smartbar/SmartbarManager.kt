package com.rathanak.khmerroman.keyboard.smartbar

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.keyboard.common.KeyData
import com.rathanak.khmerroman.keyboard.common.Styles
import com.rathanak.khmerroman.utils.DownloadData
import com.rathanak.khmerroman.view.KhmerLangApp
import kotlinx.android.synthetic.main.activity_roman_mapping.*
import kotlinx.android.synthetic.main.smartbar.view.*

class SmartbarManager(private val r_2_khmer: R2KhmerService) {
    private var smartbarView: LinearLayout? = null
    private var isComposingEnabled: Boolean = false
    private var isShowBanner: Boolean = true
    private var isDarkMood: Boolean = false
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
                buttonView.setBackgroundResource(R.drawable.ic_btn_khmerlang)
                toggleBarLayOut(true)
                this.smartbarView!!.settingsList.visibility = View.GONE
            } else {
                checkButtonOptionsVisibility()
                buttonView.setBackgroundResource(R.drawable.ic_btn_khmerlang_off)
                toggleBarLayOut(false)
                this.smartbarView!!.settingsList.visibility = View.VISIBLE
            }
        }
        this.smartbarView!!.btnDownloadData.setOnClickListener {
            it.visibility = View.GONE
            this.smartbarView!!.downloadingData.visibility = View.VISIBLE
            R2KhmerService.dataStatus = KeyboardPreferences.STATUS_DOWNLOADING
            val download = DownloadData(r_2_khmer.context)
            download.downloadKeyboardData()
            listenJobDone()
        }

        this.smartbarView!!.bannerImage.setOnClickListener {
            if(r_2_khmer.bannerTargetUrl != "") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(r_2_khmer.bannerTargetUrl))
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                r_2_khmer.context.startActivity(intent)
            }
        }

        initToggleButton()
        toggleBarLayOut(true)
        handleNumberClick()
        updateByMood()
        listenJobDone()
        return smartbarView
    }

    fun setDarkMood(darkMood: Boolean) {
        isDarkMood = darkMood
        if (this.smartbarView != null) {
            updateByMood()
        }
    }

    private fun updateByMood() {
        this.smartbarView!!.smartbar.setBackgroundColor(Styles.keyboardStyle.keyboardBackground)
        DrawableCompat.setTint(this.smartbarView!!.smartbar.btnOpenApp.background, Styles.keyStyle.labelColor)
        for (numberButton in this.smartbarView!!.numbersList.children) {
            if (numberButton is Button) {
                DrawableCompat.setTint(numberButton.background, Styles.keyStyle.normalBackgroundColor)
                numberButton.setTextColor(Styles.keyStyle.labelColor)
            }
        }
    }

    private fun checkButtonOptionsVisibility() {
        val selectedLangIdx = KhmerLangApp.preferences?.getInt(KeyboardPreferences.KEY_CURRENT_LANGUAGE_IDX, 0)
        if(selectedLangIdx == 1) {
            this.smartbarView!!.btnToggleRMCorrection.visibility = View.INVISIBLE//View.GONE
            this.smartbarView!!.btnToggleENCorrection.visibility = View.INVISIBLE//View.GONE
        } else {
            this.smartbarView!!.btnToggleRMCorrection.visibility = View.VISIBLE
            this.smartbarView!!.btnToggleENCorrection.visibility = View.VISIBLE
        }

        if (r_2_khmer.currentInputPassword) {
            this.smartbarView!!.btnOpenApp.visibility = View.GONE
        } else {
            this.smartbarView!!.btnOpenApp.visibility = View.VISIBLE
        }
    }
    private fun initToggleButton() {
        this.smartbarView!!.btnToggleRMCorrection.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.ic_btn_roman)
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_btn_roman_off)
            }
            KhmerLangApp.preferences?.putBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, isChecked)
        }
        val isRMChecked = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, true)
        this.smartbarView!!.btnToggleRMCorrection.isChecked = isRMChecked!!

        this.smartbarView!!.btnToggleENCorrection.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.ic_btn_english)
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_btn_english_off)
            }
            KhmerLangApp.preferences?.putBoolean(KeyboardPreferences.KEY_EN_CORRECTION_MODE, isChecked)
        }
        val isENChecked = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_EN_CORRECTION_MODE, false)
        this.smartbarView!!.btnToggleENCorrection.isChecked = isENChecked!!
    }

    fun toggleBarLayOut(show: Boolean) {
        if (this.smartbarView == null) {
            return
        }

        if (R2KhmerService.dataStatus == KeyboardPreferences.STATUS_NONE) {
            Glide.with(r_2_khmer.context)
                .load(R.drawable.banner_download_data)
                .error(R.drawable.banner_default_animate)
                .into(this.smartbarView!!.btnDownloadData);

            this.smartbarView!!.noDataContainer!!.visibility = View.VISIBLE
            this.smartbarView!!.hasDataContainer!!.visibility = View.GONE
            this.smartbarView!!.downloadingData.visibility = View.GONE
            this.smartbarView!!.btnDownloadData.visibility = View.VISIBLE

            return
        } else if (R2KhmerService.dataStatus == KeyboardPreferences.STATUS_DOWNLOADING) {
            this.smartbarView!!.noDataContainer!!.visibility = View.VISIBLE
            this.smartbarView!!.hasDataContainer!!.visibility = View.GONE
            this.smartbarView!!.downloadingData.visibility = View.VISIBLE
            this.smartbarView!!.btnDownloadData.visibility = View.GONE

            return
        } else {
            this.smartbarView!!.noDataContainer!!.visibility = View.GONE
            this.smartbarView!!.hasDataContainer!!.visibility = View.VISIBLE
        }

        this.smartbarView!!.toggleOption!!.isChecked = show
        if(isTyping) {
            return
        }

        var suggestionCount = this.smartbarView!!.candidatesList.childCount

        this.smartbarView!!.settingsList.visibility = View.GONE
        this.smartbarView!!.bannerContainer!!.visibility = View.GONE
        this.smartbarView!!.candidatesContainer.visibility = View.GONE
        this.smartbarView!!.numbersList!!.visibility = View.GONE
        if (show) {
            if (r_2_khmer.currentInputPassword) {
                this.smartbarView!!.numbersList!!.visibility = View.VISIBLE
            } else if (suggestionCount > 0) {
                this.smartbarView!!.candidatesContainer.visibility = View.VISIBLE
            } else {
                this.smartbarView!!.bannerContainer!!.visibility = View.VISIBLE
            }
            // else if has text for suggestion show suggestion
        }
    }

    fun setBannerImage(bannerID: String) {
        if (this.smartbarView == null) {
            return
        }

        if(bannerID != "") {
            Glide.with(r_2_khmer.context)
                .load(bannerID)
                .error(R.drawable.banner_default_animate)
                .into(this.smartbarView!!.bannerImage);
        } else {
            Glide.with(r_2_khmer.context)
                .load(R.drawable.banner_default_animate)
                .error(R.drawable.banner_default_animate)
                .into(this.smartbarView!!.bannerImage);
        }
    }

    fun onStartInputView(isComposingEnabled: Boolean) {
        this.isComposingEnabled = isComposingEnabled
        if(isComposingEnabled) {
        }
    }

    fun onFinishInputView() {

    }

    fun generateCandidatesFromComposing(inputText: String, prevOne: String, prevTwo: String, isStartSen: Boolean,composingText: String?) {
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

        if (R2KhmerService.dataStatus > KeyboardPreferences.STATUS_DOWNLOADED) {
            return
        }

        if (inputText.isEmpty()) {
            this.smartbarView!!.candidatesList.removeAllViews()
        } else {
            this.smartbarView!!.candidatesList.removeAllViews()
            var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            if (!composingText.isNullOrEmpty()) {
                var result = R2KhmerService.spellingCorrector.correct(prevOne, prevTwo, composingText, isStartSen)
                if (!result.isNullOrEmpty()) for(word in result) {
                    val btnSuggestion = Button(r_2_khmer.context)
                    btnSuggestion.layoutParams =layoutParams
                    btnSuggestion.text = word.toString()
                    btnSuggestion.setTextColor(Styles.keyStyle.labelColor)
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

    private fun listenJobDone() {
        if (R2KhmerService.jobLoadData != null) {
            R2KhmerService.jobLoadData!!.invokeOnCompletion {
                toggleBarLayOut(true)
            }
        }
    }
}