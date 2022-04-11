package com.example.heroadmin

class Player(
    var playerId : String,
    var firstName : String,
    var lastName : String,
    var age : Int,
    var totalExp : Int = getPlayerEXP(playerId),
    var healerLevels : MutableList<Int>,
    var rogueLevels : MutableList<Int>,
    var mageLevels : MutableList<Int>,
    var knightLevels : MutableList<Int>,
    var warriorLevels : MutableList<Int>,
    var guardians : List<String>, // List of phoneNumbers
) {
    var fullName = "$firstName $lastName"
    var usedExp = 0
    var remExp = totalExp - usedExp
    var classLevelsArray = mutableListOf(healerLevels, rogueLevels, mageLevels, knightLevels)
    var upgradeExpCosts : MutableList<MutableList<MutableList<Int>>> = mutableListOf()

    fun updateExp() {
        var cost = 0

        // i = class, j = subclass, k = upgrade
        // if the player's subclass level is higher than the index of the item you're at, add the item's value
        // eg. if your class(i) 0, subclass(j) 0 is on 4th level, add cost until you're at i0, j0, k3, then stop

        for (i in upgradeExpCosts.indices){
            for (j in upgradeExpCosts[i].indices){
                for (k in upgradeExpCosts[i][j]){
                    if (classLevelsArray[i][j] > j){
                        cost += upgradeExpCosts[i][j][k]
                    }
                }
            }
        }
        usedExp = cost
    }
}