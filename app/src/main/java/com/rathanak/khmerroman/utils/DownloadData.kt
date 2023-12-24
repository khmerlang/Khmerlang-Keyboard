package com.rathanak.khmerroman.utils

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.serializable.NgramRecordSerializable
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Realm
import kotlinx.coroutines.*
import okhttp3.*
import java.io.ObjectInputStream
import java.io.IOException
import java.lang.Thread.sleep
import okhttp3.Response

class DownloadData {
    fun downloadKeyboardData(isRemoveCustom: Boolean = false) {
        R2KhmerService.jobLoadData = GlobalScope.launch(Dispatchers.Main) {
            downloadData(isRemoveCustom)
        }
    }

    private suspend fun downloadData(isRemoveCustom: Boolean = false) {
        coroutineScope {
            async(Dispatchers.IO) {
                val client = OkHttpClient()
                val builder = FormBody.Builder()
                val formBody = builder.build()
                val request: Request = Request.Builder()
                    .url(KEYBOARD_DATA_URL)
                    .post(formBody)
                    .build()
                var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
                try {

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val readResult = arrayListOf<NgramRecordSerializable>()
                        ObjectInputStream(response.body()?.byteStream()).use { ois ->
                            for (i in 0 until ois.readInt()) {
                                readResult.add(NgramRecordSerializable().readObject(ois))
                            }
                        }
                        val dataAdapter = DataLoader()
                        dataAdapter.saveDataToDB(readResult, isRemoveCustom)
                        R2KhmerService.spellingCorrector.reset()
                        R2KhmerService.spellingCorrector.loadData(realm)
                        R2KhmerService.downloadDataStatus = KeyboardPreferences.STATUS_DOWNLOADED
                        KhmerLangApp.preferences?.putInt(KeyboardPreferences.KEY_DATA_STATUS, KeyboardPreferences.STATUS_DOWNLOADED)
                    } else {
                        R2KhmerService.downloadDataStatus = KeyboardPreferences.STATUS_DOWNLOAD_FAIL
                        KhmerLangApp.preferences?.putInt(KeyboardPreferences.KEY_DATA_STATUS, KeyboardPreferences.STATUS_DOWNLOAD_FAIL)
                    }
                } catch (e: Exception) {
                    R2KhmerService.downloadDataStatus = KeyboardPreferences.STATUS_DOWNLOAD_FAIL
                    KhmerLangApp.preferences?.putInt(KeyboardPreferences.KEY_DATA_STATUS, KeyboardPreferences.STATUS_DOWNLOAD_FAIL)
                } finally {
                    realm.close()
                }
            }
        }
    }

    companion object {
        const val KEYBOARD_DATA_URL = "https://mobile.khmerlang.com/mobile-keyboard-data.bin"
    }
}