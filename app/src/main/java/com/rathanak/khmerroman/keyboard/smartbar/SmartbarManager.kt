package com.rathanak.khmerroman.keyboard.smartbar

import android.content.Intent
import android.view.View
import android.view.textservice.SentenceSuggestionsInfo
import android.view.textservice.SpellCheckerSession
import android.view.textservice.SuggestionsInfo
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.startActivity
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.keyboard.R2KhmerService
import kotlinx.android.synthetic.main.smartbar.view.*


class SmartbarManager(
    private val r_2_khmer: R2KhmerService
) : SpellCheckerSession.SpellCheckerSessionListener {
    private var smartbarView: LinearLayout? = null
    
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
                toggleOptionBarLayout(View.GONE)
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_chevron_right)
                toggleOptionBarLayout(View.VISIBLE)
            }
        }

        showBarLayOut()

        return smartbarView
    }

    private fun showBarLayOut() {}

    private fun toggleOptionBarLayout(visibility: Int) {
        this.smartbarView!!.settingsList.visibility = visibility
        // hide all other
        this.smartbarView!!.numbersList.visibility = View.GONE
        this.smartbarView!!.candidatesContainer.visibility = View.GONE
        this.smartbarView!!.bannerContainer.visibility = View.GONE
    }

    private fun launchApp() {
        val pm = r_2_khmer.context.packageManager
        val intent:Intent? = pm.getLaunchIntentForPackage(r_2_khmer.context.packageName)
        intent?.addCategory(Intent.CATEGORY_LAUNCHER)
        if(intent!=null){
            r_2_khmer.context.startActivity(intent)
        }
    }
}