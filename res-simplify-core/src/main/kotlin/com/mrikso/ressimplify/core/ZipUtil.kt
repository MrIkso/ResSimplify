package com.mrikso.ressimplify.core

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.File

object ZipUtil {
    private val TAG = "ZipUtil"

    const val UNZIP_SUCCESS = 0
    const val UNZIP_FAILED = 1

    fun zipFile(oriFilePath: String, zipFilePath: String): Boolean {
        try {
            val oriFile = File(oriFilePath)
            val zipFile = ZipFile(zipFilePath)
            val zipParameters = ZipParameters()

            zipParameters.compressionMethod = CompressionMethod.DEFLATE
            zipParameters.compressionLevel = CompressionLevel.FASTEST

            if (oriFile.isDirectory) {
               /* oriFile.walk().forEach { file ->
                    if(file.isFile) {
                        zipParameters.rootFolderNameInZip = file.parent
                        if (file.extension == "png") {
                            zipParameters.compressionMethod = CompressionMethod.STORE
                        }
                        zipFile.addFile(file, zipParameters)
                    }
                }
*/
               zipFile.addFolder(oriFile, zipParameters)
            } else {
                if(oriFile.extension == "arsc"){
                    zipParameters.compressionMethod = CompressionMethod.STORE
                }
                zipFile.addFile(oriFile, zipParameters)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun unZipFile(zipFilePath: String, dstFilePath: String): Int {
        try {
            val zipFile = ZipFile(zipFilePath)
            zipFile.extractAll(dstFilePath)
        } catch (e: ZipException) {
            e.printStackTrace()
            return UNZIP_FAILED
        }

        return UNZIP_SUCCESS
    }


    fun renameFileInZip(zipFilePath: String, sourceFilePath: String, destinationPath: String) {
        try {
            val zipFile = ZipFile(zipFilePath)
            zipFile.renameFile(zipFile.getFileHeader(sourceFilePath), destinationPath)
            //  zipFile.removeFile(sourceFilePath)
        } catch (e: ZipException) {
            e.printStackTrace()
        }
    }

    fun renameFilesInZip(zipFilePath: String, zipFilePaths: MutableMap<String, String>) {
        val zipFile = ZipFile(zipFilePath)
        zipFile.renameFiles(zipFilePaths)
    }
}