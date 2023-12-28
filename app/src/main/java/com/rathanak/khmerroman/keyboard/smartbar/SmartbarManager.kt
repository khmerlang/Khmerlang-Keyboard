package com.rathanak.khmerroman.keyboard.smartbar

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
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
import io.realm.Realm
import kotlinx.android.synthetic.main.smartbar.view.*
import kotlinx.coroutines.*

enum class SPELLCHECKER { NORMAL, VALIDATION, SPELLING_ERROR, NETWORK_ERROR, REACH_LIMIT_ERROR, TOKEN_INVALID_ERROR }

class SmartbarManager(private val r2Khmer: R2KhmerService) {
    private var smartbarView: LinearLayout? = null
    private var isComposingEnabled: Boolean = false
    private var isDarkMood: Boolean = false
    private var isTyping: Boolean = false
    var isCorrection: Boolean = true
    private var suggestionJob: Job? = null
    private var result: List<String> = emptyList()
    private var viewState: SPELLCHECKER = SPELLCHECKER.NORMAL
    private var isSmartSettingOpen: Boolean = false
    private val spellSuggestionManager: SpellSuggestionManager = SpellSuggestionManager(this, r2Khmer)
    fun createSmartbarView(): LinearLayout {
        val smartbarView = View.inflate(r2Khmer.context, R.layout.smartbar, null) as LinearLayout
        this.smartbarView = smartbarView
        this.smartbarView!!.btnOpenApp.setOnClickListener {
            launchApp()
        }

        this.smartbarView!!.btnAppLogo.setOnClickListener {
            isSmartSettingOpen = !isSmartSettingOpen
            checkButtonOptionsVisibility()
            updateSmartBarView(isSmartSettingOpen)
        }
        this.smartbarView!!.btnDownloadData.setOnClickListener {
            it.visibility = View.GONE
            this.smartbarView!!.downloadingData.visibility = View.VISIBLE
            R2KhmerService.downloadDataPrevStatus = R2KhmerService.downloadDataStatus
            R2KhmerService.downloadDataStatus = KeyboardPreferences.STATUS_DOWNLOADING
            val download = DownloadData()
            download.downloadKeyboardData()
            listenJobDone()
        }

        this.smartbarView!!.bannerImage.setOnClickListener {
            if(r2Khmer.bannerTargetUrl != "") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(r2Khmer.bannerTargetUrl))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                r2Khmer.context.startActivity(intent)
            }
        }

        initToggleButton()
        updateSmartBarView(isSmartSettingOpen)
        handleNumberClick()
        updateByMood()
        listenJobDone()

        return smartbarView
    }

    fun createSpellSuggestionView(): LinearLayout {
        return spellSuggestionManager.createSpellSuggestionView()
    }

    fun setDarkMood(darkMood: Boolean) {
        isDarkMood = darkMood
        spellSuggestionManager.setDarkMood(isDarkMood)
        if (this.smartbarView != null) {
            updateByMood()
        }
    }

    fun updateSmartBarView(isSettingOpen: Boolean) {
        if (this.smartbarView == null) {
            return
        }

        if (isSmartSettingOpen != isSettingOpen) {
            isSmartSettingOpen = isSettingOpen
            updateLogoBtnImage()
            updateSpellSuggestionView()
        }

        if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_NONE) {
            Glide.with(r2Khmer.context)
                .load(R.drawable.banner_download_data)
                .into(this.smartbarView!!.btnDownloadData)

            this.smartbarView!!.noDataContainer!!.visibility = View.VISIBLE
            this.smartbarView!!.hasDataContainer!!.visibility = View.GONE
            this.smartbarView!!.downloadingData.visibility = View.GONE
            this.smartbarView!!.btnDownloadData.visibility = View.VISIBLE

            return
        } else if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_DOWNLOAD_FAIL) {
            Glide.with(r2Khmer.context)
                .load(R.drawable.banner_download_data_fail)
                .into(this.smartbarView!!.btnDownloadData)

            this.smartbarView!!.noDataContainer!!.visibility = View.VISIBLE
            this.smartbarView!!.hasDataContainer!!.visibility = View.GONE
            this.smartbarView!!.downloadingData.visibility = View.GONE
            this.smartbarView!!.btnDownloadData.visibility = View.VISIBLE

            return
        } else if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_DOWNLOADING) {
            this.smartbarView!!.noDataContainer!!.visibility = View.VISIBLE
            this.smartbarView!!.hasDataContainer!!.visibility = View.GONE
            this.smartbarView!!.downloadingData.visibility = View.VISIBLE
            this.smartbarView!!.btnDownloadData.visibility = View.GONE

            return
        } else {
            this.smartbarView!!.noDataContainer!!.visibility = View.GONE
            this.smartbarView!!.hasDataContainer!!.visibility = View.VISIBLE
        }

        if(isTyping) {
            return
        }

        var suggestionCount = this.smartbarView!!.candidatesList.childCount

        this.smartbarView!!.settingsList.visibility = View.GONE
        this.smartbarView!!.bannerContainer!!.visibility = View.GONE
        this.smartbarView!!.candidatesContainer.visibility = View.GONE
        this.smartbarView!!.numbersList!!.visibility = View.GONE

        if (isSmartSettingOpen) {
            this.smartbarView!!.settingsList.visibility = View.VISIBLE
        } else {
            if (r2Khmer.currentInputPassword) {
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
            Glide.with(r2Khmer.context)
                .load(bannerID)
                .error(R.drawable.banner_default_animate)
                .into(this.smartbarView!!.bannerImage)
        } else {
            Glide.with(r2Khmer.context)
                .load(R.drawable.banner_default_animate)
                .error(R.drawable.banner_default_animate)
                .into(this.smartbarView!!.bannerImage)
        }
    }

    fun onStartInputView(isComposingEnabled: Boolean) {
        this.isComposingEnabled = isComposingEnabled
        if(isComposingEnabled) {
        }
    }

    fun onFinishInputView() {

    }

    fun resetSuggestionResult() {
        if (this.smartbarView == null) {
            return
        }

        result = emptyList()
        this.smartbarView!!.candidatesList.removeAllViews()
        updateSmartBarView(false)
    }

    fun generateCandidatesFromComposing(prevOne: String, prevTwo: String, isStartSen: Boolean, composingText: String?) {
        if (this.smartbarView == null) {
            return
        }

        if (R2KhmerService.downloadDataStatus < KeyboardPreferences.STATUS_DOWNLOADED) {
            return
        }

        isCorrection = true
        suggestionJob?.cancel()
        suggestionJob = GlobalScope.launch(Dispatchers.Main) {
            if (!composingText.isNullOrEmpty()) {
                delay(250)
                getSuggestion(prevOne, prevTwo, composingText, isStartSen)
                isCorrection = true
            } else {
                if(isStartSen) {
                    result = emptyList()
                } else {
                    isCorrection = false
                    getSuggestionNext(prevOne, prevTwo)
                }
            }
        }
        suggestionJob!!.invokeOnCompletion {
            this.smartbarView!!.candidatesList.removeAllViews()
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            if (result.isNotEmpty()) for(word in result) {
                val btnSuggestion = Button(r2Khmer.context)
                btnSuggestion.layoutParams =layoutParams
                btnSuggestion.text = word.toString()
                btnSuggestion.setTextColor(Styles.keyStyle.labelColor)
                btnSuggestion.setBackgroundColor(Color.TRANSPARENT)
                this.smartbarView!!.candidatesList.addView(btnSuggestion)
                btnSuggestion.setOnClickListener(candidateViewOnClickListener)
                btnSuggestion.setOnLongClickListener(candidateViewOnLongClickListener)
                btnSuggestion.setPadding(10,1,10,1)
            }
            this.smartbarView!!.candidatesScrollContainer.fullScroll(HorizontalScrollView.FOCUS_LEFT)
            updateSmartBarView(false)
        }
    }

    fun setTyping(typing: Boolean) {
        isTyping = typing
    }

    fun destroy() {
        spellSuggestionManager.destroy()
    }

    fun performSpellChecking() {
        if(isSpellSuggestionEnable() && isConnected()) {
            spellSuggestionManager.performSpellChecking(r2Khmer.getCurrentText())
        }
    }

    fun setCurrentViewState(state: SPELLCHECKER) {
        if(viewState == state) {
            return
        }

        viewState = state
        updateLogoBtnImage()
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

    private fun isConnected(): Boolean {
        return true
    }

    private fun checkButtonOptionsVisibility() {
        if(!isSmartSettingOpen) {
            return
        }

        val selectedLangIdx = KhmerLangApp.preferences?.getInt(KeyboardPreferences.KEY_CURRENT_LANGUAGE_IDX, 0)
        if(selectedLangIdx == 1) {
            this.smartbarView!!.btnToggleRMCorrection.visibility = View.GONE//View.INVISIBLE//View.GONE
            this.smartbarView!!.btnToggleENCorrection.visibility = View.GONE//View.INVISIBLE//View.GONE
        } else {
            this.smartbarView!!.btnToggleRMCorrection.visibility = View.VISIBLE
            this.smartbarView!!.btnToggleENCorrection.visibility = View.VISIBLE
        }

        if (r2Khmer.currentInputPassword) {
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

        this.smartbarView!!.btnToggleAutoCorrection.setOnCheckedChangeListener { buttonView, isChecked ->
            KhmerLangApp.preferences?.putBoolean(KeyboardPreferences.KEY_AUTO_TYPING_CORRECTION_MODE, isChecked)
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.btn_auto)
                performSpellChecking()
            } else {
                buttonView.setBackgroundResource(R.drawable.btn_auto_off)
            }
            updateSpellSuggestionView()
        }
        val autoTypeSpellCheck = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_AUTO_TYPING_CORRECTION_MODE, false)
        this.smartbarView!!.btnToggleAutoCorrection.isChecked = autoTypeSpellCheck!!
    }

    private fun updateLogoBtnImage() {
        if(this.smartbarView == null) {
            return
        }

        if(this.smartbarView!!.btnAppLogo == null) {
            return
        }

        if (isSmartSettingOpen) {
            Glide.with(r2Khmer.context)
                .load(R.drawable.ic_btn_khmerlang_off_v2)
                .into(this.smartbarView!!.btnAppLogo)
            return
        }

        if (!isSpellSuggestionEnable()) {
            Glide.with(r2Khmer.context)
                .load(R.drawable.ic_btn_khmerlang)
                .into(this.smartbarView!!.btnAppLogo)
            return
        }

        if(!isConnected()) {
            Glide.with(r2Khmer.context)
                .load(R.drawable.btn_base_error_v1)
                .into(this.smartbarView!!.btnAppLogo)
            return
        }

        var currentIcon = when(viewState) {
            SPELLCHECKER.VALIDATION -> {
                R.drawable.khmerlang_loading
            }
            SPELLCHECKER.NETWORK_ERROR -> {
                R.drawable.btn_base_error_v1
            }
            SPELLCHECKER.REACH_LIMIT_ERROR -> {
                R.drawable.btn_base_error_v1
            }
            SPELLCHECKER.TOKEN_INVALID_ERROR -> {
                R.drawable.btn_base_error_v1
            }
            SPELLCHECKER.SPELLING_ERROR -> {
                R.drawable.btn_base_error_v2
            }
            else -> {
                R.drawable.ic_btn_khmerlang
            }
        }

        Glide.with(r2Khmer.context)
            .load(currentIcon)
            .error(R.drawable.ic_btn_khmerlang)
            .into(this.smartbarView!!.btnAppLogo)
    }

    private fun updateSpellSuggestionView() {
        r2Khmer.customInputMethodView?.visibility = View.VISIBLE;
        spellSuggestionManager.spellSuggestionView?.visibility = View.GONE

        if (isSmartSettingOpen && isSpellSuggestionEnable()) {
            if (isComposingEnabled) {
                r2Khmer.customInputMethodView?.visibility = View.GONE;
                spellSuggestionManager.spellSuggestionView?.visibility = View.VISIBLE
            }
        }
    }

    private fun isSpellSuggestionEnable(): Boolean {
        val autoTypeSpellCheck = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_AUTO_TYPING_CORRECTION_MODE, false)
        return autoTypeSpellCheck == true && isComposingEnabled
    }

    //  load spell suggestion data
    private suspend fun getSuggestion(prevOne: String, prevTwo: String, composingText: String, isStartSen: Boolean) {
        coroutineScope {
            var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
            try {
                result = R2KhmerService.spellingCorrector.correct(realm, prevOne, prevTwo, composingText, isStartSen)
            } finally {
                realm.close()
            }
//            async(Dispatchers.IO) {
//                var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
//                try {
//                    result = R2KhmerService.spellingCorrector.correct(realm, prevOne, prevTwo, composingText, isStartSen)
//                } finally {
//                    realm.close()
//                }
//
//            }
        }
    }

    private  suspend fun getSuggestionNext(prevOne: String, prevTwo: String) {
        coroutineScope {
            var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
            try {
                result = R2KhmerService.spellingCorrector.getNextWords(realm, prevOne, prevTwo)
            } finally {
                realm.close()
            }
//            async(Dispatchers.IO) {
//                var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
//                try {
//                    result = R2KhmerService.spellingCorrector.getNextWords(realm, prevOne, prevTwo)
//                } finally {
//                    realm.close()
//                }
//
//            }
        }
    }

    private val candidateViewOnLongClickListener = View.OnLongClickListener { v ->
        true
    }

    private val candidateViewOnClickListener = View.OnClickListener { v ->
        val view = v as Button
        val text = view.text.toString()
        if (text.isNotEmpty()) {
//            r_2_khmer.commitCandidate(text + " ")
            r2Khmer.commitCandidate(text, isCorrection)
        }
    }

    private fun launchApp() {
        val pm = r2Khmer.context.packageManager
        val intent:Intent? = pm.getLaunchIntentForPackage(r2Khmer.context.packageName)
        intent?.addCategory(Intent.CATEGORY_LAUNCHER)
        if(intent!=null){
            r2Khmer.context.startActivity(intent)
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
        r2Khmer.sendKeyPress(keyData)
    }

    private fun listenJobDone() {
        if (R2KhmerService.jobLoadData != null) {
            R2KhmerService.jobLoadData!!.invokeOnCompletion {
                updateSmartBarView(false)
                if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_DOWNLOAD_FAIL) {
                    R2KhmerService.downloadDataStatus = R2KhmerService.downloadDataPrevStatus
                    KhmerLangApp.preferences?.putInt(KeyboardPreferences.KEY_DATA_STATUS, R2KhmerService.downloadDataPrevStatus)
                }
            }
        }
    }
}