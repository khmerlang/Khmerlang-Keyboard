package com.rathanak.khmerroman.utils

import android.content.Context
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.serializable.NgramRecordSerializable
import com.rathanak.khmerroman.view.KhmerLangApp
import kotlinx.coroutines.*
import java.io.ObjectInputStream

class DownloadData(private val context: Context) {
    fun downloadKeyboardData() {
        R2KhmerService.jobLoadData = GlobalScope.launch(Dispatchers.Main) {
            downloadData()
        }
    }

    private suspend fun downloadData() {
        coroutineScope {
            async(Dispatchers.IO) {
                //  TODO download from internet
                val readResult = arrayListOf<NgramRecordSerializable>()
                ObjectInputStream(context.assets.open(KhmerLangApp.mobiledataFile)).use { ois ->
                    for (i in 0 until ois.readInt()) {
                        readResult.add(NgramRecordSerializable().readObject(ois))
                    }
                }

                val dataAdapter = DataLoader()
                dataAdapter.saveDataToDB(readResult)
                R2KhmerService.dataStatus = KeyboardPreferences.STATUS_DOWNLOADED
                KhmerLangApp.preferences?.putInt(KeyboardPreferences.KEY_DATA_STATUS, KeyboardPreferences.STATUS_DOWNLOADED)
                R2KhmerService.spellingCorrector.reset()
                R2KhmerService.spellingCorrector.loadData(context)
            }
        }
    }
}