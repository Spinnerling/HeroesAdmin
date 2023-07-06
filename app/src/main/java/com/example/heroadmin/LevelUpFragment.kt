package com.example.heroadmin

import android.app.AlertDialog
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.heroadmin.databinding.FragmentLevelUpBinding
import kotlinx.coroutines.launch

class LevelUpFragment : Fragment() {
    private lateinit var binding: FragmentLevelUpBinding
    private lateinit var player: Player
    private lateinit var ticket: Ticket
    private lateinit var v: View
    private lateinit var DBF: DatabaseFunctions
    private lateinit var currPlayerId: String
    private lateinit var currTicketId: String
    private var currSection = 0
    private lateinit var buttonList: Array<ImageButton>
    private lateinit var warriorButtonList: Array<ImageButton>
    private lateinit var warriorExpTextArray: Array<TextView>
    private lateinit var specialSection: LinearLayout
    private lateinit var warriorSection: LinearLayout
    private var canHaveBoth: Boolean = false

    private val upgradeSpecialCost: Array<Int> = arrayOf(
        0, 50, 75, 100, 100 // Special classes levels' experience costs
    )
    private val upgradeWarriorCost: Array<Int> = arrayOf(
        0, 100, 100, 100, 100 // Warrior experience costs
    )

    private lateinit var specialExpTextArray: Array<TextView>
    private lateinit var args: LevelUpFragmentArgs
    private var currEventId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_level_up, container, false)
        v = binding.root
        DBF = DatabaseFunctions(v.context)
        DBF.setLevelUpView(this)
        args = LevelUpFragmentArgs.fromBundle(requireArguments())
        currPlayerId = args.passedPlayerId
        currTicketId = args.passedTicketId
        currEventId = args.passedEventId

        specialSection = binding.specialClassList
        warriorSection = binding.warriorList

        buttonList = arrayOf(
            binding.level1Button,
            binding.level2Button,
            binding.level3Button,
            binding.ultimateAButton,
            binding.ultimateBButton
        )

        for (button in buttonList.indices) {
            buttonList[button].setOnClickListener { buttonClick(button) }
        }

        specialExpTextArray = arrayOf(
            binding.level1Exp,
            binding.level2Exp,
            binding.level3Exp,
            binding.ultimateAExp,
            binding.ultimateBExp
        )

        warriorButtonList = arrayOf(
            binding.warrior1button,
            binding.warrior2button,
            binding.warrior3button,
            binding.warrior4button,
            binding.warrior5button
        )
        for (button in warriorButtonList.indices) {
            warriorButtonList[button].setOnClickListener { warriorButtonClick(button) }
        }

        warriorExpTextArray = arrayOf(
            binding.warrior1exp,
            binding.warrior2exp,
            binding.warrior3exp,
            binding.warrior4exp,
            binding.warrior5exp
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

        binding.levelUpApplyButton.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.award_exp, null)

            val builder = AlertDialog.Builder(context).setView(dialogView)

            val notification = builder.show()

            val nameTextHolder: TextView = dialogView.findViewById(R.id.ae_playerNameText)
            val expTextHolder: EditText = dialogView.findViewById(R.id.ae_expAmount)
            val cancelButton = dialogView.findViewById<Button>(R.id.ae_cancelButton)
            val acceptButton = dialogView.findViewById<Button>(R.id.ae_acceptButton)

            nameTextHolder.text = player.fullName
            expTextHolder.setText(ticket.expPersonal.toString())

            cancelButton.setOnClickListener {
                notification.dismiss()
            }

            acceptButton.setOnClickListener {
                notification.dismiss()
                var amount = expTextHolder.text.toString()
                if (amount == "") amount = "0"
                ticket.expPersonal = amount.toInt()
                DBF.updateData(ticket)
                updatePlayer()
            }
        }

        updatePlayer()

        return binding.root
    }

    private fun updatePlayer() {
        lifecycleScope.launch {
            try {
                Log.i("LevelUp", "Passed Ticket Id: ${args.passedTicketId}, currTicketId: $currTicketId")
                ticket = LocalDatabaseSingleton.ticketDatabase.getById(currTicketId)!!
                if (ticket == null) {
                    Log.i("LevelUp", "Ticket is null")
                }
                else {
                    Log.i("LevelUp", "Ticket exists")
                    Log.i("LevelUp", "Ticket Exp: ${ticket.expPersonal}")
                }
                player = DBF.getPlayer(currPlayerId)!!
                player.updateUsedExp()
                binding.levelUpPlayerNameText.text = player.fullName
                updateExpText()
                changeSection(R.color.healerColor, 0)
            } catch (e: Exception) {
                // handle the exception
            }
        }
    }

    private fun updatePlayerLocal() {
        Log.i("player", "updating player locally: $currPlayerId")
        player = DBF.getPlayerLocal(currPlayerId)!!
        player.updateUsedExp()
        binding.levelUpPlayerNameText.text = player.fullName
        updateExpText()
        changeSection(R.color.healerColor, 0)
    }

    private fun updateExpText() {
        Log.i("player", "updating exp text")
        player.updateUsedExp()
        binding.levelUpExpRemText.text = player.remExp.toString()
        DBF.updateData(player)
        Log.i("test", "Updated exp text. Rem exp: " + player.remExp)
        Log.i(
            "Level",
            "Levels:\nHealer: ${player.healerLevel}, ${player.healerUltimateA}, ${player.healerUltimateB}\n"
        )
    }

    private fun buttonClick(buttonIndex: Int) {
        val classLevel = player.getClassLevel(currSection)
        val expCost = upgradeSpecialCost[buttonIndex]
        when (buttonIndex) {
            in 0..2 -> {
                if (classLevel == buttonIndex + 1 && buttonIndex != 0) {
                    player.setClassLevel(currSection, buttonIndex)
                    player.setUltimateA(currSection, false)
                    player.setUltimateB(currSection, false)
                } else if (classLevel > buttonIndex + 1) {
                    player.setClassLevel(currSection, buttonIndex + 1)
                    player.setUltimateA(currSection, false)
                    player.setUltimateB(currSection, false)
                } else if (player.remExp >= expCost) {
                    player.setClassLevel(currSection, buttonIndex + 1)
                }
            }

            3 -> {
                if (!player.getUltimateA(currSection) && player.remExp >= expCost) {
                    player.setClassLevel(currSection, 4)
                    player.setUltimateA(currSection, true)
                    if (!canHaveBoth) {
                        player.setUltimateB(currSection, false)
                    }
                } else if (player.getUltimateA(currSection)) {
                    player.setUltimateA(currSection, false)
                    if (!player.getUltimateB(currSection)) player.setClassLevel(currSection, 3)
                }
            }

            4 -> {
                if (!player.getUltimateB(currSection) && player.remExp >= expCost) {
                    player.setClassLevel(currSection, 4)
                    player.setUltimateB(currSection, true)
                    if (!canHaveBoth) {
                        player.setUltimateA(currSection, false)
                    }
                } else if (player.getUltimateB(currSection)) {
                    player.setUltimateB(currSection, false)
                    if (!player.getUltimateA(currSection)) player.setClassLevel(currSection, 3)
                }
            }
        }
        updateExpText()
        updateUpgrades()
    }

    private fun updateUpgrades() {
        buttonList.forEachIndexed { index, button ->
            setButtonOwnership(button, index)
        }
        updateButtonImages()
    }

    private fun setButtonOwnership(button: ImageButton, index: Int) {
        val isOwned: Boolean = when (index) {
            in 0..2 -> player.getClassLevel(currSection) > index
            3 -> player.getUltimateA(currSection)
            4 -> player.getUltimateB(currSection)
            else -> false
        }

        if (isOwned) {
            // Setting a border
            button.background =
                context?.let { ContextCompat.getDrawable(it, R.drawable.button_border) }

            // Clearing color filter to show the original image color
            button.clearColorFilter()
            button.isEnabled = true
            button.alpha = 1.0f

            // Set experience text to "UNLOCKED"
            specialExpTextArray[index].text = "UNLOCKED"
        } else if (player.remExp >= upgradeSpecialCost[index]) {
            // Removing the border
            button.background = null

            // Applying a gray color filter to indicate it's not owned yet
            context?.let { ContextCompat.getColor(it, android.R.color.darker_gray) }
                ?.let { button.setColorFilter(it, PorterDuff.Mode.MULTIPLY) }
            button.isEnabled = true
            button.alpha = 1.0f

            // Set experience text to the cost
            specialExpTextArray[index].text = upgradeSpecialCost[index].toString()
        } else {
//        button.isEnabled = false
            button.alpha = 0.3f

            // Set experience text to the cost
            specialExpTextArray[index].text = upgradeSpecialCost[index].toString()
        }
    }

    private fun changeSection(colorRes: Int, section: Int) {
        currSection = section
//        binding.subclassListBackground.background.setTint(resources.getColor(colorRes))

        if (section != 4) {
            specialSection.visibility = View.VISIBLE
            warriorSection.visibility = View.GONE
            updateUpgrades()
        } else {
            specialSection.visibility = View.GONE
            warriorSection.visibility = View.VISIBLE
            updateWarriorUpgrades()
        }
        updateButtonImages()
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
                R.drawable.ws,
                R.drawable.wh,
                R.drawable.wr,
                R.drawable.wm,
                R.drawable.wk
            )

            else -> arrayOf()
        }

        if (currSection < 4) {
            for (i in buttonList.indices) {
                buttonList[i].setImageResource(imageResources[i])
            }
        } else if (currSection == 4) {
            for (i in warriorButtonList.indices) {
                warriorButtonList[i].setImageResource(imageResources[i])
            }
        }
    }

    private fun warriorButtonClick(buttonIndex: Int) {
        val expCost = upgradeWarriorCost[buttonIndex]
        when (buttonIndex) {
            0 -> { // base button
                player.warriorHealer = false
                player.warriorRogue = false
                player.warriorMage = false
                player.warriorKnight = false
            }

            1 -> { // warriorHealer
                player.warriorHealer = !player.warriorHealer && player.remExp >= expCost
            }

            2 -> { // warriorRogue
                player.warriorRogue = !player.warriorRogue && player.remExp >= expCost
            }

            3 -> { // warriorMage
                player.warriorMage = !player.warriorMage && player.remExp >= expCost
            }

            4 -> { // warriorKnight
                player.warriorKnight = !player.warriorKnight && player.remExp >= expCost
            }
        }
        updateExpText()
        updateWarriorUpgrades()
    }

    private fun updateWarriorUpgrades() {
        warriorButtonList.forEachIndexed { index, button ->
            setWarriorButtonOwnership(button, index)
        }
        updateButtonImages()
    }

    private fun setWarriorButtonOwnership(button: ImageButton, index: Int) {
        val isOwned: Boolean = when (index) {
            0 -> true
            1 -> player.warriorHealer
            2 -> player.warriorRogue
            3 -> player.warriorMage
            4 -> player.warriorKnight
            else -> false
        }

        if (isOwned) {
            // Setting a border
            button.background =
                context?.let { ContextCompat.getDrawable(it, R.drawable.button_border) }

            // Clearing color filter to show the original image color
            button.clearColorFilter()
            button.isEnabled = true
            button.alpha = 1.0f

            // Set experience text to "UNLOCKED"
            warriorExpTextArray[index].text = "UNLOCKED"

        } else if (player.remExp >= upgradeWarriorCost[index]) {
            // Removing the border
            button.background = null

            // Applying a gray color filter to indicate it's not owned yet
            context?.let { ContextCompat.getColor(it, android.R.color.darker_gray) }
                ?.let { button.setColorFilter(it, PorterDuff.Mode.MULTIPLY) }
            button.isEnabled = true
            button.alpha = 1.0f

            // Set experience text to the cost
            warriorExpTextArray[index].text = upgradeWarriorCost[index].toString()

        } else {
            button.alpha = 0.3f
            // Set experience text to the cost
            warriorExpTextArray[index].text = upgradeWarriorCost[index].toString()
        }
    }

    fun lostConnection() {
        findNavController().navigate(
            LevelUpFragmentDirections.actionLevelUpFragmentToEventView(
                currEventId
            )
        )
    }

}