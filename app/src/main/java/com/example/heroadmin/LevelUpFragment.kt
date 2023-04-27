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
import androidx.navigation.fragment.findNavController
import com.example.heroadmin.databinding.FragmentLevelUpBinding

class LevelUpFragment : Fragment() {
    private lateinit var binding: FragmentLevelUpBinding
    lateinit var player: Player
    private lateinit var v: View
    private lateinit var DBF: DatabaseFunctions
    lateinit var currPlayerId: String
    private var currSection = 0
    private lateinit var mainClassLevel: IntArray
    private lateinit var buttonList: Array<Array<ImageButton>>
    private lateinit var warriorButtonList: Array<ImageButton>
    private lateinit var warriorExpTextArray: Array<TextView>
    private lateinit var specialSection: LinearLayout
    private lateinit var warriorSection: LinearLayout

    private val expArray: Array<IntArray> = arrayOf(
        intArrayOf(50, 75, 100, 100), // Healer levels' experience costs
        intArrayOf(50, 75, 100, 100), // Rogue levels' experience costs
        intArrayOf(50, 75, 100, 100), // Mage levels' experience costs
        intArrayOf(50, 75, 100, 100), // Knight levels' experience costs
        intArrayOf(100, 100, 100, 100) // Warrior levels' experience costs
    )

    private lateinit var expTextArray: Array<TextView>
    private lateinit var args: LevelUpFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_level_up, container, false)
        v = binding.root
        args = LevelUpFragmentArgs.fromBundle(requireArguments())
        currPlayerId = args.passedPlayerId
        DBF = DatabaseFunctions(v.context)
        player = DBF.getPlayer(currPlayerId)
        player.updateExp()
        mainClassLevel = IntArray(5)

        binding.levelUpPlayerNameText.text = player.fullName
        updateExpText()

        specialSection = binding.specialClassList
        warriorSection = binding.warriorList

        buttonList = arrayOf(
            arrayOf(
                binding.upgrade1button,
                binding.upgrade2button,
                binding.upgrade3button,
                binding.upgrade4button,
                binding.upgrade5button
            )
        )

        warriorButtonList = arrayOf(
            binding.warrior1button,
            binding.warrior2button,
            binding.warrior3button,
            binding.warrior4button
        )

        expTextArray = arrayOf(
            binding.upgrade1exp, binding.upgrade2exp, binding.upgrade3exp, binding.upgrade4exp
        )

        warriorExpTextArray = arrayOf(
            binding.warrior1exp, binding.warrior2exp, binding.warrior3exp, binding.warrior4exp
        )

        with(binding) {
            levelUpHealerButton.setOnClickListener { changeSection(R.color.healerColor, 0, true) }
            levelUpRogueButton.setOnClickListener { changeSection(R.color.rogueColor, 1, true) }
            levelUpMageButton.setOnClickListener { changeSection(R.color.mageColor, 2, true) }
            levelUpKnightButton.setOnClickListener { changeSection(R.color.knightColor, 3, true) }
            levelUpWarriorButton.setOnClickListener { changeSection(R.color.warriorColor, 4, false) }}

            binding.levelUpBackButton.setOnClickListener {
                findNavController().navigate(LevelUpFragmentDirections.actionLevelUpFragmentToEventView())
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
                binding.warrior4button
            )

            for (button in buttons) {
                button.setOnClickListener { buttonClick(button) }
            }

        changeSection(R.color.healerColor, 0, true)

        return binding.root
    }

    private fun updateExpText() {
        player.updateExp()
        binding.levelUpExpRemText.text = player.remExp.toString()
        Log.i("test", "Updated exp text. Rem exp: " + player.remExp)
    }

    private fun buttonClick(button: ImageButton) {
        val index = findIndex(buttonList, button)
        if (index == -1) {
            Log.i("test", "Could not find upgrade button")
            return
        } else {
            // Use index to identify the main class and level
            val mainClass = index / 4
            val level = index % 4
            val isSpecialSection = mainClass != 4

            if (mainClassLevel[mainClass] >= level + 1) {
                // remove upgrade
                mainClassLevel[mainClass] = level
                updateUpgrades(currSection, isSpecialSection)
                updateExpText()
            } else {
                // if enough exp, add upgrade
                if (player.remExp >= expArray[mainClass][level]) {
                    mainClassLevel[mainClass] = level + 1
                    updateUpgrades(currSection, isSpecialSection)
                    updateExpText()
                }
            }
        }
    }

    private fun findIndex(arr: Array<Array<ImageButton>>, item: ImageButton): Int {
        for (i in arr.indices) {
            for (j in arr[i].indices) {
                if (arr[i][j] == item) {
                    return i * 4 + j
                }
            }
        }
        return -1
    }

    private fun updateUpgrades(currentRole: Int, isSpecialSection: Boolean) {
        val numOfLevels = 4
        val currentButtonList = if (isSpecialSection) buttonList[0] else warriorButtonList
        val currentExpTextArray = if (isSpecialSection) expTextArray else warriorExpTextArray

        for (level in 0 until numOfLevels) {
            val index = currentRole * numOfLevels + level
            val classLevel = player.getClassLevel(currentRole)

            if (index < currentExpTextArray.size) {
                if (classLevel >= level || player.isWarriorUpgradeUnlocked("warriorHealer") ||
                    player.isWarriorUpgradeUnlocked("warriorRogue") || player.isWarriorUpgradeUnlocked("warriorMage") ||
                    player.isWarriorUpgradeUnlocked("warriorKnight")) {
                    currentExpTextArray[index].text = "OWNED"
                } else {
                    currentExpTextArray[index].text = "${expArray[currentRole][level]} EXP"
                }
            }

            val buttonIndex = currentRole * (numOfLevels + 1) + level
            if (buttonIndex < currentButtonList.size) {
                if (classLevel >= level || player.isWarriorUpgradeUnlocked("warriorHealer") ||
                    player.isWarriorUpgradeUnlocked("warriorRogue") || player.isWarriorUpgradeUnlocked("warriorMage") ||
                    player.isWarriorUpgradeUnlocked("warriorKnight")) {
                    setButtonOwnership(currentButtonList[buttonIndex], true, true)
                } else {
                    setButtonOwnership(currentButtonList[buttonIndex], false, true)
                }
            }
        }
    }

    private fun changeSection(colorRes: Int, currentRole: Int, isSpecialSection: Boolean) {
        binding.subclassListBackground.background.setTint(resources.getColor(colorRes))
        updateUpgrades(currentRole, isSpecialSection)
        currSection = currentRole

        if (isSpecialSection) {
            specialSection.visibility = View.VISIBLE
            warriorSection.visibility = View.GONE
        } else {
            specialSection.visibility = View.GONE
            warriorSection.visibility = View.VISIBLE // set the visibility of the warrior section to VISIBLE
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
            button.background.alpha = 255
            button.imageAlpha = 255
        } else {
            button.background.alpha = 75
            button.imageAlpha = 75
        }
    }

    private fun updateButtonImages() {
        val imageResources = when (currSection) {
            0 -> arrayOf(
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger
            ) // Add healer ultimate images
            1 -> arrayOf(
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger
            ) // Add rogue ultimate images
            2 -> arrayOf(
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger
            ) // Add mage ultimate images
            3 -> arrayOf(
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger
            ) // Add knight ultimate images
            4 -> arrayOf(
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger,
                R.mipmap.ic_tiger
            ) // Add warrior ultimate images
            else -> arrayOf()
        }

        for (i in buttonList.indices) {
            for (j in buttonList[i].indices) {
                buttonList[i][j].setImageResource(imageResources[i * 4 + j])
            }
        }
    }
}