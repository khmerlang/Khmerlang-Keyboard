package com.rathanak.khmerroman.keyboard.smartbar

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.view.LayoutInflater
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.databinding.SmartbarBinding
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.keyboard.common.KeyData
import com.rathanak.khmerroman.keyboard.common.Styles
import com.rathanak.khmerroman.utils.DownloadData
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Realm
import kotlinx.coroutines.*

enum class SPELLCHECKER { NORMAL, VALIDATION, SPELLING_ERROR, NETWORK_ERROR, REACH_LIMIT_ERROR, TOKEN_INVALID_ERROR }

class SmartbarManager(private val r2Khmer: R2KhmerService) {
    private var binding: SmartbarBinding? = null
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
        val binding = SmartbarBinding.inflate(LayoutInflater.from(r2Khmer.context))
        this.binding = binding
        binding.btnOpenApp.setOnClickListener {
            launchApp()
        }

        binding.btnAppLogo.setOnClickListener {
            isSmartSettingOpen = !isSmartSettingOpen
            checkButtonOptionsVisibility()
            updateSmartBarView(isSmartSettingOpen)
        }
        binding.btnDownloadData.setOnClickListener {
            it.visibility = View.GONE
            binding.downloadingData.visibility = View.VISIBLE
            R2KhmerService.downloadDataPrevStatus = R2KhmerService.downloadDataStatus
            R2KhmerService.downloadDataStatus = KeyboardPreferences.STATUS_DOWNLOADING
            val download = DownloadData()
            download.downloadKeyboardData()
            listenJobDone()
        }

        binding.bannerImage.setOnClickListener {
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

        return binding.root
    }

    fun createSpellSuggestionView(): LinearLayout {
        return spellSuggestionManager.createSpellSuggestionView()
    }

    fun setDarkMood(darkMood: Boolean) {
        isDarkMood = darkMood
        spellSuggestionManager.setDarkMood(isDarkMood)
        if (this.binding != null) {
            updateByMood()
        }
    }

