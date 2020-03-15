package com.rathanak.khmerroman.settings

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.preference.Preference
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceScreen
import android.provider.Settings
import android.text.TextUtils
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager

/* package private */ internal class InputMethodSettingsImpl : InputMethodSettingsInterface {
    private var mSubtypeEnablerPreference: Preference? = null
    private var mInputMethodSettingsCategoryTitleRes: Int = 0
    private var mInputMethodSettingsCategoryTitle: CharSequence? = null
    private var mSubtypeEnablerTitleRes: Int = 0
    private var mSubtypeEnablerTitle: CharSequence? = null
    private var mSubtypeEnablerIconRes: Int = 0
    private var mSubtypeEnablerIcon: Drawable? = null
    private var mImm: InputMethodManager? = null
    private var mImi: InputMethodInfo? = null
    private var mContext: Context? = null

    /**
     * Initialize internal states of this object.
     * @param context the context for this application.
     * @param prefScreen a PreferenceScreen of PreferenceActivity or PreferenceFragment.
     * @return true if this application is an IME and has two or more subtypes, false otherwise.
     */
    fun init(context: Context, prefScreen: PreferenceScreen): Boolean {
        mContext = context
        mImm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mImi = getMyImi(context, mImm)
        if (mImi == null || mImi!!.subtypeCount <= 1) {
            return false
        }
        mSubtypeEnablerPreference = Preference(context)
        mSubtypeEnablerPreference!!.onPreferenceClickListener = OnPreferenceClickListener {
            val title = getSubtypeEnablerTitle(context)
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS)
            intent.putExtra(Settings.EXTRA_INPUT_METHOD_ID, mImi!!.id)
            if (!TextUtils.isEmpty(title)) {
                intent.putExtra(Intent.EXTRA_TITLE, title)
            }
            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
            true
        }
        prefScreen.addPreference(mSubtypeEnablerPreference)
        updateSubtypeEnabler()
        return true
    }

    private fun getMyImi(context: Context, imm: InputMethodManager?): InputMethodInfo? {
        val imis = imm!!.inputMethodList
        for (i in imis.indices) {
            val imi = imis[i]
            if (imis[i].packageName == context.packageName) {
                return imi
            }
        }
        return null
    }

    private fun getEnabledSubtypesLabel(
        context: Context?, imm: InputMethodManager?, imi: InputMethodInfo?): String? {
        if (context == null || imm == null || imi == null) return null
        val subtypes = imm.getEnabledInputMethodSubtypeList(imi, true)
        val sb = StringBuilder()
        val N = subtypes.size
        for (i in 0 until N) {
            val subtype = subtypes[i]
            if (sb.length > 0) {
                sb.append(", ")
            }
            sb.append(subtype.getDisplayName(context, imi.packageName,
                imi.serviceInfo.applicationInfo))
        }
        return sb.toString()
    }

    /**
     * {@inheritDoc}
     */
    override fun setInputMethodSettingsCategoryTitle(resId: Int) {
        mInputMethodSettingsCategoryTitleRes = resId
        updateSubtypeEnabler()
    }

    /**
     * {@inheritDoc}
     */
    override fun setInputMethodSettingsCategoryTitle(title: CharSequence) {
        mInputMethodSettingsCategoryTitleRes = 0
        mInputMethodSettingsCategoryTitle = title
        updateSubtypeEnabler()
    }

    /**
     * {@inheritDoc}
     */
    override fun setSubtypeEnablerTitle(resId: Int) {
        mSubtypeEnablerTitleRes = resId
        updateSubtypeEnabler()
    }

    /**
     * {@inheritDoc}
     */
    override fun setSubtypeEnablerTitle(title: CharSequence) {
        mSubtypeEnablerTitleRes = 0
        mSubtypeEnablerTitle = title
        updateSubtypeEnabler()
    }

    /**
     * {@inheritDoc}
     */
    override fun setSubtypeEnablerIcon(resId: Int) {
        mSubtypeEnablerIconRes = resId
        updateSubtypeEnabler()
    }

    /**
     * {@inheritDoc}
     */
    override fun setSubtypeEnablerIcon(drawable: Drawable) {
        mSubtypeEnablerIconRes = 0
        mSubtypeEnablerIcon = drawable
        updateSubtypeEnabler()
    }

    private fun getSubtypeEnablerTitle(context: Context): CharSequence? {
        return if (mSubtypeEnablerTitleRes != 0) {
            context.getString(mSubtypeEnablerTitleRes)
        } else {
            mSubtypeEnablerTitle
        }
    }

    fun updateSubtypeEnabler() {
        if (mSubtypeEnablerPreference != null) {
            if (mSubtypeEnablerTitleRes != 0) {
                mSubtypeEnablerPreference!!.setTitle(mSubtypeEnablerTitleRes)
            } else if (!TextUtils.isEmpty(mSubtypeEnablerTitle)) {
                mSubtypeEnablerPreference!!.title = mSubtypeEnablerTitle
            }
            val summary = getEnabledSubtypesLabel(mContext, mImm, mImi)
            if (!TextUtils.isEmpty(summary)) {
                mSubtypeEnablerPreference!!.summary = summary
            }
            if (mSubtypeEnablerIconRes != 0) {
                mSubtypeEnablerPreference!!.setIcon(mSubtypeEnablerIconRes)
            } else if (mSubtypeEnablerIcon != null) {
                mSubtypeEnablerPreference!!.icon = mSubtypeEnablerIcon
            }
        }
    }
}