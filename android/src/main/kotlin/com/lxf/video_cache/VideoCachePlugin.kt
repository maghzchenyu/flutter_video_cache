package com.lxf.video_cache

import LXFVideoCacheHostApi
import android.util.Log
import com.danikula.videocache.CacheListener
import com.danikula.videocache.HttpProxyCacheServer

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.log

/** VideoCachePlugin */
class VideoCachePlugin : FlutterPlugin, MethodCallHandler {

    private lateinit var videoCacheHostApiImplementation: LXFVideoCacheHostApiImplementation

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        videoCacheHostApiImplementation = LXFVideoCacheHostApiImplementation(flutterPluginBinding)

        LXFVideoCacheHostApi.setUp(
                flutterPluginBinding.binaryMessenger,
                videoCacheHostApiImplementation,
        )


    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        videoCacheHostApiImplementation.shutdown()
    }

    override fun onMethodCall(call: MethodCall, result: Result) {}
}

class LXFVideoCacheHostApiImplementation(
        private val flutterPluginBinding: FlutterPlugin.FlutterPluginBinding
) : LXFVideoCacheHostApi, CacheListener {

    private val VIDEO_CACHE_TAG = "video.cache.tag"
    private val cacheServer by lazy { HttpProxyCacheServer.Builder(flutterPluginBinding.applicationContext).build() }

    private var cacheMethondChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "video.cache.channel")
    override fun convertToCacheProxyUrl(url: String): String {
        startCacheListener(url)

        var convertUrl = cacheServer.getProxyUrl(url)
        Log.d(VIDEO_CACHE_TAG,"cache video origin url = $convertUrl")
        return convertUrl
    }

    override fun getTranscodeUrl(openurl: String, headers: Map<String, String>): String {
        val url = URL(openurl)
        val connection = url.openConnection() as HttpURLConnection
       connection.instanceFollowRedirects = false
        headers.forEach {
            connection.setRequestProperty(it.key, it.value)
        }
        connection.requestMethod = "GET"

        // 发起请求并获取响应
        val responseCode = connection.responseCode
        println("Response Code: $responseCode")
        var redirectUrl = ""
        // 处理响应码
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            redirectUrl = connection.getHeaderField("Location")
            println("redirect origin openurl = $openurl redirectUrl = $redirectUrl")
        } else if (responseCode == HttpURLConnection.HTTP_OK) {
            // 处理成功响应
        }
        connection.disconnect()
        return redirectUrl
    }

    fun shutdown() {
        cacheServer.shutdown()
    }

    private fun startCacheListener(url: String) {
        cacheServer.registerCacheListener(this, url)
    }

    override fun onCacheAvailable(cacheFile: File?, url: String?, percentsAvailable: Int) {
        val result = mapOf(
            "path" to cacheFile?.path,
            "url" to url,
            "percentsAvailable" to percentsAvailable,
        )
        Log.d(VIDEO_CACHE_TAG,"url = $url percentsAvailable = $percentsAvailable")
        cacheMethondChannel.invokeMethod("onCacheAvailable", result)
    }
}


