package com.rathanak.khmerroman.keyboard.smartbar

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.graphics.drawable.DrawableCompat
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.keyboard.common.Styles
import com.rathanak.khmerroman.request.ApiClient
import com.rathanak.khmerroman.request.SpellCheckRequestDTO
import com.rathanak.khmerroman.request.SpellCheckRespondDTO
import com.rathanak.khmerroman.request.SpellCheckResultDTO
import com.rathanak.khmerroman.request.SpellSelectRequestDTO
import com.rathanak.khmerroman.request.SpellSelectRespondDTO
import kotlinx.android.synthetic.main.smartbar.view.btnOpenApp
import kotlinx.android.synthetic.main.smartbar.view.smartbar
import kotlinx.android.synthetic.main.spell_suggestion.view.spellSuggestionList
import kotlinx.android.synthetic.main.spell_suggestion.view.noDataText
import kotlinx.android.synthetic.main.spell_suggestion.view.smartSpellSuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SpellSuggestionManager(private val smartBar: SmartbarManager, private val r2Khmer: R2KhmerService) {
    private var spellCheckJob: Job? = null
    private var currentSentence: String = ""
    var spellSuggestionView: LinearLayout? = null
    private var isDarkMood: Boolean = false
    var spellSuggestionAdapter: SpellSuggestionAdapter? = null
    private var spellSuggestionItems: ArrayList<SpellCheckResultDTO> = ArrayList<SpellCheckResultDTO>()
    fun createSpellSuggestionView(): LinearLayout {
        var spellSuggestionView = View.inflate(r2Khmer.context, R.layout.spell_suggestion, null) as LinearLayout
        this.spellSuggestionView = spellSuggestionView
        val listSuggestionView = spellSuggestionView.spellSuggestionList
        spellSuggestionAdapter = SpellSuggestionAdapter(this, r2Khmer.context, spellSuggestionItems)
        listSuggestionView.adapter = spellSuggestionAdapter
        listSuggestionView.emptyView = spellSuggestionView.noDataText


        updateByMood()
        return spellSuggestionView
    }

    fun setDarkMood(darkMood: Boolean) {
        isDarkMood = darkMood
        if (this.spellSuggestionView != null) {
            updateByMood()
        }
    }

    private fun updateByMood() {
        this.spellSuggestionView!!.smartSpellSuggestion.setBackgroundColor(Styles.keyStyle.normalBackgroundColor)
        this.spellSuggestionView!!.spellSuggestionList.setBackgroundColor(Styles.keyStyle.normalBackgroundColor)
//        DrawableCompat.setTint(this.spellSuggestionView!!.smartbar.btnOpenApp.background, Styles.keyStyle.labelColor)
//        for (numberButton in this.spellSuggestionView!!.numbersList.children) {
//            if (numberButton is Button) {
//                DrawableCompat.setTint(numberButton.background, Styles.keyStyle.normalBackgroundColor)
//                numberButton.setTextColor(Styles.keyStyle.labelColor)
//            }
//        }
    }
    fun destroy() {
    }

    fun performSpellChecking(sentence: String) {
        if(currentSentence == sentence) {
            return
        }

        spellCheckJob?.cancel()

        spellSuggestionAdapter?.suggestionsList?.clear()
        spellSuggestionAdapter?.notifyDataSetChanged()

        if (sentence.isEmpty()) {
            smartBar.setCurrentViewState(SPELLCHECKER.NORMAL)
            spellSuggestionAdapter?.suggestionsList?.clear()
            spellSuggestionAdapter?.notifyDataSetChanged()
            manageEmptyList(R.string.spell_suggestion_no_typo, R.color.colorPrimary)
            return
        } else if (sentence.length <= 2) {
            smartBar.setCurrentViewState(SPELLCHECKER.NORMAL)
            spellSuggestionAdapter?.suggestionsList?.clear()
            spellSuggestionAdapter?.notifyDataSetChanged()
            manageEmptyList(R.string.spell_suggestion_text_too_short, R.color.colorPrimary)
            return
        }

        currentSentence = sentence
        manageEmptyList(R.string.spell_suggestion_loading, R.color.colorPrimary)
        smartBar.setCurrentViewState(SPELLCHECKER.VALIDATION)
        spellCheckJob = GlobalScope.launch(Dispatchers.Main) {
            delay(500)
            spellChecking(currentSentence)
        }
    }

    fun setCurrentText(typoWord: String, selectText: String, startPos: Int, endPos: Int) {
        r2Khmer.setCurrentText(typoWord, selectText, startPos, endPos)
        currentSentence = r2Khmer.getCurrentText()
        performSpellSelect(typoWord, selectText)
    }

    fun onCallEmptyResult() {
        manageEmptyList(R.string.spell_suggestion_no_typo, R.color.colorPrimary)
        smartBar.setCurrentViewState(SPELLCHECKER.NORMAL)
    }

    private fun performSpellSelect(typo: String, selected: String) {
        val requestBody = SpellSelectRequestDTO(typo, selected)
        val call = ApiClient.apiService.spellWordSelection(requestBody)
        call.enqueue(object : Callback<SpellSelectRespondDTO> {
            override fun onFailure(call: Call<SpellSelectRespondDTO>, t: Throwable) {
                // Handle failure
            }

            override fun onResponse(
                call: Call<SpellSelectRespondDTO>,
                response: Response<SpellSelectRespondDTO>
            ) {
                // Handle success
            }
        })
    }

    private fun manageEmptyList(emptyMessageID: Int, colorId: Int) {
        spellSuggestionView?.noDataText?.setText(emptyMessageID)
        spellSuggestionView?.noDataText?.setTextColor(r2Khmer.getColorInt(colorId))
    }

    private fun spellChecking(searchText: String) {
        val requestBody = SpellCheckRequestDTO(searchText)
        val call = ApiClient.apiService.spellCheckIng(requestBody)
        call.enqueue(object : Callback<SpellCheckRespondDTO> {
            override fun onResponse(call: Call<SpellCheckRespondDTO>, response: Response<SpellCheckRespondDTO>) {
                if (response.isSuccessful) {
                    // Handle the retrieved spell check data
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (!responseBody.results.isNullOrEmpty()) {
                            smartBar.setCurrentViewState(SPELLCHECKER.SPELLING_ERROR)
                            spellSuggestionAdapter?.suggestionsList = responseBody.results
                            spellSuggestionAdapter?.notifyDataSetChanged()
                        } else {
                            smartBar.setCurrentViewState(SPELLCHECKER.NORMAL)
                        }

                        manageEmptyList(R.string.spell_suggestion_no_typo, R.color.colorPrimary)
                    }
                } else {
                    // Handle error
                    val errorCode = response.code()
                    if(errorCode == 400) {
                        smartBar.setCurrentViewState(SPELLCHECKER.NORMAL)
                        manageEmptyList(R.string.spell_suggestion_no_internet, R.color.danger)
                    }else if(errorCode == 429) {
                        smartBar.setCurrentViewState(SPELLCHECKER.REACH_LIMIT_ERROR)
                        manageEmptyList(R.string.spell_suggestion_react_limit, R.color.danger)
                    }else if(errorCode == 401) {
                        smartBar.setCurrentViewState(SPELLCHECKER.TOKEN_INVALID_ERROR)
                        manageEmptyList(R.string.spell_suggestion_token_invalid, R.color.danger)
                    }else {
                        smartBar.setCurrentViewState(SPELLCHECKER.NETWORK_ERROR)
                        manageEmptyList(R.string.spell_suggestion_no_internet, R.color.danger)
                    }
                }
            }

            override fun onFailure(call: Call<SpellCheckRespondDTO>, t: Throwable) {
                // Handle failure
                smartBar.setCurrentViewState(SPELLCHECKER.NETWORK_ERROR)
                manageEmptyList(R.string.spell_suggestion_no_internet, R.color.danger)
            }
        })
    }
}