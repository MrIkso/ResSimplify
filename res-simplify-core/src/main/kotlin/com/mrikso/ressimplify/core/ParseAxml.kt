package com.mrikso.ressimplify.core

import com.google.common.collect.Lists
import com.google.devrel.gmscore.tools.apk.arsc.*
import java.io.FileInputStream
import java.io.InputStream

class ParseAxml(normalAttributeNames: MutableMap<String, String>) {

    private var fileChanged = false
    private var _normalAttributeNames: MutableMap<String, String> = normalAttributeNames

    fun parseAxml(filePath: String):ByteArray? {
        return parseAxml(FileInputStream(filePath))
    }

    fun parseAxml(filePath: InputStream): ByteArray? {
        val binaryResourceFile = BinaryResourceFile.fromInputStream(filePath)
        for (chunk in binaryResourceFile.chunks) {
            val xmlChunk = chunk as (XmlChunk)
            deobfuscateAttributes(xmlChunk.chunks)
        }
        if (fileChanged) {
            fileChanged = false
            return binaryResourceFile.toByteArray()
        }
        return null
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