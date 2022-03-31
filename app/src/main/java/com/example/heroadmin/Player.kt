package com.example.heroadmin

class Player(
    var playerId : String,
    var firstName : String,
    var lastName : String,
    var age : Int,
    var totalExp : Int = getPlayerEXP(playerId) ,
    var healerLevels : List<Int>,
    var mageLevels : List<Int>,
    var rogueLevels : List<Int>,
    var knightLevels : List<Int>,
    var warriorLevels : List<Int>,
    var guardians : List<String>, // List of phoneNumbers
) {
    var fullName = "$firstName $lastName"
}