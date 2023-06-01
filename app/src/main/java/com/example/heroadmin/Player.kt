package com.example.heroadmin

import android.util.Log
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    var playerId: String,
    var firstName: String? = "",
    var lastName: String? = "",
    var age: Int? = null,
    var exp2021: Int? = 0,
    var exp2022: Int? = 0,
    var exp2023: Int? = 0,
    var healerLevel: Int = 1,
    var rogueLevel: Int = 1,
    var mageLevel: Int = 1,
    var knightLevel: Int = 1,
    var healerUltimateA: Boolean = true,
    var rogueUltimateA: Boolean = true,
    var mageUltimateA: Boolean = true,
    var knightUltimateA: Boolean = true,
    var healerUltimateB: Boolean = true,
    var rogueUltimateB: Boolean = true,
    var mageUltimateB: Boolean = true,
    var knightUltimateB: Boolean = true,
    var warriorHealer: Boolean = false,
    var warriorRogue: Boolean = false,
    var warriorMage: Boolean = false,
    var warriorKnight: Boolean = false,
    var bookerNames: MutableList<String> = mutableListOf(),
    var bookerEmails: MutableList<String> = mutableListOf(),
    var bookerPhones: MutableList<String> = mutableListOf(),
    var bookerAddresses: MutableList<String> = mutableListOf(),
) {
    var fullName = "$firstName $lastName"
    var totalExp = exp2023!!
    var usedExp = 0
        private set
    val remExp get() = totalExp - usedExp

    fun updateUsedExp() {
        val levelCosts = listOf(0, 50, 75, 0)

        // Calculate the total cost of each special class
        val healerExp = levelCosts.take(healerLevel).sum()
        val rogueExp = levelCosts.take(rogueLevel).sum()
        val mageExp = levelCosts.take(mageLevel).sum()
        val knightExp = levelCosts.take(knightLevel).sum()

        // Calculate the cost of unlockables
        val unlockableCost = 100
        val warriorHealerExp = if (warriorHealer) unlockableCost else 0
        val warriorRogueExp = if (warriorRogue) unlockableCost else 0
        val warriorMageExp = if (warriorMage) unlockableCost else 0
        val warriorKnightExp = if (warriorKnight) unlockableCost else 0

        // Calculate the cost of ultimates
        val ultimateCost = 100
        val healerUltimateAExp = if (healerUltimateA) ultimateCost else 0
        val healerUltimateBExp = if (healerUltimateB) ultimateCost else 0
        val rogueUltimateAExp = if (rogueUltimateA) ultimateCost else 0
        val rogueUltimateBExp = if (rogueUltimateB) ultimateCost else 0
        val mageUltimateAExp = if (mageUltimateA) ultimateCost else 0
        val mageUltimateBExp = if (mageUltimateB) ultimateCost else 0
        val knightUltimateAExp = if (knightUltimateA) ultimateCost else 0
        val knightUltimateBExp = if (knightUltimateB) ultimateCost else 0

        // Calculate the total, used, and remaining experience
        totalExp = exp2023!!
        usedExp = healerExp + rogueExp + mageExp + knightExp +
                warriorHealerExp + warriorRogueExp + warriorMageExp + warriorKnightExp +
                healerUltimateAExp + healerUltimateBExp +
                rogueUltimateAExp + rogueUltimateBExp +
                mageUltimateAExp + mageUltimateBExp +
                knightUltimateAExp + knightUltimateBExp

    }

    fun getClassLevel(classNum: Int): Int {
        return when (classNum) {
            0 -> healerLevel
            1 -> rogueLevel
            2 -> mageLevel
            3 -> knightLevel
            else -> 0 // Warrior
        }
    }

    fun setClassLevel(classNum: Int, level: Int) {
        when (classNum) {
            0 -> healerLevel = level
            1 -> rogueLevel = level
            2 -> mageLevel = level
            3 -> knightLevel = level
        }
        updateUsedExp()
    }

    fun getUltimateA(classNum: Int): Boolean {
        return when (classNum) {
            0 -> healerUltimateA
            1 -> rogueUltimateA
            2 -> mageUltimateA
            3 -> knightUltimateA
            else -> false
        }
    }

    fun getUltimateB(classNum: Int): Boolean {
        return when (classNum) {
            0 -> healerUltimateB
            1 -> rogueUltimateB
            2 -> mageUltimateB
            3 -> knightUltimateB
            else -> false
        }
    }

    fun setUltimateA(classNum: Int, value: Boolean) {
        when (classNum) {
            0 -> healerUltimateA = value
            1 -> rogueUltimateA = value
            2 -> mageUltimateA = value
            3 -> knightUltimateA = value
        }
        updateUsedExp()
    }

    fun setUltimateB(classNum: Int, value: Boolean) {
        when (classNum) {
            0 -> healerUltimateB = value
            1 -> rogueUltimateB = value
            2 -> mageUltimateB = value
            3 -> knightUltimateB = value
        }
        updateUsedExp()
    }


}