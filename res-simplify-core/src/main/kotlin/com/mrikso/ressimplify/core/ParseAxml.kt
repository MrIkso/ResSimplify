package com.mrikso.ressimplify.core

import com.google.common.collect.Lists
import com.google.common.io.Files
import com.google.devrel.gmscore.tools.apk.arsc.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class ParseAxml(normalAttributeNames: MutableMap<String, String>) {

    private var xmlFilePath: String? = null
    private var fileChanged = false
    private var _normalAttributeNames: MutableMap<String, String> = normalAttributeNames


    fun parseAxml(inputStream: InputStream, outXmlFilePath: String): Boolean {
        xmlFilePath = outXmlFilePath
        return parseAxml(inputStream)
    }

    fun parseAxml(filePath: String, outXmlFilePath: String) {
        xmlFilePath = outXmlFilePath
        parseAxml(FileInputStream(filePath))
    }

    private fun parseAxml(filePath: InputStream): Boolean {
        val binaryResourceFile = BinaryResourceFile.fromInputStream(filePath)
        for (chunk in binaryResourceFile.chunks) {
            val xmlChunk = chunk as (XmlChunk)
            deobfuscateAttributes(xmlChunk.chunks)
        }
        if (fileChanged) {
            Files.write(binaryResourceFile.toByteArray(), File(xmlFilePath!!))
            fileChanged = false
            return true
        }
        return false
    }

    private fun deobfuscateAttributes(chunks: Map<Int, Chunk>) {
        // sort the chunks by their offset in the file in order to traverse them in the right order
        val contentChunks: List<Chunk> = sortByOffset(chunks)
        var stringPool: StringPoolChunk? = null
        var xmlResMap: XmlResourceMapChunk? = null
        for (chunk in contentChunks) {
            when (chunk) {
                is StringPoolChunk -> {
                    stringPool = chunk
                }
                is XmlResourceMapChunk -> {
                    xmlResMap = chunk
                }
                is XmlNamespaceStartChunk -> {
                    // println("${chunk.uri}, ${chunk.prefix}")
                }
                is XmlNamespaceEndChunk -> {

                }
                is XmlStartElementChunk -> {
                    // println(chunk.name)
                    for (xmlAttribute in chunk.attributes) {
                        val nameIndex = xmlAttribute.nameIndex()
                        val attributeId: BinaryResourceIdentifier? = xmlResMap?.getResourceId(nameIndex)
                        val a = attributeId?.toString()
                        //  println("${stringPool?.getString(nameIndex)}, $attributeId")
                        if (_normalAttributeNames.containsKey(a)) {
                            fileChanged = true
                            stringPool?.setString(nameIndex, _normalAttributeNames[a])
                        }

                    }
                }
                is XmlEndElementChunk -> {
                    //  println(chunk.name)

                }
            }
        }
    }

    private fun sortByOffset(contentChunks: Map<Int, Chunk>): MutableList<Chunk> {
        val offsets: MutableList<Int> = Lists.newArrayList(contentChunks.keys)
        offsets.sort()
        val chunks: MutableList<Chunk> = ArrayList(offsets.size)
        for (offset in offsets) {
            contentChunks[offset]?.let { chunks.add(it) }
        }
        return chunks
    }
}