package com.rathanak.khmerroman.request

import com.google.gson.annotations.SerializedName

data class SpellCheckResultDTO(
    @SerializedName("start_index")
    var startIndex: Int,
    @SerializedName("end_index")
    var endIndex: Int,
    @SerializedName("word")
    val word: String,
    @SerializedName("suggestions")
    val suggestions: Array<String>,
    @SerializedName("scores")
    val scores: Array<Float>,
    @SerializedName("type")
    val type: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpellCheckResultDTO

        if (startIndex != other.startIndex) return false
        if (endIndex != other.endIndex) return false
        if (word != other.word) return false
        if (!suggestions.contentEquals(other.suggestions)) return false
        return scores.contentEquals(other.scores)
    }

    override fun hashCode(): Int {
        var result = startIndex
        result = 31 * result + endIndex
        result = 31 * result + word.hashCode()
        result = 31 * result + suggestions.contentHashCode()
        result = 31 * result + scores.contentHashCode()
        return result
    }
}

data class SpellCheckRespondDTO(val results: ArrayList<SpellCheckResultDTO>)

data class SpellCheckRequestDTO (val text: String)

data class SpellSelectRequestDTO(val word: String, val selected: String)
data class SpellSelectRespondDTO(val status: String)