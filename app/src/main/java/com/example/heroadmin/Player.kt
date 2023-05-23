package com.example.heroadmin

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
    var healerLevel: Int = 1,
    var rogueLevel: Int = 1,
    var mageLevel: Int = 1,
    var knightLevel: Int = 1,
    var healerUltimateA: Boolean = true,
    var rogueUltimateA: Boolean = true,
    var mageUltimateA: Boolean = true,
    var knightUltimateA: Boolean = true,
    var warriorHealer: Int = 0,
    var warriorRogue: Int = 0,
    var warriorMage: Int = 0,
    var warriorKnight: Int = 0,
    var bookerNames : MutableList<String> = mutableListOf(),
    var bookerEmails : MutableList<String> = mutableListOf(),
    var bookerPhones : MutableList<String> = mutableListOf(),
    var bookerAddresses : MutableList<String> = mutableListOf(),
) {
    var fullName = "$firstName $lastName"
    var totalExp = exp2021!! + exp2022!! + exp2023!! + extraExp!!
    var usedExp = 0
    var remExp = totalExp - usedExp


    fun updateExp() {
        val levelCosts = listOf(0, 50, 75, 100, 100)

        // Calculate the total cost of each special class
        val healerExp = if (healerLevel > 0) levelCosts[healerLevel - 1] else 0
        val rogueExp = if (rogueLevel > 0) levelCosts[rogueLevel - 1] else 0
        val mageExp = if (mageLevel > 0) levelCosts[mageLevel - 1] else 0
        val knightExp = if (knightLevel > 0) levelCosts[knightLevel - 1] else 0

        // Calculate the cost of unlockables
        val unlockableCost = 100
        val warriorHealerExp = if (warriorHealer == 1) unlockableCost else 0
        val warriorRogueExp = if (warriorRogue == 1) unlockableCost else 0
        val warriorMageExp = if (warriorMage == 1) unlockableCost else 0
        val warriorKnightExp = if (warriorKnight == 1) unlockableCost else 0

        // Calculate the total, used, and remaining experience
        totalExp = exp2021!! + exp2022!! + exp2023!! + extraExp!!
        usedExp = healerExp + rogueExp + mageExp + knightExp +
                warriorHealerExp + warriorRogueExp + warriorMageExp + warriorKnightExp
        remExp = totalExp - usedExp
    }

    fun getClassLevel(mainClass: Int): Int {
        return when (mainClass) {
            0 -> healerLevel
            1 -> rogueLevel
            2 -> mageLevel
            3 -> knightLevel
            else -> 0 // Warrior
        }
    }
//    fun isWarriorUpgradeUnlocked(upgrade: String): Boolean {
//        return when (upgrade) {
//            "warriorHealer" -> warriorHealer == 1
//            "warriorRogue" -> warriorRogue == 1
//            "warriorMage" -> warriorMage == 1
//            "warriorKnight" -> warriorKnight == 1
//            else -> false
//        }
//    }
    fun upgradeClass(mainClass: Int, level: Int) {
        when (mainClass) {
            0 -> healerLevel = level
            1 -> rogueLevel = level
            2 -> mageLevel = level
            3 -> knightLevel = level
            4 -> {
                when (level) {
                    1 -> warriorHealer = 1
                    2 -> warriorRogue = 1
                    3 -> warriorMage = 1
                    4 -> warriorKnight = 1
                }
            }
        }
        updateExp()
    }
//    fun removeOtherUltimateUpgrades(mainClass: Int) {
//        when (mainClass) {
//            0 -> {
//                rogueLevel = min(rogueLevel, 3)
//                mageLevel = min(mageLevel, 3)
//                knightLevel = min(knightLevel, 3)
//            }
//            1 -> {
//                healerLevel = min(healerLevel, 3)
//                mageLevel = min(mageLevel, 3)
//                knightLevel = min(knightLevel, 3)
//            }
//            2 -> {
//                healerLevel = min(healerLevel, 3)
//                rogueLevel = min(rogueLevel, 3)
//                knightLevel = min(knightLevel, 3)
//            }
//            3 -> {
//                healerLevel = min(healerLevel, 3)
//                rogueLevel = min(rogueLevel, 3)
//                mageLevel = min(mageLevel, 3)
//            }
//        }
//    }
//
//    fun getOwnedLevels(mainClass: Int, isSpecialSection: Boolean): List<Int> {
//        val classLevel = getClassLevel(mainClass)
//        val numOfLevels = if (isSpecialSection) 5 else 4
//        val ownedLevels = mutableListOf<Int>()
//
//        for (level in 1..numOfLevels) {
//            if (classLevel >= level || (isSpecialSection && level == 1)) {
//                ownedLevels.add(level)
//            }
//        }
//
//        return ownedLevels
//    }
//
//    fun hasChosenUltimateOne(mainClass: Int): Boolean {
//        return when (mainClass) {
//            0 -> healerUltimateOne
//            1 -> rogueUltimateOne
//            2 -> mageUltimateOne
//            3 -> knightUltimateOne
//            else -> false
//        }
//    }
//
//    fun chooseUltimateOne(mainClass: Int) {
//        when (mainClass) {
//            0 -> healerUltimateOne = true
//            1 -> rogueUltimateOne = true
//            2 -> mageUltimateOne = true
//            3 -> knightUltimateOne = true
//        }
//    }
//
//    fun chooseUltimateTwo(mainClass: Int) {
//        when (mainClass) {
//            0 -> healerUltimateOne = false
//            1 -> rogueUltimateOne = false
//            2 -> mageUltimateOne = false
//            3 -> knightUltimateOne = false
//        }
//    }
}