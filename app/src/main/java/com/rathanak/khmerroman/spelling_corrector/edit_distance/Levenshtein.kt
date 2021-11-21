package com.rathanak.khmerroman.spelling_corrector.edit_distance

class Levenshtein {
    companion object {
        private fun costOfSubstitution(a: Char, b: Char): Int {
            if (a == b) {
                return 0
            }

            if(neighborsOf[a]?.contains(b) == true) {
                return 1
            }

            return 2
        }

        fun distance(lhs : CharSequence, rhs : CharSequence) : Int {
            if(lhs == rhs) { return 0 }
            if(lhs.isEmpty()) { return rhs.length }
            if(rhs.isEmpty()) { return lhs.length }

            val lhsLength = lhs.length + 1
            val rhsLength = rhs.length + 1

            var cost = Array(lhsLength) { it }
            var newCost = Array(lhsLength) { 0 }

            for (i in 1 until rhsLength) {
                newCost[0] = i

                for (j in 1 until lhsLength) {
                    val costReplace = cost[j - 1] + costOfSubstitution(lhs[j - 1], rhs[i - 1])
                    val costInsert = cost[j] + 1
                    val costDelete = newCost[j - 1] + 1

                    newCost[j] = costInsert.coerceAtMost(costDelete).coerceAtMost(costReplace)
                }

                val swap = cost
                cost = newCost
                newCost = swap
            }

            return cost[lhsLength - 1]
        }

        private var neighborsOf: HashMap<Char, Array<Char>> = hashMapOf(
            'q' to arrayOf('w', 'a'),
            'w' to arrayOf('e', 's', 'a', 'q'),
            'e' to arrayOf('r', 'd', 's', 'w'),
            'r' to arrayOf( 't', 'f', 'd', 'e'),
            't' to arrayOf( 'y', 'g', 'f', 'r'),
            'y' to arrayOf( 'u', 'h', 'g', 't'),
            'u' to arrayOf( 'i', 'j', 'h', 'y'),
            'i' to arrayOf( 'o', 'k', 'j', 'u'),
            'o' to arrayOf( 'p', 'l', 'k', 'i'),

            'p' to arrayOf( 'l', 'o'),
            'a' to arrayOf('q', 'w', 's', 'z'),
            's' to arrayOf('w', 'e', 'd', 'x', 'z', 'a'),
            'd' to arrayOf('e', 'r', 'f', 'c', 'x', 's'),
            'f' to arrayOf('r', 't', 'g', 'v', 'c', 'd'),
            'g' to arrayOf('t', 'y', 'h', 'b', 'v', 'f'),
            'h' to arrayOf('y', 'u', 'j', 'n', 'b', 'g'),
            'j' to arrayOf('u', 'i', 'k', 'm', 'n', 'h'),
            'k' to arrayOf('i', 'o', 'l', 'm', 'j'),
            'l' to arrayOf('o', 'p', 'k'),

            'z' to arrayOf('a', 's', 'x'),
            'x' to arrayOf('s', 'd', 'c', 'z'),
            'c' to arrayOf('d', 'f', 'v', 'x'),
            'v' to arrayOf('f', 'g', 'b', 'c'),
            'b' to arrayOf('g', 'h', 'n', 'v'),
            'n' to arrayOf('h', 'j', 'm', 'b'),
            'm' to arrayOf('j', 'k', 'n'),

            // first page
            'ំ' to arrayOf('េ', 'ា', 'ៃ'),
            'េ' to arrayOf('ំ' , 'ា', 'ៃ', 'ែ', 'ស'),
            'ែ' to arrayOf('េ',  'ៃ', 'ស', 'ដ', 'រ'),
            'រ' to arrayOf('ែ' , 'ស', 'ដ', 'ង', 'ត'),
            'ត' to arrayOf('រ', 'ដ', 'ង', 'យ', 'ទ'),
            'យ' to arrayOf('ត', 'ង', 'ទ', 'ុ', '្'),
            'ុ' to arrayOf('យ', 'ទ', '្', 'ញ', 'ិ'),
            'ិ' to arrayOf('ុ' , '្', 'ញ', 'ី', 'ក'),
            'ី' to arrayOf('ិ' , 'ញ', 'ក', 'ល', '់'),
            '់' to arrayOf('ី' , 'ក', 'ល'),

            'ា' to arrayOf('ំ' , 'េ', 'ៃ'),
            'ៃ' to arrayOf('ា' , 'ំ' , 'េ', 'ែ', 'ស', 'ើ'),
            'ស' to arrayOf('ៃ' , 'េ', 'ែ', 'ើ', 'ដ', 'រ', 'ច'),
            'ដ' to arrayOf('ស', 'ែ', 'រ', 'ច', 'ង', 'ត', 'ជ'),
            'ង' to arrayOf('ដ', 'រ', 'ត', 'ជ', 'យ', 'ទ', 'ព'),
            'ទ' to arrayOf('ង', 'ត', 'យ', 'ព', 'ុ', '្', 'ប'),
            '្' to arrayOf('ទ', 'យ', 'ុ', 'ប', 'ញ', 'ិ', 'ន'),
            'ញ' to arrayOf('្' , 'ុ', 'ិ', 'ន', 'ី', 'ក', 'ម'),
            'ក' to arrayOf('ញ', 'ិ', 'ី', 'ម', 'ល', '់'),
            'ល' to arrayOf('ក', 'ី', '់'),

            'ើ' to arrayOf('ៃ' ,'ស', 'ច'),
            'ច' to arrayOf('ើ' ,'ស', 'ដ', 'ជ'),
            'ជ' to arrayOf('ច', 'ដ', 'ង', 'ព'),
            'ព' to arrayOf('ជ', 'ង', 'ទ', 'ប'),
            'ប' to arrayOf('ព', 'ទ', '្', 'ន'),
            'ន' to arrayOf('ប', '្', 'ញ', 'ម'),
            'ម' to arrayOf('ន', 'ញ', 'ក'),

            'ឈ' to arrayOf('ឆ', 'ៀ', 'ឿ'),
            'ឆ' to arrayOf('ឈ', 'ៀ', 'ឿ', 'ឹ', 'ឌ'),
            'ឹ' to arrayOf('ឆ', 'ឿ', 'ឌ', 'ឺ', 'ធ'),
            'ឺ' to arrayOf('ឹ' , 'ឌ', 'ធ', 'ួ', 'ថ'),
            'ួ' to arrayOf('ឺ' , 'ធ', 'ថ', 'ូ', 'អ'),
            'ូ' to arrayOf('ួ' , 'ថ', 'ដ', 'ោ', 'ះ'),
            'ោ' to arrayOf('ូ' , 'ដ', 'ះ', 'ៅ', 'ហ'),
            'ៅ' to arrayOf('ោ' , 'ះ','ហ', 'ភ', 'គ'),
            'ភ' to arrayOf('ៅ' ,'ហ', 'គ', 'ផ', 'ឡ'),
            'ផ' to arrayOf('ភ', 'គ', 'ឡ'),

            'ៀ' to arrayOf('ឈ', 'ឆ', 'ឿ'),
            'ឿ' to arrayOf('ៀ' , 'ឈ', 'ឆ', 'ឹ', 'ឌ', 'ឋ'),
            'ឌ' to arrayOf('ឿ' , 'ឆ', 'ឹ', 'ឋ', 'ឺ', 'ធ', 'ឍ'),
            'ធ' to arrayOf('ឌ', 'ឹ', 'ឺ', 'ឍ', 'ួ', 'ថ', 'ខ'),
            'ថ' to arrayOf('ធ', 'ឺ', 'ួ', 'ខ', 'ូ', 'អ', 'ឃ'),
            'អ' to arrayOf('ថ', 'ួ', 'ូ', 'ឃ', 'ោ', 'ះ', 'វ'),
            'ះ' to arrayOf('អ', 'ូ', 'ោ', 'វ', 'ៅ', 'ហ', 'ណ'),
            'ហ' to arrayOf('ះ' , 'ោ', 'ៅ', 'ណ', 'ភ', 'គ', 'ឲ'),
            'គ' to arrayOf('ហ', 'ៅ', 'ភ', 'ឲ', 'ផ', 'ឡ'),
            'ឡ' to arrayOf('គ', 'ភ', 'ផ'),

            'ឋ' to arrayOf('ឿ', 'ឌ', 'ឍ'),
            'ឍ' to arrayOf('ឋ', 'ឌ', 'ធ', 'ខ'),
            'ខ' to arrayOf('ឍ', 'ធ', 'ថ', 'ឃ'),
            'ឃ' to arrayOf('ខ', 'ថ', 'អ', 'វ'),
            'វ' to arrayOf('ឃ', 'អ', 'ះ', 'ណ'),
            'ណ' to arrayOf('វ', 'ះ', 'ហ', 'ឲ'),
            'ឲ' to arrayOf('ណ', 'ហ', 'គ')

//            'ឋ' to arrayOf('ា', 'ៀ', 'ស', '័', 'ខ', 'ឦ', 'ឿ'),
//            'ឿ' to arrayOf('ា', 'ៀ', 'ស', '័', 'ខ', 'ឦ', 'ឋ'),
//            'ខ' to arrayOf('ស', '័', 'ដ', '៍', 'ច', 'ឭ', 'ឋ', 'ឿ', 'ឦ'),
//            'ឦ' to arrayOf('ស', '័', 'ដ', '៍', 'ច', 'ឭ', 'ឋ', 'ឿ', 'ខ'),
//            'ច' to arrayOf('ដ', '៍', 'ថ', '៏', 'វ', 'ឰ', 'ខ', 'ឦ', 'ឭ'),
//            'ឭ' to arrayOf('ដ', '៍', 'ថ', '៏', 'វ', 'ឰ', 'ខ', 'ឦ', 'ច'),
//            'វ' to arrayOf('ថ', '៏', 'ង', '៌', 'ប', 'ឧ', 'ច', 'ឭ', 'ឰ'),
//            'ឰ' to arrayOf('ថ', '៏', 'ង', '៌', 'ប', 'ឧ', 'ច', 'ឭ', 'វ'),
//            'ប' to arrayOf('ង', '៌', 'ហ', 'ៈ', 'ន', 'ឯ', 'វ', 'ឰ', 'ឧ'),
//            'ឧ' to arrayOf('ង', '៌', 'ហ', 'ៈ', 'ន', 'ឯ', 'វ', 'ឰ', 'ប'),
//            'ន' to arrayOf('ហ', 'ៈ', '្', 'ម', 'ប', 'ឧ', 'ឯ'),
//            'ឯ' to arrayOf('ហ', 'ៈ', '្', 'ម', 'ប', 'ឧ', 'ន'),
//            'ម' to arrayOf('្', 'ក', 'ន', 'ឯ'),
//
//            'ឈ' to arrayOf('ឺ'),
//            'ឺ' to arrayOf('ែ', 'ៃ', 'ា', 'ឈ'),
//            'ែ' to arrayOf('ឬ', 'ឌ', 'ៃ', 'ឺ'),
//            'ឬ' to arrayOf( 'ទ', 'ធ', 'ឌ', 'ែ'),
//            'ទ' to arrayOf( 'ួ', 'អ', 'ធ', 'ឬ'),
//            'ួ' to arrayOf( 'ូ', 'ះ', 'អ', 'ទ'),
//            'ូ' to arrayOf( 'ី', 'ញ', 'ះ', 'ួ'),
//            'ី' to arrayOf( 'ៅ', 'គ', 'ញ', 'ូ'),
//            'ៅ' to arrayOf( 'ភ', 'ឡ', 'គ', 'ី'),
//            'ភ' to arrayOf( 'ឡ', 'ៅ'),
//
//            'ៃ' to arrayOf('ឺ', 'ែ', 'ឌ', 'ឃ', 'ឍ'),
//            'ឌ' to arrayOf('ែ', 'ឬ', 'ធ', 'ជ', 'ឃ', 'ៃ'),
//            'ធ' to arrayOf('ឬ', 'ទ', 'អ', '៽', 'ជ', 'ឌ'),
//            'អ' to arrayOf('ទ', 'ួ', 'ះ', 'ព', '៽', 'ធ'),
//            'ះ' to arrayOf('ួ', 'ូ', 'ញ', 'ណ', 'ព', 'អ'),
//            'ញ' to arrayOf('ូ', 'ី', 'គ', 'ំ', 'ណ', 'ះ'),
//            'គ' to arrayOf('ី', 'ៅ', 'ឡ', 'ំ', 'ញ'),
//            'ឡ' to arrayOf('ៅ', 'ភ', 'គ'),
//
//            'ឍ' to arrayOf('ៃ', 'ឃ'),
//            'ឃ' to arrayOf('ៃ', 'ឌ', 'ជ', 'ឍ'),
//            'ជ' to arrayOf('ឌ', 'ធ', '៽', 'ឃ'),
//            '៽' to arrayOf('ធ', 'អ', 'ព', 'ជ'),
//            'ព' to arrayOf('អ', 'ះ', 'ណ', '៽'),
//            'ណ' to arrayOf('ះ', 'ញ', 'ំ', 'ព'),
//            'ំ' to arrayOf('ញ', 'គ', 'ណ')

//            'ឆ' to arrayOf('ឹ', 'ា'),
//            'ឹ' to arrayOf('េ', 'ស', 'ា', 'ឆ'),
//            'េ' to arrayOf('រ', 'ដ', 'ស', 'ឹ'),
//            'រ' to arrayOf( 'ត', 'ថ', 'ដ', 'េ'),
//            'ត' to arrayOf( 'យ', 'ង', 'ថ', 'រ'),
//            'យ' to arrayOf( 'ុ', 'ហ', 'ង', 'ត'),
//            'ុ' to arrayOf( 'ិ', '្', 'ហ', 'យ'),
//            'ិ' to arrayOf( 'ោ', 'ក', '្', 'ុ'),
//            'ោ' to arrayOf( 'ផ', 'ល', 'ក', 'ិ'),
//            'ផ' to arrayOf( 'ល', 'ោ', 'ៀ', 'ើ', 'ឥ'),
//
//            'ៀ' to arrayOf( 'ផ', 'ើ', '់', 'ឪ', 'ឥ', 'ឲ'),
//            'ឪ' to arrayOf('់', 'ៀ', 'ឭ', 'ឥ', 'ឲ'),
//            'ឥ' to arrayOf('ផ', 'ៀ', 'ឪ', 'ឲ'),
//            'ឲ' to arrayOf('ៀ', 'ឪ', 'ឥ'),
//
//            'ា' to arrayOf('ឆ', 'ឹ', 'ស', 'ឋ'),
//            'ស' to arrayOf('ឹ', 'េ', 'ដ', 'ខ', 'ឋ', 'ា'),
//            'ដ' to arrayOf('េ', 'រ', 'ថ', 'ច', 'ខ', 'ស'),
//            'ថ' to arrayOf('រ', 'ត', 'ង', 'វ', 'ច', 'ដ'),
//            'ង' to arrayOf('ត', 'យ', 'ហ', 'ប', 'វ', 'ថ'),
//            'ហ' to arrayOf('យ', 'ុ', '្', 'ន', 'ប', 'ង'),
//            '្' to arrayOf('ុ', 'ិ', 'ក', 'ម', 'ន', 'ហ'),
//            'ក' to arrayOf('ិ', 'ោ', 'ល', 'ម', '្'),
//            'ល' to arrayOf('ោ', 'ផ', 'ក'),
//
//            'ើ' to arrayOf('ល', 'ផ', '់'),
//            '់' to arrayOf('ឭ', 'ើ', '៊', 'ៀ'),
//            'ឭ' to arrayOf( 'ៀ', 'ឪ', '់'),
//
//            'ឋ' to arrayOf('ា', 'ស', 'ខ'),
//            'ខ' to arrayOf('ស', 'ដ', 'ច', 'ឋ'),
//            'ច' to arrayOf('ដ', 'ថ', 'វ', 'ខ'),
//            'វ' to arrayOf('ថ', 'ង', 'ប', 'ច'),
//            'ប' to arrayOf('ង', 'ហ', 'ន', 'វ'),
//            'ន' to arrayOf('ហ', '្', 'ម', 'ប'),
//            'ម' to arrayOf('្', 'ក', 'ន'),
//
//            'ឈ' to arrayOf('ឺ'),
//            'ឺ' to arrayOf('ែ', 'ៃ', 'ា', 'ឈ'),
//            'ែ' to arrayOf('ឬ', 'ឌ', 'ៃ', 'ឺ'),
//            'ឬ' to arrayOf( 'ទ', 'ធ', 'ឌ', 'ែ'),
//            'ទ' to arrayOf( 'ួ', 'អ', 'ធ', 'ឬ'),
//            'ួ' to arrayOf( 'ូ', 'ះ', 'អ', 'ទ'),
//            'ូ' to arrayOf( 'ី', 'ញ', 'ះ', 'ួ'),
//            'ី' to arrayOf( 'ៅ', 'គ', 'ញ', 'ូ'),
//            'ៅ' to arrayOf( 'ភ', 'ឡ', 'គ', 'ី', 'ឫ'),
//            'ភ' to arrayOf( 'ឡ', 'ៅ', 'ឭ', 'ឿ'),
//
//            'ឿ' to arrayOf( 'ភ', 'ឯ', 'ឰ', 'ឦ'),
//            'ឰ' to arrayOf('ឿ' , 'ឧ'),
//            'ឧ' to arrayOf('ឦ', 'ឰ'),
//            'ឦ' to arrayOf('ៀ', 'ឧ', 'ឭ'),
//
//            'ៃ' to arrayOf('ឺ', 'ែ', 'ឌ', 'ឃ', 'ឍ'),
//            'ឌ' to arrayOf('ែ', 'ឬ', 'ធ', 'ជ', 'ឃ', 'ៃ'),
//            'ធ' to arrayOf('ឬ', 'ទ', 'អ', '៽', 'ជ', 'ឌ'),
//            'អ' to arrayOf('ទ', 'ួ', 'ះ', 'ព', '៽', 'ធ'),
//            'ះ' to arrayOf('ួ', 'ូ', 'ញ', 'ណ', 'ព', 'អ'),
//            'ញ' to arrayOf('ូ', 'ី', 'គ', 'ំ', 'ណ', 'ះ'),
//            'គ' to arrayOf('ី', 'ៅ', 'ឡ', 'ំ', 'ញ'),
//            'ឡ' to arrayOf('ៅ', 'ភ', 'គ'),
//            'ឯ' to arrayOf('ឿ' , '៉'),
//            'ឍ' to arrayOf('ៃ', 'ឃ'),
//            'ឃ' to arrayOf('ៃ', 'ឌ', 'ជ', 'ឍ'),
//            'ជ' to arrayOf('ឌ', 'ធ', '៽', 'ឃ'),
//            '៽' to arrayOf('ធ', 'អ', 'ព', 'ជ'),
//            'ព' to arrayOf('អ', 'ះ', 'ណ', '៽'),
//            'ណ' to arrayOf('ះ', 'ញ', 'ំ', 'ព'),
//            'ំ' to arrayOf('ញ', 'គ', 'ណ')
        )
    }
}