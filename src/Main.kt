import java.io.File
import java.io.IOException

fun encrypt(text: String, shift: Int): String {
    return text.map { char ->
        when {
            char.isLetter() && char in 'а'..'я' -> {
                val base = 'а'
                val offset = (char - base + shift) % 32
                if (offset < 0) (base + offset + 32) else (base + offset)
            }
            char.isLetter() && char in 'А'..'Я' -> {
                val base = 'А'
                val offset = (char - base + shift) % 32
                if (offset < 0) (base + offset + 32) else (base + offset)
            }
            else -> char
        }
    }.joinToString("")
}

fun decrypt(text: String, shift: Int): String {
    return encrypt(text, -shift)
}

fun bruteForceDecrypt(encryptedText: String, exampleText: String? = null): Pair<Int, String>? {
    val exampleFrequency = exampleText?.let { calculateFrequency(it) }
    var bestShift = 0
    var bestMatch = Double.MAX_VALUE
    val decryptedTexts = mutableListOf<Pair<Int, String>>()

    for (shift in 0..31) {
        val decryptedText = decrypt(encryptedText, shift)
        decryptedTexts.add(shift to decryptedText)

        if (exampleFrequency != null) {
            val decryptedFrequency = calculateFrequency(decryptedText)
            val match = compareFrequencies(decryptedFrequency, exampleFrequency)
            if (match < bestMatch) {
                bestMatch = match
                bestShift = shift
            }
        }
    }

    if (exampleFrequency != null) {
        return bestShift to decrypt(encryptedText, bestShift)
    } else {
        println("Все возможные варианты расшифровки:")
        decryptedTexts.forEach { (shift, text) ->
            println("Shift $shift: $text")
        }

        println("Введите номер сдвига (0-31), который кажется правильным:")
        val selectedShift = readLine()?.toIntOrNull()
        return if (selectedShift != null && selectedShift in 0..31) {
            selectedShift to decrypt(encryptedText, selectedShift)
        } else {
            null
        }
    }
}

fun calculateFrequency(text: String): Map<Char, Double> {
    val frequencyMap = mutableMapOf<Char, Int>()
    val totalLetters = text.filter { it.isLetter() && it in 'а'..'я' || it in 'А'..'Я' }.length

    text.forEach { char ->
        if (char.isLetter() && char in 'а'..'я' || char in 'А'..'Я') {
            frequencyMap[char.toLowerCase()] = frequencyMap.getOrDefault(char.toLowerCase(), 0) + 1
        }
    }

    return frequencyMap.mapValues { (_, count) -> count.toDouble() / totalLetters }
}

fun compareFrequencies(freq1: Map<Char, Double>, freq2: Map<Char, Double>): Double {
    return freq1.keys.sumByDouble { char -> Math.abs(freq1.getOrDefault(char, 0.0) - freq2.getOrDefault(char, 0.0)) }
}

fun main() {
    try {
        println("Выберите режим работы:")
        println("1. Шифрование текста")
        println("2. Расшифровка текста с известным ключом")
        println("3. Расшифровка методом перебора (brute force)")

        when (readLine()?.toIntOrNull()) {
            1 -> {
                println("Введите путь к файлу с текстом для шифрования:")
                val inputFile = readLine() ?: ""
                println("Введите путь к файлу для сохранения зашифрованного текста:")
                val outputFile = readLine() ?: ""
                println("Введите ключ шифрования (сдвиг):")
                val shift = readLine()?.toIntOrNull() ?: 0

                try {
                    if (File(inputFile).exists()) {
                        val text = File(inputFile).readText()
                        val encryptedText = encrypt(text, shift)
                        File(outputFile).writeText(encryptedText)
                        println("Текст успешно зашифрован и сохранен в $outputFile")
                    } else {
                        println("Файл не найден.")
                    }
                } catch (e: IOException) {
                    println("Ошибка ввода-вывода: ${e.message}")
                }
            }
            2 -> {
                println("Введите путь к файлу с зашифрованным текстом:")
                val inputFile = readLine() ?: ""
                println("Введите путь к файлу для сохранения расшифрованного текста:")
                val outputFile = readLine() ?: ""
                println("Введите ключ шифрования (сдвиг):")
                val shift = readLine()?.toIntOrNull() ?: 0

                try {
                    if (File(inputFile).exists()) {
                        val text = File(inputFile).readText()
                        val decryptedText = decrypt(text, shift)
                        File(outputFile).writeText(decryptedText)
                        println("Текст успешно расшифрован и сохранен в $outputFile")
                    } else {
                        println("Файл не найден.")
                    }
                } catch (e: IOException) {
                    println("Ошибка ввода-вывода: ${e.message}")
                }
            }
            3 -> {
                println("Введите путь к файлу с зашифрованным текстом:")
                val inputFile = readLine() ?: ""
                println("Введите путь к файлу для сохранения расшифрованного текста:")
                val outputFile = readLine() ?: ""
                println("Введите путь к файлу с примером текста (опционально):")
                val exampleFile = readLine() ?: ""

                try {
                    if (File(inputFile).exists()) {
                        val encryptedText = File(inputFile).readText()
                        val exampleText = if (exampleFile.isNotEmpty() && File(exampleFile).exists()) {
                            File(exampleFile).readText()
                        } else {
                            null
                        }

                        val result = bruteForceDecrypt(encryptedText, exampleText)
                        if (result != null) {
                            val (bestShift, decryptedText) = result
                            File(outputFile).writeText(decryptedText)
                            println("Текст успешно расшифрован с ключом $bestShift и сохранен в $outputFile")
                            println("Расшифрованный текст: $decryptedText")
                        } else {
                            println("Не удалось выбрать правильный сдвиг.")
                        }
                    } else {
                        println("Файл не найден.")
                    }
                } catch (e: IOException) {
                    println("Ошибка ввода-вывода: ${e.message}")
                }
            }
            else -> println("Неверный выбор.")
        }
    } catch (e: Exception) {
        println("Произошла ошибка: ${e.message}")
    }
}