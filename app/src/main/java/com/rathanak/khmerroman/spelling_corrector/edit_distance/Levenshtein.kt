package com.rathanak.khmerroman.spelling_corrector.edit_distance

fun LevenshteinDistance(str1 : String , str2 : String) : Int {
    if (str1 == str2)  return 0
    if (str1 == "") return str2.length
    if (str2 == "") return str1.length

    fun array2dOfInt(sizeOuter: Int, sizeInner: Int): Array<IntArray>
            = Array(sizeOuter) { IntArray(sizeInner) }

    fun min( a : Int , b : Int , c : Int ) : Int  = Math.min( a , Math.min(b,c) )

    val arr = array2dOfInt(str1.length + 1 , str2.length + 1)

    for (col in 0..str2.length)
        arr[0][col] = col

    for( row in 1..str1.length )
        arr[row][0] = row

    for( row in 1..str1.length ){
        for( col in 1..str2.length ){

            if( str1[row-1] == str2[col-1] ) {
                arr[row][col] = arr[row - 1][col - 1]
            }
            else{
//                arr[row][col] = 2 + min( arr[row][col-1] , arr[row-1][col] , arr[row-1][col-1] )
                arr[row][col] = 1 + min( arr[row][col-1] , arr[row-1][col] , arr[row-1][col-1] )
            }
        }
    }

    return arr[str1.length][str2.length]
}

//fun LevenshteinDistance(tmpA: String, tmpB: String): Int {
////        val tmpA = a.toLowerCase()
////        val tmpB = b.toLowerCase()
//    // special cases
//    if (tmpA == tmpB)  return 0
//    if (tmpA == "") return tmpB.length
//    if (tmpB == "") return tmpA.length
//
//    val costs = IntArray(tmpB.length + 1)
//    for (j in costs.indices) costs[j] = j
//    for (i in 1..tmpA.length) {
//        costs[0] = i
//        var nw = i - 1
//        for (j in 1..tmpB.length) {
//            val cj = Math.min(
//                1 + Math.min(costs[j], costs[j - 1]),
//                if (tmpA[i - 1] === tmpB[j - 1]) nw else nw + 1
//            )
//            nw = costs[j]
//            costs[j] = cj
//        }
//    }
//    return costs[tmpB.length]
//}