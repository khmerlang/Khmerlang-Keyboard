package com.rathanak.khmerroman.utils

import android.content.Context
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.keyboard.R2KhmerService
import kotlinx.coroutines.*

class DownloadData(private val context: Context) {
    fun downloadKeyboardData() {
        R2KhmerService.jobLoadData = GlobalScope.launch(Dispatchers.Main) {
            downloadData()
        }
    }

    private suspend fun downloadData() {
        coroutineScope {
            async(Dispatchers.IO) {
//                delay(5000)
                R2KhmerService.dataStatus = KeyboardPreferences.STATUS_DOWNLOADED
//                R2KhmerService.spellingCorrector.reset()
//                R2KhmerService.spellingCorrector.loadData(context)

//            //  TODO run download and save to DB in background
//            val readResult = arrayListOf<NgramRecordSerializable>()
//            applicationContext.assets.open(KhmerLangApp.mobiledataFile)
////            applicationContext.assets.
////            ObjectInputStream(FileInputStream("ddd.bin")).use { ois ->
//            ObjectInputStream(applicationContext.assets.open(KhmerLangApp.mobiledataFile)).use { ois ->
//                val start = System.nanoTime()
//                for (i in 0 until ois.readInt()) {
//                    readResult.add(NgramRecordSerializable().readObject(ois))
//                }
//            }
//            Log.d("khmerlang", readResult[0].ngram.keyword)
            }
        }
    }
}