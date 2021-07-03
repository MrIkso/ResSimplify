package com.mrikso.ressimplify.core

import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileInputStream


class DeobfuscatorResources {

    fun runDeobfuscator(
        apkPath: String,
        tempDir: String,
        resourcePaths: MutableMap<String, String>,
        normalAttributeNames: MutableMap<String, String>
    ) {
        println("Renaming resources...")
        var renamedCount = 0;
        var count = 0;
        val zipFile = ZipFile(apkPath)
        resourcePaths.forEach { file ->
            if (zipFile.getFileHeader(file.key) != null) {
                //  println("${file.key} -> ${file.value}")
                // zipFile.renameFile(zipFile.getFileHeader(file.key), file.value)
                val outFilePath = File("${tempDir}/${file.value}")
                if (file.value.endsWith(".xml")) {
                    zipFile.extractFile(file.key, outFilePath.parent, outFilePath.name)
                    zipFile.removeFile(file.key)
                    if (ParseAxml(normalAttributeNames).parseAxml(
                            FileInputStream(outFilePath.absolutePath),
                            outFilePath.absolutePath
                        )
                    ) {
                        count++
                    }

                } else {
                    zipFile.renameFile(file.key, file.value)
                }
                /* val outFilePath = File("${tempDir}/${file.value}")

                 zipFile.removeFile(file.key)*/
                renamedCount++
            }
        }

        println("Renamed resources: $renamedCount")
        //    println("Renaming attributes in xml files...")
        //  var count = 0;

        /*  resourcePaths.forEach { file ->

              val outFilePath = File("${tempDir}/${file.value}")

              if (file.value.endsWith(".xml")) {
                  if (ParseAxml(normalAttributeNames).parseAxml(
                          FileInputStream(outFilePath.absolutePath),
                          outFilePath.absolutePath
                      )
                  )
                      count++

              }
          }*/

        println("Renamed xml attributes: $count")

        println("Adding renamed resources back to apk file")
        ZipUtil.zipFile("${tempDir}/res", apkPath)

        println("Adding renamed resource.arsc back to apk file")
        ZipUtil.zipFile("${tempDir}/${Run.ARSC_FILE_NAME}", apkPath)
    }

}