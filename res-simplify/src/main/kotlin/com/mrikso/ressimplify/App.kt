package com.mrikso.ressimplify

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.mrikso.ressimplify.core.Run
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureTimeMillis

class DeobfuscateCommand : CliktCommand(name = "res-simplify", help = "Deobfcuscation of resources in the apk file.") {
    private val inputFile by option("--in", "-input", help = "Input file to deobfuscation file")
    private val outputFile by option("--out", "-output", help = "Output file to save")

    override fun run() {
        if (!inputFile.isNullOrEmpty() && !outputFile.isNullOrEmpty()) {
            val elapsedTime = measureTimeMillis { Run().run(inputFile, outputFile) }
            echo("Elapsed time: ${getTimeStamp(elapsedTime)}")
        } else {
            echo("Arguments empty or null")
        }
    }
}

fun getTimeStamp(timeinMillies: Long): String? {
    val formatter = SimpleDateFormat("HH:mm:ss") // modify format
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(timeinMillies))
}

fun main(args: Array<String>) {
    DeobfuscateCommand().main(args)
}

