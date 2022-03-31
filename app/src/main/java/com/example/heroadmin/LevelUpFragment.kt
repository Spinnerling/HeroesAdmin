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
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.heroadmin.databinding.FragmentLevelUpBinding

class LevelUpFragment : Fragment() {
    private lateinit var binding: FragmentLevelUpBinding
    lateinit var player: Player
    private var currClassName : String = "Healer"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_level_up, container, false)

        player = getPlayer("player123")
        binding.levelUpPlayerNameText.text = player.fullName
        binding.levelUpExpRemText.text = player.totalExp.toString() + " EXP"
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

        // Inflate the layout for this fragment
        return binding.root
    }

    fun updateSubclassList(className : String) {

        binding.subclassListBackground.background.setTint(211211211)
        currClassName = className
    }

    fun healerSection(){
        binding.subclassTitle1.text = "Templar"
        binding.subclassTitle2.text = "Fältskär"
        binding.subclassTitle3.text = "Väktare"
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.healerColor))
        binding.subclassList3.visibility = View.VISIBLE
    }

    fun mageSection() {
        binding.subclassTitle1.text = "Mystiker"
        binding.subclassTitle2.text = "Gycklare"
        binding.subclassTitle3.text = ""
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.mageColor))
        binding.subclassList3.visibility = View.GONE
    }

    fun rogueSection() {
        binding.subclassTitle1.text = "Tjuv"
        binding.subclassTitle2.text = "Ninja"
        binding.subclassTitle3.text = "Rövare"
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.rogueColor))
        binding.subclassList3.visibility = View.VISIBLE
    }

    fun knightSection() {
        binding.subclassTitle1.text = "Förkämpe"
        binding.subclassTitle2.text = "Paladin"
        binding.subclassTitle3.text = "Knekt"
        binding.subclassListBackground.background.setTint(resources.getColor(R.color.knightColor))
        binding.subclassList3.visibility = View.VISIBLE
    }

}