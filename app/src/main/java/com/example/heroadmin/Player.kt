package com.example.heroadmin

import android.util.Log

class Player(
    var playerId : String,
    var firstName : String,
    var lastName : String,
    var age : Int,
    var totalExp : Int,
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
    var upgradeExpCosts : Array<Array<Array<Int>>> = arrayOf()
    lateinit var healerExpArray : Array<Array<Int>>
    lateinit var rogueExpArray : Array<Array<Int>>
    lateinit var mageExpArray : Array<Array<Int>>
    lateinit var knightExpArray : Array<Array<Int>>

    fun setSubclassLevel(classNo : Int, subclassNo : Int, amount : Int){
        when (classNo){
            0 -> {
                healerLevels[subclassNo] = amount
            }
            1 -> {
                rogueLevels[subclassNo] = amount
            }
            2 -> {
                mageLevels[subclassNo] = amount
            }
            3 -> {
                knightLevels[subclassNo] = amount
            }
        }
    }

    fun updateExp() {
        var cost = 0

        // i = class, j = subclass, k = upgrade
        // if the player's subclass level is larger than the index of the item you're at, add the item's value
        // eg. if your class(i) 0, subclass(j) 0 is on 3rd level, add cost until you're at i0, j0, k2, then stop

        for (i in upgradeExpCosts.indices){
            for (j in upgradeExpCosts[i].indices){
                for (k in upgradeExpCosts[i][j].indices){
                    if (classLevelsArray[i][j] > k){
                        cost += upgradeExpCosts[i][j][k]
                        Log.i("test", "if classLevelsArray: " + classLevelsArray[i][j] + " is larger than: " + k)
                    }
                }
            }
        }
        usedExp = cost
        remExp = totalExp - usedExp
        Log.i("test", "Player's exp updated. Total exp: " + totalExp + ", used exp: " + usedExp + ", rem exp: " + remExp)
    }

    fun getExpCosts(){
        healerExpArray = arrayOf(
            arrayOf(0, 50, 75, 100),
            arrayOf(100, 50, 75, 100),
            arrayOf(100, 50, 75, 100))

        rogueExpArray = arrayOf(
            arrayOf(0, 50, 75, 100),
            arrayOf(100, 50, 75, 100),
            arrayOf(100, 50, 75, 100))

        mageExpArray = arrayOf(
            arrayOf(0, 50, 75, 100),
            arrayOf(100, 50, 75, 100))

        knightExpArray = arrayOf(
            arrayOf(0, 50, 75, 100),
            arrayOf(100, 50, 75, 100),
            arrayOf(100, 50, 75, 100))

        upgradeExpCosts = arrayOf(healerExpArray, rogueExpArray, mageExpArray, knightExpArray)
    }
}