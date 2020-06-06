package com.rathanak.khmerroman.core

import android.util.Log
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.module
import java.io.ObjectInputStream
import kotlinx.coroutines.async
// my libs
import com.rathanak.nlp.NGrams
import com.rathanak.nlp.LanguageModel
import com.rathanak.nlp.RankingModel
import com.rathanak.nlp.StupidBackoffRanking
import kotlinx.coroutines.Dispatchers

val predictModule = module {
//    single {
//        Log.i("hello", "Load file")
//        val fileName = "enModel"
//        val fileDescriptor = androidApplication().assets.open(fileName)
//        ObjectInputStream(fileDescriptor).use { ois ->
//            @Suppress("UNCHECKED_CAST")
//            ois.readObject() as LanguageModel
//        }
//
////        LanguageModel()
//    }
//
//    single {
//        NGrams(get())
//    }
//
//    single {
//        StupidBackoffRanking() as RankingModel
//    }
}