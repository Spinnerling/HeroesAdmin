package com.example.heroadmin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.heroadmin.databinding.FragmentLevelUpBinding

class LevelUpFragment : Fragment() {
    private lateinit var binding: FragmentLevelUpBinding
    lateinit var player: Player
    lateinit var currPlayerId: String
    private var currSection = 0
    private lateinit var subClassLevels : MutableList<MutableList<Int>>
    private lateinit var upgBtn1_1 : ImageButton
    private lateinit var upgBtn1_2 : ImageButton
    private lateinit var upgBtn1_3 : ImageButton
    private lateinit var upgBtn1_4 : ImageButton
    private lateinit var upgBtn2_1 : ImageButton
    private lateinit var upgBtn2_2 : ImageButton
    private lateinit var upgBtn2_3 : ImageButton
    private lateinit var upgBtn2_4 : ImageButton
    private lateinit var upgBtn3_1 : ImageButton
    private lateinit var upgBtn3_2 : ImageButton
    private lateinit var upgBtn3_3 : ImageButton
    private lateinit var upgBtn3_4 : ImageButton
    private lateinit var buttonList : Array<Array<ImageButton>>

    private lateinit var expArray: Array<Array<Int>>
    private lateinit var expTextArray: Array<Array<TextView>>
    private lateinit var args: LevelUpFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_level_up, container, false)

        args = LevelUpFragmentArgs.fromBundle(requireArguments())
        currPlayerId = args.passedPlayerId
        player = getPlayer(currPlayerId)
        player.getExpCosts()
        player.updateExp()
        subClassLevels = player.classLevelsArray

        binding.levelUpPlayerNameText.text = player.fullName
        updateExpText()

        upgBtn1_1 = binding.subclass1button1
        upgBtn1_2 = binding.subclass1button2
        upgBtn1_3 = binding.subclass1button3
        upgBtn1_4 = binding.subclass1button4
        upgBtn2_1 = binding.subclass2button1
        upgBtn2_2 = binding.subclass2button2
        upgBtn2_3 = binding.subclass2button3
        upgBtn2_4 = binding.subclass2button4
        upgBtn3_1 = binding.subclass3button1
        upgBtn3_2 = binding.subclass3button2
        upgBtn3_3 = binding.subclass3button3
        upgBtn3_4 = binding.subclass3button4

        expTextArray = arrayOf(
            arrayOf(
                binding.subclass1Exp1,
                binding.subclass1Exp2,
                binding.subclass1Exp3,
                binding.subclass1Exp4
            ),
            arrayOf(
                binding.subclass2Exp1,
                binding.subclass2Exp2,
                binding.subclass2Exp3,
                binding.subclass2Exp4
            ),
            arrayOf(
                binding.subclass3Exp1,
                binding.subclass3Exp2,
                binding.subclass3Exp3,
                binding.subclass3Exp4
            )
        )

        buttonList = arrayOf(
            arrayOf(
                binding.subclass1button1,
                binding.subclass1button2,
                binding.subclass1button3,
                binding.subclass1button4
            ),
            arrayOf(
                binding.subclass2button1,
                binding.subclass2button2,
                binding.subclass2button3,
                binding.subclass2button4
            ),
            arrayOf(
                binding.subclass3button1,
                binding.subclass3button2,
                binding.subclass3button3,
                binding.subclass3button4
            ),
        )

        healerSection()

        // TODO: Set an index. That list is the current one.

        binding.levelUpHealerButton.setOnClickListener {
            healerSection()
        }
        binding.levelUpRogueButton.setOnClickListener {
            rogueSection()
            currSection = 1
        }
        binding.levelUpMageButton.setOnClickListener {
            mageSection()
            currSection = 2
        }
        binding.levelUpKnightButton.setOnClickListener {
            knightSection()
            currSection = 3
        }

        binding.levelUpBackButton.setOnClickListener {
            findNavController().navigate(LevelUpFragmentDirections.actionLevelUpFragmentToEventView())
        }

        upgBtn1_1.setOnClickListener { buttonClick(upgBtn1_1) }
        upgBtn1_2.setOnClickListener { buttonClick(upgBtn1_2) }
        upgBtn1_3.setOnClickListener { buttonClick(upgBtn1_3) }
        upgBtn1_4.setOnClickListener { buttonClick(upgBtn1_4) }
        upgBtn2_1.setOnClickListener { buttonClick(upgBtn2_1) }
        upgBtn2_2.setOnClickListener { buttonClick(upgBtn2_2) }
        upgBtn2_3.setOnClickListener { buttonClick(upgBtn2_3) }
        upgBtn2_4.setOnClickListener { buttonClick(upgBtn2_4) }
        upgBtn3_1.setOnClickListener { buttonClick(upgBtn3_1) }
        upgBtn3_2.setOnClickListener { buttonClick(upgBtn3_2) }
        upgBtn3_3.setOnClickListener { buttonClick(upgBtn3_3) }
        upgBtn3_4.setOnClickListener { buttonClick(upgBtn3_4) }


        // Inflate the layout for this fragment
        return binding.root
    }

    private fun updateExpText() {
        player.updateExp()
        binding.levelUpExpRemText.text = player.remExp.toString()
        Log.i("test", "Updated exp text. Rem exp: " + player.remExp)
    }

    private fun buttonClick(button : ImageButton){
        val indexArray = findIndex(buttonList, button)
        if (indexArray[0] == -1){
            Log.i("test", "Could not find upgrade button")
            return
        }
        else {
            val i = indexArray[0]
            val j = indexArray[1]

            // If your level in subclass i (ex 1) is larger than the button index j you clicked (0)
            if (subClassLevels[currSection][i] >= j + 1){
                // remove upgrade
                player.setSubclassLevel(currSection, i, j)
                updateUpgrades()
                updateExpText()
            }
            else {
                // if enough exp, add upgrade
                if (player.remExp >= expArray[i][j]) {
                    subClassLevels[currSection][i] = j + 1
                    player.setSubclassLevel(currSection, i, subClassLevels[currSection][i])
                    updateUpgrades()
                    updateExpText()
                }
            }

        }
    }

    private fun findIndex(arr: Array<Array<ImageButton>>, item: ImageButton): Array<Int> {
        for (i in arr.indices)
        {
            for (j in arr[i].indices){
                if (arr[i][j] == item) {
                    return arrayOf(i, j)
                }
            }

        }
        return arrayOf(-1)
    }

    private fun updateUpgrades() {
        for ((i, array) in expArray.withIndex()) {
            for ((j, item) in array.withIndex()) {

                var canBeBought = false
                if (subClassLevels[currSection][i] >= j){
                    canBeBought = true
                }

                if (subClassLevels[currSection][i]-1 >= j){
                    expTextArray[i][j].text = "OWNED"
                    setButtonOwnership(buttonList[i][j], true, true)
                }
                else {
                    expTextArray[i][j].text = item.toString() + " EXP"
                    setButtonOwnership(buttonList[i][j], false, canBeBought)
                }
            }
        }
    }


    private fun healerSection() {
        expArray = player.healerExpArray
        binding.subclassTitle1.text = "Templar"
        binding.subclassTitle2.text = "Fältskär"
        binding.subclassTitle3.text = "Väktare"
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.healerColor))
        binding.subclassList3.visibility = View.VISIBLE
        currSection = 0
        updateUpgrades()
    }

    private fun rogueSection() {
        expArray = player.rogueExpArray
        binding.subclassTitle1.text = "Tjuv"
        binding.subclassTitle2.text = "Ninja"
        binding.subclassTitle3.text = "Rövare"
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.rogueColor))
        binding.subclassList3.visibility = View.VISIBLE
        currSection = 1
        updateUpgrades()
    }

    private fun mageSection() {
        expArray = player.mageExpArray
        binding.subclassTitle1.text = "Mystiker"
        binding.subclassTitle2.text = "Gycklare"
        binding.subclassTitle3.text = ""
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.mageColor))
        binding.subclassList3.visibility = View.GONE
        currSection = 2
        updateUpgrades()
    }

    private fun knightSection() {
        expArray = player.knightExpArray
        binding.subclassTitle1.text = "Förkämpe"
        binding.subclassTitle2.text = "Paladin"
        binding.subclassTitle3.text = "Knekt"
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.knightColor))
        binding.subclassList3.visibility = View.VISIBLE
        currSection = 3
        updateUpgrades()
    }

    fun setButtonOwnership(button : ImageButton, isOwned : Boolean, available : Boolean) {
        if (isOwned){
            button.background.setTint(resources.getColor(R.color.teal_700))
        }
        else {
            button.background.setTint(resources.getColor(R.color.white))
        }

        if (available){
            button.background.alpha = 255
            button.imageAlpha = 255
        }
        else {
            button.background.alpha = 75
            button.imageAlpha = 75
        }
    }

}