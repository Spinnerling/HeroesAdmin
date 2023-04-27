package com.example.heroadmin

import android.util.Log
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    var playerId : String,
    var firstName : String? = "",
    var lastName : String? = "",
    var age : Int? = null,
    var exp2021 : Int? = 0,
    var exp2022 : Int? = 0,
    var exp2023 : Int? = 0,
    var extraExp : Int? = 0,
    var healerLevel : Int = 1,
    var rogueLevel : Int = 1,
    var mageLevel : Int = 1,
    var knightLevel : Int = 1,
    var warriorHealer : Int = 0,
    var warriorRogue : Int = 0,
    var warriorMage : Int = 0,
    var warriorKnight : Int = 0
) {
    var fullName = "$firstName $lastName"
    var totalExp = exp2021!! + exp2022!! + exp2023!! + extraExp!!
    var usedExp = 0
    var remExp = totalExp - usedExp

    fun updateExp() {
        val levelCosts = listOf(0, 50, 75, 100)

        // Calculate the total cost of each special class
        val healerExp = levelCosts[healerLevel - 1]
        val rogueExp = levelCosts[rogueLevel - 1]
        val mageExp = levelCosts[mageLevel - 1]
        val knightExp = levelCosts[knightLevel - 1]

        // Calculate the cost of unlockables
        val unlockableCost = 100
        val warriorHealerExp = if (warriorHealer == 1) unlockableCost else 0
        val warriorRogueExp = if (warriorRogue == 1) unlockableCost else 0
        val warriorMageExp = if (warriorMage == 1) unlockableCost else 0
        val warriorKnightExp = if (warriorKnight == 1) unlockableCost else 0

        // Calculate the total, used, and remaining experience
        totalExp = exp2021!! + exp2022!! + extraExp!!
        usedExp = healerExp + rogueExp + mageExp + knightExp +
                warriorHealerExp + warriorRogueExp + warriorMageExp + warriorKnightExp
        remExp = totalExp - usedExp
    }

    fun getClassLevel(classIndex: Int): Int {
        return when (classIndex) {
            0 -> healerLevel
            1 -> rogueLevel
            2 -> mageLevel
            3 -> knightLevel
            4 -> 0 // Warrior upgrades start at level 0
            else -> throw IllegalArgumentException("Invalid class index: $classIndex")
        }
    }
    fun isWarriorUpgradeUnlocked(upgrade: String): Boolean {
        return when (upgrade) {
            "warriorHealer" -> warriorHealer == 1
            "warriorRogue" -> warriorRogue == 1
            "warriorMage" -> warriorMage == 1
            "warriorKnight" -> warriorKnight == 1
            else -> false
        }
    }
}