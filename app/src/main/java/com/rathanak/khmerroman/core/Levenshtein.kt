package com.rathanak.khmerroman.core

fun LevenshteinDistance(str1 : String , str2 : String) : Int {

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
                arr[row][col] = 1 + min( arr[row][col-1] , arr[row-1][col] , arr[row-1][col-1] )
            }
        }
    }

    return arr[str1.length][str2.length]
}