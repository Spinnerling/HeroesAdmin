package com.example.heroadmin

class Player(
    var playerId : String,
    var firstName : String,
    var lastName : String,
    var age : Int,
    var totalExp : Int = getPlayerEXP(playerId) ,
    var healerLevels : MutableList<Int> = mutableListOf(0,0,0),
    var mageLevels : MutableList<Int> = mutableListOf(0,0),
    var rogueLevels : MutableList<Int> = mutableListOf(0,0,0),
    var knightLevels : MutableList<Int> = mutableListOf(0,0,0),
    var warriorLevels : MutableList<Int> = mutableListOf(0,0,0),
    var guardians : List<String>, // List of phoneNumbers
) {
    var fullName = "$firstName $lastName"
    var subclassArray = getPlayerSubclasses()

    fun findSubclassLevels(){
        var tempArray = mutableListOf(0,0,0)
        var arrayNo = 1
        var placeNo = 0
        var lastItem = 0

        for (item in subclassArray){
            if (item > lastItem) {
                lastItem = item
            }
            else {
                if (arrayNo == 1) {
                    if (placeNo >= healerLevels.size){
                        arrayNo++
                        continue
                    }
                    healerLevels[placeNo] = item
                }

                if (arrayNo == 2) {
                    if (placeNo >= healerLevels.size){
                        arrayNo++
                        continue
                    }
                    mageLevels[placeNo] = item
                }

                if (arrayNo == 3) {
                    if (placeNo >= healerLevels.size){
                        arrayNo++
                        continue
                    }
                    rogueLevels[placeNo] = item
                }

                if (arrayNo == 4) {
                    if (placeNo >= healerLevels.size){
                        arrayNo++
                        continue
                    }
                    knightLevels[placeNo] = item
                }
                // PlaceNo must restart when array is changed
                placeNo++

            }
        }
    }

}