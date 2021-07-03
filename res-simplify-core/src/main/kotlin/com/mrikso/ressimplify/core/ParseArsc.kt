package com.mrikso.ressimplify.core

import com.google.common.io.Files
import com.google.devrel.gmscore.tools.apk.arsc.*
import com.mrikso.ressimplify.core.Run.Companion.ARSC_FILE_NAME
import java.io.File
import java.util.zip.ZipFile

class ParseArsc {

    private val normalAttributeNames: MutableMap<String, String> = mutableMapOf()
    private val resourceFileNames: MutableMap<String, String> = mutableMapOf()
    private val resourceNames: MutableMap<Int, String> = mutableMapOf()
    private var packageName: String? = null
    private var _whiteListMap: MutableList<String> = mutableListOf()
    private var _apkFile: String? = null
    private var _tempDir: String? = null

    fun parse(apkFile: String, tempDir: String, resources: BinaryResourceFile, whiteListMap: MutableList<String>) {
        _apkFile = apkFile
        _tempDir = tempDir
        _whiteListMap = whiteListMap
        for (chunk in resources.chunks) {
            val resourceTableChunk = chunk as (ResourceTableChunk)
            println("Renaming resource table...")
            val stringPool = resourceTableChunk.stringPool

            // getting all packages
            for (packageChunk in resourceTableChunk.packages) {
                packageName = packageChunk.packageName

                // getting all types
                for (typeSpecChunk in packageChunk.typeSpecChunks) {

                    val resTypeId = typeSpecChunk.id

                    // getting all configs from types
                    for (typeChunk in packageChunk.getTypeChunks(resTypeId)) {
                        deobfuscateResTable(typeChunk, stringPool, packageChunk.id, resTypeId)
                    }
                }

            }

        }

        // write deobfuscated arsc file
        Files.write(resources.toByteArray(), File("${_tempDir!!}/${ARSC_FILE_NAME}"))
        // run deobfuscator res path and axml files
        DeobfuscatorResources().runDeobfuscator(_apkFile!!, _tempDir!!, resourceFileNames, normalAttributeNames)
    }

    private fun deobfuscateResTable(typeChunk: TypeChunk, stringPool: StringPoolChunk, packId: Int, resTypeId: Int) {
        val config = typeChunk.configuration.toString()

        // getting all ids on this package
        for (entry in typeChunk.entries) {
            val entryId = entry.key
            val resType = entry.value.typeName()
            val resId = String.format("0x%1$08x", getResId(packId, resTypeId, entryId))
            var resName = entry.value.key()
            val resPath: String? = getResPath(entry.value, stringPool)

            val keyIndex = entry.value.keyIndex()

            if (resourceNames.containsKey(keyIndex)) {
                resName = resourceNames[keyIndex]
            }

            val normalName: String = if (_whiteListMap.contains(resName)) {
                resName
            } else {
                getNormalName(resType, entryId)
            }

            if (resPath.isNullOrBlank()) {
                /*  println(
                      String.format(
                          "pkg=%s, id=%s, name=%s, type=%s, config=%s, normal_name=%s",
                          packageName, resId, resName, resType, config, normalName
                      )
                  )*/
                normalizeName(normalName, resName, entry.value)

                if (resType == "attr") {
                    normalAttributeNames[resId] = normalName
                }
            } else {
                var normalPath = getNormalPath(resPath, resType, config, entryId)

                if (resourceFileNames.containsKey(resPath)) {
                    normalPath = resourceFileNames[resPath]!!
                } else {
                    resourceFileNames[resPath] = normalPath
                }
                /* println(
                     String.format(
                         "pkg=%s, id=%s, name=%s, type=%s, config=%s, path=%s, normal_name=%s, normal_path=%s",
                         packageName, resId, resName, resType, config, resPath, normalName, normalPath
                     )
                 )*/

                normalizeName(normalName, resName, entry.value)

                stringPool.setString(entry.value.value()!!.data(), normalPath)
            }
        }
    }

    private fun getNormalName(resType: String, resTypeId: Int): String {
        return resType + String.format("%04x", resTypeId)
    }

    private fun normalizeName(normalName: String, resName: String, entry: TypeChunk.Entry) {
        if (resourceNames.containsKey(entry.keyIndex())) {
            entry.addKey(normalName)
        } else {
            resourceNames[entry.keyIndex()] = resName
            entry.updateKey(entry.keyIndex(), normalName)
        }
    }

    private fun getNormalNameOnFile(oldFilePath: String, resType: String, resTypeId: Int): String {
        val normalName = getNormalName(resType, resTypeId)
        val zip = ZipFile(_apkFile!!)
        val ext: String = when (resType) {
            "color",
            "navigation",
            "layout",
            "anim",
            "animator",
            "menu",
            "interpolator",
            "transition",
            "xml" -> "xml"
            else -> DetectExt().getExtension(zip.getInputStream(zip.getEntry(oldFilePath)), oldFilePath)

        }
        zip.close()
        if (ext.isEmpty()) {
            return normalName
        }
        return "$normalName.$ext"
    }


    private fun getNormalPath(oldFilePath: String, resType: String, resConfigType: String, resTypeId: Int): String {
        return when (resConfigType) {
            "default",
            "navigation" ->
                String.format("res/%s/%s", resType, getNormalNameOnFile(oldFilePath, resType, resTypeId))
            else -> String.format(
                "res/%s-%s/%s",
                resType,
                resConfigType,
                getNormalNameOnFile(oldFilePath, resType, resTypeId)
            )
        }

    }

    private fun getResPath(typeChunk: TypeChunk.Entry, stringPool: StringPoolChunk?): String? {
        when (typeChunk.typeName()) {
            "animator",
            "anim",
            "color",
            "drawable",
            "mipmap",
            "layout",
            "menu",
            "raw",
            "xml",
            "navigation",
            "interpolator",
            "transition",
            "font" -> {
                val value: BinaryResourceValue? = typeChunk.value()
                return formatValue(value!!, stringPool!!)
            }
        }

        return null
    }

    private fun formatValue(value: BinaryResourceValue, stringPool: StringPoolChunk): String? {
        return if (value.type() == BinaryResourceValue.Type.STRING) {
            stringPool.getString(value.data())
        } else return null
    }

    private fun getResId(packId: Int, resTypeId: Int, entryId: Int): Int {
        return packId shl 24 or (resTypeId and 0xFF shl 16) or (entryId and 0xFFFF)
    }
}