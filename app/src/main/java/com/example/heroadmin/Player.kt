package com.example.heroadmin

import android.util.Log
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    var playerId : String? = null,
    var firstName : String? = null,
    var lastName : String? = null,
    var age : Int? = null,
    var exp2021 : Int? = null,
    var exp2022 : Int? = null,
    var extraExp : Int? = null,
    var healerLevels : MutableList<Int?>? = null,
    var rogueLevels : MutableList<Int?>? = null,
    var mageLevels : MutableList<Int?>? = null,
    var knightLevels : MutableList<Int?>? = null,
    var warriorLevels : MutableList<Int?>? = null,
    var guardians : List<String?>? = null,
) {
    var fullName = "$firstName $lastName"
    var totalExp = 0
    var usedExp = 0
    var remExp = totalExp - usedExp
    var classLevelsArray = mutableListOf(healerLevels, rogueLevels, mageLevels, knightLevels)
    var upgradeExpCosts : Array<Array<Array<Int?>?>>? = null
    lateinit var healerExpArray : Array<Array<Int?>?>
    lateinit var rogueExpArray : Array<Array<Int?>?>
    lateinit var mageExpArray : Array<Array<Int?>?>
    lateinit var knightExpArray : Array<Array<Int?>?>

    fun setSubclassLevel(classNo : Int, subclassNo : Int, amount : Int?){
        when (classNo){
            0 -> {
                healerLevels?.set(subclassNo, amount)
            }
            1 -> {
                rogueLevels?.set(subclassNo, amount)
            }
            2 -> {
                mageLevels?.set(subclassNo, amount)
            }
            3 -> {
                knightLevels?.set(subclassNo, amount)
            }
        }
    }

    fun updateExp() {
        var cost = 0

        // i = class, j = subclass, k = upgrade
        // if the player's subclass level is larger than the index of the item you're at, add the item's value
        // eg. if your class(i) 0, subclass(j) 0 is on 3rd level, add cost until you're at i0, j0, k2, then stop

        for (i in upgradeExpCosts?.indices ?: 0 until 0){
            for (j in upgradeExpCosts?.get(i)?.indices ?: 0 until 0){
                for (k in upgradeExpCosts?.get(i)?.get(j)?.indices ?: 0 until 0){
                    if (classLevelsArray[i]?.get(j) ?: 0 > k){
                        cost += upgradeExpCosts?.get(i)?.get(j)?.get(k) ?: 0
                        Log.i("test", "if classLevelsArray: " + classLevelsArray[i]?.get(j) + " is larger than: " + k)
                    }
                }
            }
        }
        usedExp = cost
        remExp = totalExp ?: 0 - usedExp
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