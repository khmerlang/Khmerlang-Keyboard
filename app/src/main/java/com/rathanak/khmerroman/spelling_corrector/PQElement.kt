package com.rathanak.khmerroman.spelling_corrector

class PQElement(var word: String="",
                var editDistance: Int=0,
                var frequency: String = ""): Comparable<PQElement> {
    override fun compareTo(element: PQElement): Int {
        if (this.editDistance > element.editDistance) {
            return 1
        } else if (this.editDistance < element.editDistance) {
            return -1
        } else if (this.frequency.toBigInteger() < element.frequency.toBigInteger()) {
            return 1
        } else if (this.frequency.toBigInteger() > element.frequency.toBigInteger()) {
            return -1
        }
        return element.frequency.compareTo(this.frequency)
    }
}