    fun updateSmartBarView(isSettingOpen: Boolean) {
        if (this.binding == null) {
            return
        }

        isSmartSettingOpen = isSettingOpen
        updateLogoBtnImage()
        updateSpellSuggestionView()

        if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_NONE) {
            Glide.with(r2Khmer.context)
                .load(R.drawable.banner_download_data)
                .into(this.binding!!.btnDownloadData)

            this.binding!!.noDataContainer!!.visibility = View.VISIBLE
            this.binding!!.hasDataContainer!!.visibility = View.GONE
            this.binding!!.downloadingData.visibility = View.GONE
            this.binding!!.btnDownloadData.visibility = View.VISIBLE

            return
        } else if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_DOWNLOAD_FAIL) {
            Glide.with(r2Khmer.context)
                .load(R.drawable.banner_download_data_fail)
                .into(this.binding!!.btnDownloadData)

            this.binding!!.noDataContainer!!.visibility = View.VISIBLE
            this.binding!!.hasDataContainer!!.visibility = View.GONE
            this.binding!!.downloadingData.visibility = View.GONE
            this.binding!!.btnDownloadData.visibility = View.VISIBLE

            return
        } else if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_DOWNLOADING) {
            this.binding!!.noDataContainer!!.visibility = View.VISIBLE
            this.binding!!.hasDataContainer!!.visibility = View.GONE
            this.binding!!.downloadingData.visibility = View.VISIBLE
            this.binding!!.btnDownloadData.visibility = View.GONE

            return
        } else {
            this.binding!!.noDataContainer!!.visibility = View.GONE
            this.binding!!.hasDataContainer!!.visibility = View.VISIBLE
        }

        if(isTyping) {
            return
        }

        var suggestionCount = this.binding!!.candidatesList.childCount

        this.binding!!.settingsList.visibility = View.GONE
        this.binding!!.bannerContainer!!.visibility = View.GONE
        this.binding!!.candidatesContainer.visibility = View.GONE
        this.binding!!.numbersList!!.visibility = View.GONE

        if (isSmartSettingOpen) {
            this.binding!!.settingsList.visibility = View.VISIBLE
        } else {
            if (r2Khmer.currentInputPassword) {
                this.binding!!.numbersList!!.visibility = View.VISIBLE
            } else if (suggestionCount > 0) {
                this.binding!!.candidatesContainer.visibility = View.VISIBLE
            } else {
                this.binding!!.bannerContainer!!.visibility = View.VISIBLE
            }
            // else if has text for suggestion show suggestion
        }
    }

    fun setBannerImage(bannerID: String) {
        if (this.binding == null) {
            return
        }

        if(bannerID != "") {
            Glide.with(r2Khmer.context)
                .load(bannerID)
                .error(R.drawable.banner_default_animate)
                .into(this.binding!!.bannerImage)
        } else {
            Glide.with(r2Khmer.context)
                .load(R.drawable.banner_default_animate)
                .error(R.drawable.banner_default_animate)
                .into(this.binding!!.bannerImage)
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
        if (this.binding == null) {
            return
        }

        result = emptyList()
        this.binding!!.candidatesList.removeAllViews()
        updateSmartBarView(false)
    }

    // generateCandidatesFromComposing is called both directly (e.g. from commitCandidate) and
    // indirectly, asynchronously, whenever the input connection reports a text change - so calls
    // can genuinely overlap. This generation counter, checked right before result/isCorrection are
    // published, ensures a slower/superseded call can never clobber a newer one's outcome.
    private var candidateRequestGeneration = 0

    // True when the composing text is Roman/Latin input being converted to Khmer (this app's
    // core "type romanized, get Khmer candidates" feature), as opposed to a native Khmer word
    // getting spelling-correction candidates. Space-to-select only makes sense for the former:
    // a native Khmer typist pressing space between words shouldn't have it hijacked by an
    // unrelated spelling suggestion.
    private var isRomanConversion: Boolean = false

    fun generateCandidatesFromComposing(prevOne: String, prevTwo: String, isStartSen: Boolean, composingText: String?) {
        if (this.binding == null) {
            return
        }

        if (R2KhmerService.downloadDataStatus < KeyboardPreferences.STATUS_DOWNLOADED) {
            return
        }

        val myGeneration = ++candidateRequestGeneration
        suggestionJob?.cancel()
        suggestionJob = GlobalScope.launch(Dispatchers.Main) {
            val newResult: List<String>
            val newIsCorrection: Boolean
            if (!composingText.isNullOrEmpty()) {
                delay(250)
                newResult = getSuggestion(prevOne, prevTwo, composingText, isStartSen)
                newIsCorrection = true
            } else if (isStartSen) {
                newResult = emptyList()
                newIsCorrection = true
            } else {
                newResult = getSuggestionNext(prevOne, prevTwo)
                newIsCorrection = false
            }

            if (myGeneration != candidateRequestGeneration) {
                return@launch // A newer keystroke has already superseded this request.
            }
            result = newResult
            isCorrection = newIsCorrection
            isRomanConversion = !composingText.isNullOrEmpty() && composingText.first() !in 'ក'..'ឳ'
            renderCandidates()
        }
    }

    private fun renderCandidates() {
        this.binding!!.candidatesList.removeAllViews()
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (result.isNotEmpty()) result.forEachIndexed { index, word ->
            val btnSuggestion = Button(r2Khmer.context)
            btnSuggestion.layoutParams =layoutParams
            btnSuggestion.text = word.toString()
            btnSuggestion.setTextColor(Styles.keyStyle.labelColor)
            if (index == 0 && isCorrection && isRomanConversion && isSpaceSelectFeatureEnabled()) {
                // This is the candidate that pressing space will commit (see
                // hasCandidateToSelect/selectHighlightedCandidate), highlighted the way a
                // Pinyin-style IME highlights the word space will confirm.
                btnSuggestion.background = GradientDrawable().apply {
                    cornerRadius = 12F
                    setColor(Styles.keyStyle.pressedBackgroundColor)
                }
            } else {
                btnSuggestion.setBackgroundColor(Color.TRANSPARENT)
            }
            this.binding!!.candidatesList.addView(btnSuggestion)
            btnSuggestion.setOnClickListener(candidateViewOnClickListener)
            btnSuggestion.setOnLongClickListener(candidateViewOnLongClickListener)
            btnSuggestion.setPadding(10,1,10,1)
        }
        this.binding!!.candidatesScrollContainer.fullScroll(HorizontalScrollView.FOCUS_LEFT)
        updateSmartBarView(false)
    }

    // Off by default; the user has to explicitly opt in under Settings. Also requires Roman
    // correction mode to be on, since that's what actually produces the Roman-to-Khmer
    // conversion candidates this feature selects.
    private fun isSpaceSelectFeatureEnabled(): Boolean {
        val featureEnabled = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_ENABLE_SPACE_SELECT, false) == true
        val rmCorrectionEnabled = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, true) == true
        return featureEnabled && rmCorrectionEnabled
    }

    // True only when `result` holds real Roman-to-Khmer conversion candidates for the word
    // currently being composed (as opposed to next-word predictions shown with nothing typed
    // yet, or native-Khmer spelling-correction candidates - space should not auto-select either
    // of those), and the user has turned this feature on.
    fun hasCandidateToSelect(): Boolean {
        return result.isNotEmpty() && isCorrection && isRomanConversion && isSpaceSelectFeatureEnabled()
    }

    // Commits the highlighted (first) candidate, the same way tapping it would.
    fun selectHighlightedCandidate() {
        if (hasCandidateToSelect()) {
            r2Khmer.commitCandidate(result.first(), isCorrection)
        }
    }

    fun setTyping(typing: Boolean) {
        isTyping = typing
    }

    fun destroy() {
        spellSuggestionManager.destroy()
    }

    fun performSpellChecking() {
        val inputText = r2Khmer.getCurrentText()
        if(inputText.length > 2 && isSpellSuggestionEnable() && isConnected()) {
            spellSuggestionManager.performSpellChecking(inputText)
        } else {
            spellSuggestionManager.setShortInputText()
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
        this.binding!!.smartbar.setBackgroundColor(Styles.keyboardStyle.keyboardBackground)
        DrawableCompat.setTint(this.binding!!.btnOpenApp.background, Styles.keyStyle.labelColor)
        for (numberButton in this.binding!!.numbersList.children) {
            if (numberButton is Button) {
                DrawableCompat.setTint(numberButton.background, Styles.keyStyle.normalBackgroundColor)
                numberButton.setTextColor(Styles.keyStyle.labelColor)
            }
        }
    }

    private fun isConnected(): Boolean {
        return r2Khmer.isConnected()
    }

    private fun checkButtonOptionsVisibility() {
        if(!isSmartSettingOpen) {
            return
        }

        val selectedLangIdx = KhmerLangApp.preferences?.getInt(KeyboardPreferences.KEY_CURRENT_LANGUAGE_IDX, 0)
        if(selectedLangIdx == 1) {
            this.binding!!.btnToggleRMCorrection.visibility = View.GONE//View.INVISIBLE//View.GONE
            this.binding!!.btnToggleENCorrection.visibility = View.GONE//View.INVISIBLE//View.GONE
        } else {
            this.binding!!.btnToggleRMCorrection.visibility = View.VISIBLE
            this.binding!!.btnToggleENCorrection.visibility = View.VISIBLE
        }

        if (r2Khmer.currentInputPassword) {
            this.binding!!.btnOpenApp.visibility = View.GONE
        } else {
            this.binding!!.btnOpenApp.visibility = View.VISIBLE
        }
    }
    private fun initToggleButton() {
        this.binding!!.btnToggleRMCorrection.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.ic_btn_roman)
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_btn_roman_off)
            }
            KhmerLangApp.preferences?.putBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, isChecked)
        }
        val isRMChecked = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, true)
        this.binding!!.btnToggleRMCorrection.isChecked = isRMChecked!!

        this.binding!!.btnToggleENCorrection.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.ic_btn_english)
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_btn_english_off)
            }
            KhmerLangApp.preferences?.putBoolean(KeyboardPreferences.KEY_EN_CORRECTION_MODE, isChecked)
        }
        val isENChecked = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_EN_CORRECTION_MODE, false)
        this.binding!!.btnToggleENCorrection.isChecked = isENChecked!!

        this.binding!!.btnToggleAutoCorrection.setOnCheckedChangeListener { buttonView, isChecked ->
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
        this.binding!!.btnToggleAutoCorrection.isChecked = autoTypeSpellCheck!!
    }

    private fun updateLogoBtnImage() {
        if(this.binding == null) {
            return
        }

        if (isSmartSettingOpen) {
            Glide.with(r2Khmer.context)
                .load(R.drawable.ic_btn_khmerlang_off_v2)
                .into(this.binding!!.btnAppLogo)
            return
        }

        if (!isSpellSuggestionEnable()) {
            Glide.with(r2Khmer.context)
                .load(R.drawable.ic_btn_khmerlang)
                .into(this.binding!!.btnAppLogo)
            return
        }

        if(!isConnected()) {
            spellSuggestionManager.setNoConnection()
            Glide.with(r2Khmer.context)
                .load(R.drawable.btn_base_error_v1)
                .into(this.binding!!.btnAppLogo)
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
            .into(this.binding!!.btnAppLogo)
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
    // Realm queries block, so this runs on Dispatchers.IO rather than the caller's Main
    // dispatcher. The Realm instance is opened, used, and closed within this single
    // withContext block so it stays confined to that one background thread, as Realm requires.
    private suspend fun getSuggestion(prevOne: String, prevTwo: String, composingText: String, isStartSen: Boolean): List<String> {
        return withContext(Dispatchers.IO) {
            val realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
            try {
                R2KhmerService.spellingCorrector.correct(realm, prevOne, prevTwo, composingText, isStartSen)
            } finally {
                realm.close()
            }
        }
    }

    private suspend fun getSuggestionNext(prevOne: String, prevTwo: String): List<String> {
        return withContext(Dispatchers.IO) {
            val realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
            try {
                R2KhmerService.spellingCorrector.getNextWords(realm, prevOne, prevTwo)
            } finally {
                realm.close()
            }
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
        for (numberButton in this.binding!!.numbersList.children) {
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