package com.rathanak.khmerroman.keyboard.smartbar

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.graphics.drawable.DrawableCompat
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.keyboard.common.Styles
import com.rathanak.khmerroman.request.ApiClient
import com.rathanak.khmerroman.request.SpellCheckResultDTO
import kotlinx.android.synthetic.main.smartbar.view.btnOpenApp
import kotlinx.android.synthetic.main.smartbar.view.smartbar
import kotlinx.android.synthetic.main.spell_suggestion.view.spellSuggestionList
import kotlinx.android.synthetic.main.spell_suggestion.view.noDataText
import kotlinx.android.synthetic.main.spell_suggestion.view.noInternetConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SpellSuggestionManager(private val r_2_khmer: R2KhmerService) {
    var currentSentence: String = ""
    var spellSuggestionView: LinearLayout? = null
    private var isDarkMood: Boolean = false
    var spellSuggestionAdapter: SpellSuggestionAdapter? = null
    var spellSuggestionItems: ArrayList<SpellSuggestionItem> = ArrayList()
    fun createSpellSuggestionView(): LinearLayout {
        var spellSuggestionView = View.inflate(r_2_khmer.context, R.layout.spell_suggestion, null) as LinearLayout
        this.spellSuggestionView = spellSuggestionView

        val listView = spellSuggestionView.spellSuggestionList

        var wordsList: ArrayList<WordSuggestion> = ArrayList()
        wordsList.add(WordSuggestion("ការពិនិត្យ", 14, 22))
        wordsList.add(WordSuggestion("ការពិនិត្យ", 14, 22))
        wordsList.add(WordSuggestion("ការពិនិត្យ", 14, 22))
        wordsList.add(WordSuggestion("ការពិនិត្យ", 14, 22))
        wordsList.add(WordSuggestion("ការពិនិត្យ", 14, 22))
        wordsList.add(WordSuggestion("ការពិនិត្យ", 14, 22))
        wordsList.add(WordSuggestion("ការពិនិត្យ", 14, 22))
        spellSuggestionItems.add(SpellSuggestionItem("កាពិនិត្យ", wordsList))
        spellSuggestionItems.add(SpellSuggestionItem("កាពិនិត្យ", wordsList))
        spellSuggestionItems.add(SpellSuggestionItem( "កាពិនិត្យ", wordsList))
        spellSuggestionItems.add(SpellSuggestionItem( "កាពិនិត្យ", wordsList))
        spellSuggestionItems.add(SpellSuggestionItem( "កាពិនិត្យ", wordsList))
        spellSuggestionItems.add(SpellSuggestionItem( "កាពិនិត្យ", wordsList))

        spellSuggestionAdapter = SpellSuggestionAdapter(r_2_khmer, r_2_khmer.context, spellSuggestionItems)
        listView.adapter = spellSuggestionAdapter
        val emptyView = spellSuggestionView.noDataText
        var noInternet = spellSuggestionView.noInternetConnection
        if(true) {
            listView.emptyView = emptyView
        } else {
            listView.emptyView = noInternet
        }

        return spellSuggestionView
    }

    fun setDarkMood(darkMood: Boolean) {
        isDarkMood = darkMood
        if (this.spellSuggestionView != null) {
            updateByMood()
        }
    }

    private fun updateByMood() {
        this.spellSuggestionView!!.smartbar.setBackgroundColor(Styles.keyboardStyle.keyboardBackground)
        DrawableCompat.setTint(this.spellSuggestionView!!.smartbar.btnOpenApp.background, Styles.keyStyle.labelColor)
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
        if(currentSentence.equals(sentence)) {
            return
        }

        currentSentence = sentence

        Log.i("koko", "CALL api")
//        val call = ApiClient.apiService.spellCheckIng(1)
//        call.enqueue(object : Callback<SpellCheckResultDTO> {
//            override fun onResponse(call: Call<SpellCheckResultDTO>, response: Response<SpellCheckResultDTO>) {
//                if (response.isSuccessful) {
//                    val post = response.body()
//                    // Handle the retrieved post data
//                } else {
//                    // Handle error
//                }
//            }
//
//            override fun onFailure(call: Call<SpellCheckResultDTO>, t: Throwable) {
//                // Handle failure
//            }
//        })
    }
}