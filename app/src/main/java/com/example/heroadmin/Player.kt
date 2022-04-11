package com.example.heroadmin

class Player(
    var playerId : String,
    var firstName : String,
    var lastName : String,
    var age : Int,
    var totalExp : Int = getPlayerEXP(playerId) ,
    var healerLevels : MutableList<Int> = mutableListOf(0,0,0),
    var rogueLevels : MutableList<Int> = mutableListOf(0,0,0),
    var mageLevels : MutableList<Int> = mutableListOf(0,0),
    var knightLevels : MutableList<Int> = mutableListOf(0,0,0),
    var warriorLevels : MutableList<Int> = mutableListOf(0,0,0),
    var guardians : List<String>, // List of phoneNumbers
) {
    var fullName = "$firstName $lastName"
    // var remExp = totalExp - getUsedExp(playerId)
    var remExp = 165

    //var subclassArray = mutableListOf(healerLevels, rogueLevels, mageLevels, knightLevels)

    // placeholder
    var subclassArray = mutableListOf(mutableListOf(1,2,0), mutableListOf(3,4,1), mutableListOf(2,0,2), mutableListOf(4,1,3))
}