package com.rathanak.khmerroman.request

data class SpellCheckResultDTO(
    val startIndex: Int,
    val endIndex: Int,
    val word: String,
    val suggestions: Array<String>,
    val scores: Array<Float>
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