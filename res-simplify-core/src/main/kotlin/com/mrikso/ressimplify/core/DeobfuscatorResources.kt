package com.mrikso.ressimplify.core

import bin.zip.ZipEntry
import bin.zip.ZipFile
import bin.zip.ZipOutputStream
import java.io.File
import java.io.InputStream


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
        val changedXmlList: MutableList<String> = mutableListOf()
        val zipFile = ZipFile(apkPath)
        val entryEnumeration = zipFile.entries
        val tempApk = File("${tempDir}/${File(apkPath).name}")

        val zos = ZipOutputStream(tempApk)
        while (entryEnumeration.hasMoreElements()) {
            val zipEntry: ZipEntry = entryEnumeration.nextElement()

            if (zipEntry.isDirectory || zipEntry.name == "resources.arsc" || zipEntry.name.startsWith("META-INF/")) continue
            val oldResName = zipEntry.name
            if (resourcePaths.containsKey(oldResName)) {

                val newResName: String? = resourcePaths[oldResName]
                // set new name on file
                zipEntry.name = newResName!!

                // println("${zipEntry.name} -> $newResName")
                if (newResName.endsWith(".xml")) {
                    val byteArray = ParseAxml(normalAttributeNames).parseAxml(
                        zipFile.getInputStream(zipEntry)
                    )
                    if (byteArray != null) {
                        println("$oldResName -> $newResName")
                        writeZipEntry(zos, zipEntry, byteArray)
                        changedXmlList.add(newResName)
                        count++
                    }
                }

                renamedCount++
            }

            if (!changedXmlList.contains(zipEntry.name)) {
                // copy stream without repacking zip
                copyZipEntry(zos, zipEntry, zipFile)
            }
        }

        println("Renamed resources: $renamedCount")
        println("Renamed xml attributes: $count")
        println("Adding renamed resource.arsc back to apk file")

        // replace resources.arsc
        zos.setMethod(ZipOutputStream.STORED)
        zos.putNextEntry("resources.arsc")
        zos.write(File("${tempDir}/${Run.ARSC_FILE_NAME}").readBytes())
        zos.closeEntry()
        zos.close()
        zipFile.close()

    }

    private fun copyZipEntry(zos: ZipOutputStream, zipEntry: ZipEntry, zipFile: ZipFile) {
        val rawInputStream: InputStream = zipFile.getInputStream(zipEntry)
        zos.putNextEntry(ZipEntry(zipEntry.name))
        var len: Int
        val b = ByteArray(2048)
        while (rawInputStream.read(b).also { len = it } > 0) {
            zos.write(b, 0, len)
        }
        zos.closeEntry()
    }

    private fun writeZipEntry(zipOutputStream: ZipOutputStream, entryName: String?, entryData: InputStream) {
        val entry = ZipEntry(entryName)
        val buf = ByteArray(1024)
        zipOutputStream.putNextEntry(entry)
        var len: Int
        while (entryData.read(buf).also { len = it } > 0) {
            zipOutputStream.write(buf, 0, len)
        }
        zipOutputStream.closeEntry()
    }

    private fun writeZipEntry(zipOutputStream: ZipOutputStream, entryName: ZipEntry?, entryData: ByteArray) {
        zipOutputStream.putNextEntry(entryName)
        zipOutputStream.write(entryData)
        zipOutputStream.closeEntry()
    }
}