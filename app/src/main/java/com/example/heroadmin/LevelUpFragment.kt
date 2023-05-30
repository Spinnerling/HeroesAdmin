package com.example.heroadmin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.heroadmin.databinding.FragmentLevelUpBinding
import kotlinx.coroutines.launch

class LevelUpFragment : Fragment() {
    private lateinit var binding: FragmentLevelUpBinding
    private lateinit var player: Player
    private lateinit var v: View
    private lateinit var DBF: DatabaseFunctions
    private lateinit var currPlayerId: String
    private var currSection = 0
    private lateinit var mainClassLevel: IntArray
    private lateinit var buttonList: Array<ImageButton>
    private lateinit var warriorButtonList: Array<ImageButton>
    private lateinit var warriorExpTextArray: Array<TextView>
    private lateinit var specialSection: LinearLayout
    private lateinit var warriorSection: LinearLayout
    private var isSpecialSection: Boolean = false
    private var currentRole: Int = 0

    private val expArray: Array<IntArray> = arrayOf(
        intArrayOf(0, 50, 75, 100, 100), // Healer levels' experience costs
        intArrayOf(0, 50, 75, 100, 100), // Rogue levels' experience costs
        intArrayOf(0, 50, 75, 100, 100), // Mage levels' experience costs
        intArrayOf(0, 50, 75, 100, 100), // Knight levels' experience costs
        intArrayOf(0, 100, 100, 100, 100) // Warrior levels' experience costs
    )

