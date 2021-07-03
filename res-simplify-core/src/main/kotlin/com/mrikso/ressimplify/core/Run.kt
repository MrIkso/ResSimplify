package com.mrikso.ressimplify.core

import com.android.apksig.ApkSigner
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceFile
import com.mrikso.utils.SignatureResources
import java.io.File
import java.nio.file.Files
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.zip.ZipFile
import kotlin.concurrent.thread


class Run {

    companion object {
        const val ARSC_FILE_NAME = "resources.arsc"
        private var whiteListMap: MutableList<String> = mutableListOf(
            "ad_app_id",
            "ad_banner_id",
            "ad_interstitial_id",
            "ad_interstitial_video_id",
            "ad_rewarded_id",
            "ad_unified_native_id",
            "ad_openapp_id",
            "ad_mediation_id",
            "banner_ad_unit_id",
            "map_api_key",
            "map_direction_api_key",
            "places_api_key",
            "google_app_id",
            "gcm_defaultSenderId",
            "default_web_client_id",
            "ga_trackingId",
            "firebase_database_url",
            "google_api_key",
            "google_crash_reporting_api_key",
            "default_web_client_id",
            "project_id",
            "com.crashlytics.useFirebaseAppId",
            "com.crashlytics.useFirebaseAppId",
            "com.crashlytics.CollectDeviceIdentifiers",
            "com.crashlytics.CollectDeviceIdentifiers",
            "com.crashlytics.CollectUserIdentifiers",
            "com.crashlytics.CollectUserIdentifiers",
            "com.crashlytics.ApiEndpoint",
            "io.fabric.android.build_id",
            "com.crashlytics.android.build_id",
            "com.crashlytics.RequireBuildId",
            "com.crashlytics.RequireBuildId",
            "com.crashlytics.CollectCustomLogs",
            "com.crashlytics.CollectCustomLogs",
            "com.crashlytics.Trace",
            "com.crashlytics.Trace",
            "com.crashlytics.CollectCustomKeys",
            "facebook_app_id"
        )
    }

    fun run(inputApk: String?, outApk: String?) {
        val whiteListConfig = File("whitelist.txt")
        if (whiteListConfig.exists()) {
            println("Reading config from: ${whiteListConfig.absolutePath}")
            val inputStream: InputStream = whiteListConfig.inputStream()
            inputStream.bufferedReader().forEachLine { line: String? ->
                if (!line!!.startsWith("#")) {
                    whiteListMap.add(line)
                }
            }
        }
        val apkName = File(inputApk!!).name
        println("Starting deobfuscate resources in: $apkName")
        val tempDir = getTempDir()
        //  println(tempDir)
        val tempApk = File("${tempDir}/$apkName")
        File(inputApk).copyTo(tempApk, true)
        val resources: BinaryResourceFile = ZipFile(tempApk).use { zip ->
            zip.getInputStream(zip.getEntry(ARSC_FILE_NAME)).use { BinaryResourceFile.fromInputStream(it) }
        }

        ParseArsc().parse(tempApk.absolutePath, tempDir, resources, whiteListMap)
        signApk(tempApk, File(outApk!!))
        println("Done!")
        thread(start = true) {
            File(tempDir).deleteRecursively()
        }
    }

    @Throws(java.lang.Exception::class)
    private fun signApk(inputApk: File, outFile: File) {
        println("Signing APK: ${inputApk.path}")

        val signerConfigs: List<ApkSigner.SignerConfig> = listOf(getDefaultSignerConfigFromResources("/keys/testkey"))

        val apkSignerBuilder = ApkSigner.Builder(signerConfigs)
            .setInputApk(inputApk)
            .setOutputApk(outFile)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)

        val apkSigner = apkSignerBuilder.build()
        apkSigner.sign()
        inputApk.delete()
        println("Signed! $outFile")
    }

    @Throws(Exception::class)
    fun getDefaultSignerConfigFromResources(keyNameInResources: String): ApkSigner.SignerConfig {
        val privateKey: PrivateKey = SignatureResources.toPrivateKey("$keyNameInResources.pk8", "RSA")
        val certs: List<X509Certificate> = SignatureResources.toCertificateChain("$keyNameInResources.x509.pem")
        return ApkSigner.SignerConfig.Builder("CERT", privateKey, certs).build()
    }

    private fun getTempDir(): String {
        return Files.createTempDirectory("ResSimplify").toString()
    }

}