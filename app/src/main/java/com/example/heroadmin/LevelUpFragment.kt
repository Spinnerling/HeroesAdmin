package com.example.heroadmin

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.*
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.style.BackgroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.heroadmin.databinding.FragmentLevelUpBinding

class LevelUpFragment : Fragment() {
    private lateinit var binding: FragmentLevelUpBinding
    lateinit var player: Player
    lateinit var currPlayerId : String
    private var currClassName : String = "Healer"
    private var healerExpArray : Array<IntArray> = arrayOf(intArrayOf(0, 50, 75, 100), intArrayOf(100, 50, 75, 100), intArrayOf(100, 50, 75, 100))
    private var mageExpArray : Array<IntArray> = arrayOf(intArrayOf(0, 50, 75, 100), intArrayOf(100, 50, 75, 100))
    private var rogueExpArray : Array<IntArray> = arrayOf(intArrayOf(0, 50, 75, 100), intArrayOf(100, 50, 75, 100), intArrayOf(100, 50, 75, 100))
    private var knightExpArray : Array<IntArray> = arrayOf(intArrayOf(0, 50, 75, 100), intArrayOf(100, 50, 75, 100), intArrayOf(100, 50, 75, 100))
    private lateinit var expArray : Array<IntArray>
    private lateinit var expTextArray : Array<Array<TextView>>
    private lateinit var args : LevelUpFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_level_up, container, false)

        args = LevelUpFragmentArgs.fromBundle(requireArguments())
        currPlayerId = args.passedPlayerId
        player = getPlayer(currPlayerId)

        binding.levelUpPlayerNameText.text = player.fullName
        binding.levelUpExpRemText.text = player.totalExp.toString() + " EXP"

        expTextArray = arrayOf(
            arrayOf(binding.subclass1Exp1, binding.subclass1Exp2, binding.subclass1Exp3, binding.subclass1Exp4),
            arrayOf(binding.subclass2Exp1, binding.subclass2Exp2, binding.subclass2Exp3, binding.subclass2Exp4),
            arrayOf(binding.subclass3Exp1, binding.subclass3Exp2, binding.subclass3Exp3, binding.subclass3Exp4))


        healerSection()

        binding.levelUpHealerButton.setOnClickListener{
            healerSection()
        }
        binding.levelUpMageButton.setOnClickListener{
            mageSection()
        }
        binding.levelUpRogueButton.setOnClickListener{
            rogueSection()
        }
        binding.levelUpKnightButton.setOnClickListener{
            knightSection()
        }

        binding.levelUpBackButton.setOnClickListener{
            findNavController().navigate(LevelUpFragmentDirections.actionLevelUpFragmentToEventView())
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setUpgradeCosts() {
        for ((i, array) in expArray.withIndex()) {
            for ((j, item) in array.withIndex()) {
                if (expArray[i][j] != 0){
                    expTextArray[i][j].text = expArray[i][j].toString() + " EXP"
                }
                else {
                    expTextArray[i][j].text = "OWNED"
                }
            }
        }
    }

    fun updateSubclassList(className : String) {

        binding.subclassListBackground.background.setTint(211211211)
        currClassName = className
    }

    fun healerSection(){
        expArray = healerExpArray
        binding.subclassTitle1.text = "Templar"
        binding.subclassTitle2.text = "Fältskär"
        binding.subclassTitle3.text = "Väktare"
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.healerColor))
        binding.subclassList3.visibility = View.VISIBLE
        setUpgradeCosts()
    }

    fun mageSection() {
        expArray = mageExpArray
        binding.subclassTitle1.text = "Mystiker"
        binding.subclassTitle2.text = "Gycklare"
        binding.subclassTitle3.text = ""
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.mageColor))
        binding.subclassList3.visibility = View.GONE
        setUpgradeCosts()
    }

    fun rogueSection() {
        expArray = rogueExpArray
        binding.subclassTitle1.text = "Tjuv"
        binding.subclassTitle2.text = "Ninja"
        binding.subclassTitle3.text = "Rövare"
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.rogueColor))
        binding.subclassList3.visibility = View.VISIBLE
        setUpgradeCosts()
    }

    fun knightSection() {
        expArray = knightExpArray
        binding.subclassTitle1.text = "Förkämpe"
        binding.subclassTitle2.text = "Paladin"
        binding.subclassTitle3.text = "Knekt"
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.knightColor))
        binding.subclassList3.visibility = View.VISIBLE
        setUpgradeCosts()
    }

}