    private lateinit var expTextArray: Array<TextView>
    private lateinit var args: LevelUpFragmentArgs
    private var currEventId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_level_up, container, false)
        v = binding.root
        args = LevelUpFragmentArgs.fromBundle(requireArguments())
        currPlayerId = args.passedPlayerId
        currEventId = args.passedEventId
        lifecycleScope.launch {
            try {
                DBF = DatabaseFunctions(v.context)
                val player = DBF.getPlayer(currPlayerId)
                player?.updateExp()
            } catch (e: Exception) {
                // handle the exception
            }
        }
        mainClassLevel = IntArray(5)

        binding.levelUpPlayerNameText.text = player.fullName
        updateExpText()

        specialSection = binding.specialClassList
        warriorSection = binding.warriorList

        buttonList = arrayOf(
            binding.upgrade1button,
            binding.upgrade2button,
            binding.upgrade3button,
            binding.upgrade4button,
            binding.upgrade5button
        )

        warriorButtonList = arrayOf(
            binding.warrior1button,
            binding.warrior2button,
            binding.warrior3button,
            binding.warrior4button,
            binding.upgrade5button
        )

        expTextArray = arrayOf(
            binding.upgrade1exp,
            binding.upgrade2exp,
            binding.upgrade3exp,
            binding.upgrade4exp,
            binding.upgrade5exp
        )

        warriorExpTextArray = arrayOf(
            binding.warrior1exp,
            binding.warrior2exp,
            binding.warrior3exp,
            binding.warrior4exp,
            binding.upgrade5exp
        )

        with(binding) {
            levelUpHealerButton.setOnClickListener { changeSection(R.color.healerColor, 0) }
            levelUpRogueButton.setOnClickListener { changeSection(R.color.rogueColor, 1) }
            levelUpMageButton.setOnClickListener { changeSection(R.color.mageColor, 2) }
            levelUpKnightButton.setOnClickListener { changeSection(R.color.knightColor, 3) }
            levelUpWarriorButton.setOnClickListener {
                changeSection(R.color.warriorColor, 4)
            }
        }

        binding.levelUpBackButton.setOnClickListener {
            findNavController().navigate(
                LevelUpFragmentDirections.actionLevelUpFragmentToEventView(
                    currEventId
                )
            )
        }

        val buttons = arrayOf(
            binding.upgrade1button,
            binding.upgrade2button,
            binding.upgrade3button,
            binding.upgrade4button,
            binding.upgrade5button,
            binding.warrior1button,
            binding.warrior2button,
            binding.warrior3button,
            binding.warrior4button,
            binding.upgrade5button
        )

        for (button in buttons) {
            button.setOnClickListener { buttonClick(button) }
        }

        changeSection(R.color.healerColor, 0)

        return binding.root
    }

    private fun updateExpText() {
        player.updateExp()
        binding.levelUpExpRemText.text = player.remExp.toString()
        Log.i("test", "Updated exp text. Rem exp: " + player.remExp)
    }

    private fun buttonClick(button: ImageButton) {
        val level = buttonList.indexOf(button)
        if (level == -1) {
            Log.i("test", "Could not find upgrade button")
            return
        } else {
            val mainClass = currSection

            if (player.getClassLevel(mainClass) == level + 1 && level + 1 > 1 && !isUltimateUpgrade(
                    mainClass,
                    level
                )
            ) {
                // Case 3: Remove the current level (except level 1 and ultimate upgrades)
                player.upgradeClass(mainClass, level)
                updateUpgrades(currSection)
                updateExpText()
            } else if (player.getClassLevel(mainClass) > level + 1) {
                // Case 2: Set the level to the pressed upgrade level
                player.upgradeClass(mainClass, level + 1)
                updateUpgrades(currSection)
                updateExpText()
            } else if (player.remExp >= expArray[mainClass][level] || (isSpecialSection && level == 0 && currentRole == currSection)) {
                // Case 1: Buy the upgrade if the player has enough experience or it's a warrior unlockable
                if (isUltimateUpgrade(mainClass, level)) {
                    if (mainClass == 3 && level == 3) {
                        // Special case for knight's second ultimate
                        player.upgradeClass(mainClass, level)
                        setChosenUltimate(mainClass, false) // Choose ultimate B (false) for knight
                    } else if (mainClass == 3 && level == 4) {
                        player.upgradeClass(mainClass, level)
                        setChosenUltimate(mainClass, true) // Choose ultimate A (true) for knight
                    } else {
                        setChosenUltimate(mainClass, true) // Choose ultimate A (true) for other classes
                    }
                } else {
                    removeOtherUltimateUpgrades(mainClass)
                }
                player.upgradeClass(mainClass, level + 1)
                player.remExp -= expArray[mainClass][level] // Deduct the upgrade cost from the player's remaining XP
                updateUpgrades(currSection)
                updateExpText()
            }
        }
    }

    private fun setChosenUltimate(mainClass: Int, isUltimateA: Boolean) {
        when (mainClass) {
            0 -> {
                player.healerUltimateA = isUltimateA
            }
            1 -> {
                player.rogueUltimateA = isUltimateA
            }
            2 -> {
                player.mageUltimateA = isUltimateA
            }
            3 -> {
                player.knightUltimateA = isUltimateA
            }
        }
    }

    private fun isUltimateUpgrade(mainClass: Int, level: Int): Boolean {
        return level == 3 || level == 4
    }

    private fun removeOtherUltimateUpgrades(mainClass: Int) {
        when (mainClass) {
            0 -> {
                player.rogueUltimateA = false
                player.mageUltimateA = false
                player.knightUltimateA = false
            }

            1 -> {
                player.healerUltimateA = false
                player.mageUltimateA = false
                player.knightUltimateA = false
            }

            2 -> {
                player.healerUltimateA = false
                player.rogueUltimateA = false
                player.knightUltimateA = false
            }

            3 -> {
                player.healerUltimateA = false
                player.rogueUltimateA = false
                player.mageUltimateA = false
            }
        }
    }

    private fun updateUpgrades(currentRole: Int) {
        val currentExpTextArray = if (isSpecialSection) expTextArray else warriorExpTextArray
        val classLevel = player.getClassLevel(currentRole)
        val numOfLevels = if (currentRole != 4) 4 else 5

        for (level in 0 until numOfLevels) {
            if (level < currentExpTextArray.size && level < buttonList.size) {
                val isOwned =
                    (classLevel >= level + 1 && !(level == 3 && !getUltimateStatus(currentRole))) || (isSpecialSection && level == 0 && currentRole != 4 && currentRole == this.currentRole)
                val expText = if (isOwned) "OWNED" else "${expArray[currentRole][level]} EXP"
                currentExpTextArray[level].text = expText
                setButtonOwnership(buttonList[level], isOwned, true)
            }
        }

        Log.i("test", "Player Level for Role $currentRole: ${player.getClassLevel(currentRole)}")
        Log.i("test", "Player Ultimate for Role $currentRole: ${getUltimateStatus(currentRole)}")
    }

    private fun getUltimateStatus(mainClass: Int): Boolean {
        return when (mainClass) {
            0 -> player.healerUltimateA
            1 -> player.rogueUltimateA
            2 -> player.mageUltimateA
            3 -> player.knightUltimateA
            else -> false
        }
    }

    private fun changeSection(colorRes: Int, currentRole: Int) {
        currSection = currentRole
        binding.subclassListBackground.background.setTint(resources.getColor(colorRes))
        this.currentRole = currentRole

        if (currentRole != 4) {
            isSpecialSection = true
            specialSection.visibility = View.VISIBLE
            warriorSection.visibility = View.GONE
            updateUpgrades(currentRole)
        } else {
            isSpecialSection = false
            specialSection.visibility = View.GONE
            warriorSection.visibility = View.VISIBLE
            updateUpgrades(currentRole)
        }
        updateButtonImages()
    }

    private fun setButtonOwnership(button: ImageButton, isOwned: Boolean, available: Boolean) {
        if (isOwned) {
            button.background.setTint(resources.getColor(R.color.teal_700))
        } else {
            button.background.setTint(resources.getColor(R.color.white))
        }

        if (available) {
            button.isEnabled = true
            button.alpha = 1.0f
        } else {
            button.isEnabled = false
            button.alpha = 0.3f
        }

        val buttonText =
            if (isOwned) "OWNED" else "${expArray[currSection][buttonList.indexOf(button)]} EXP"
        val expTextView =
            if (isSpecialSection) expTextArray[buttonList.indexOf(button)] else warriorExpTextArray[buttonList.indexOf(
                button
            )]
        expTextView.text = buttonText
    }

    private fun updateButtonImages() {
        val imageResources = when (currSection) {
            0 -> arrayOf(
                R.drawable.h1,
                R.drawable.h2,
                R.drawable.h3,
                R.drawable.h4a,
                R.drawable.h4b
            )

            1 -> arrayOf(
                R.drawable.r1,
                R.drawable.r2,
                R.drawable.r3,
                R.drawable.r4a,
                R.drawable.r4b
            )

            2 -> arrayOf(
                R.drawable.m1,
                R.drawable.m2,
                R.drawable.m3,
                R.drawable.m4a,
                R.drawable.m4b
            )

            3 -> arrayOf(
                R.drawable.k1,
                R.drawable.k2,
                R.drawable.k3,
                R.drawable.k4a,
                R.drawable.k4b
            )

            4 -> arrayOf(
                R.drawable.w1,
                R.drawable.w2,
                R.drawable.w3,
                R.drawable.w4,
                R.drawable.w5 // Add a placeholder image resource here
            )

            else -> arrayOf()
        }

        if (currSection < 4) {
            for (i in buttonList.indices) {
                buttonList[i].setImageResource(imageResources[i])
            }
        } else if (currSection == 4) {
            for (i in warriorButtonList.indices) {
                if (i < imageResources.size) {
                    warriorButtonList[i].setImageResource(imageResources[i])
                } else {
                    warriorButtonList[i].setImageResource(R.mipmap.ic_tiger)
                }
            }
        }
    }
}