package com.example.heroadmin

class PlayerIdGenerator(latestId: String? = null) {
    private var currentId = latestId ?: "AAA000"

    fun generateNewPlayerId(): String {
        val letters = currentId.substring(0, 3)
        val numbers = currentId.substring(3).toInt()

        val newNumbers = (numbers + 1) % 1000
        val newLetters = if (newNumbers == 0) incrementLetters(letters) else letters

        currentId = String.format("%s%03d", newLetters, newNumbers)
        return currentId
    }

    private fun incrementLetters(letters: String): String {
        val first = letters[0]
        val second = letters[1]
        val third = letters[2]

        val newThird = if (third == 'Z') 'A' else third + 1
        val newSecond = if (newThird == 'A' && second == 'Z') 'A' else if (newThird == 'A') second + 1 else second
        val newFirst = if (newThird == 'A' && newSecond == 'A' && first == 'Z') 'A' else if (newThird == 'A' && newSecond == 'A') first + 1 else first

        return "$newFirst$newSecond$newThird"
    }
}