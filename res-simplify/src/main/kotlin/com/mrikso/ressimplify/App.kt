package com.mrikso.ressimplify

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.mrikso.ressimplify.core.Run

class DeobfuscateCommand : CliktCommand(name = "res-simplify", help = "Deobfcuscation of resources in the apk file.") {
    private val inputFile by option("--in", "-input", help = "Input file to deobfuscation file")
    private val outputFile by option("--out", "-output", help = "Output file to save")

    override fun run() {
        if (!inputFile.isNullOrEmpty() && !outputFile.isNullOrEmpty()) {
            Run().run(inputFile, outputFile)
        }else{
            echo("Arguments empty or null")
        }
    }
}

fun main(args: Array<String>) {
    DeobfuscateCommand().main(args)
}

