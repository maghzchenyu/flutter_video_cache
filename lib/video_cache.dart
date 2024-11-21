import 'package:video_cache/plugin/pigeon.g.dart';
import 'dart:async';
import 'dart:io';
import 'package:flutter/services.dart';
import 'dart:convert';

abstract class VideoCacheListener {
  void onCacheAvailable(String url,  String filePath, int percentsAvailable);
}

class VideoCache {

  factory VideoCache() => _instance;

  late VideoCacheListener listener;

  static final VideoCache _instance = VideoCache._internal();

  final LXFVideoCacheHostApi _hostApi = LXFVideoCacheHostApi();

  final _eventChannel = const MethodChannel("video.cache.channel");

  final _cacheFinishTag = 100;

  VideoCache._internal() {
    _eventChannel.setMethodCallHandler(_handler);
  }

  /// 原生层通信监听回调
  Future<dynamic> _handler(MethodCall call) async {
    if (call.method == 'onCacheAvailable'){
      String jsonString = call.arguments as String;
      Map<String, dynamic> map = jsonDecode(jsonString) as Map<String, dynamic>;
      int percentsAvailable = map['percentsAvailable'];
      if (percentsAvailable == _cacheFinishTag) {
        String path = map['path'];
        String url = map['url'];
        path = path.replaceAll("file://", "");
        if (listener != null) {
          listener?.onCacheAvailable(url, path, percentsAvailable);
        }
      }
    }
  }

  /// 转换为缓存代理URL
  Future<String> convertToCacheProxyUrl(String url) async {
    return _hostApi.convertToCacheProxyUrl(url);
  }

  /// 获取转码地址
  Future<String> getTransCodeUrl(String url, Map<String, String> headers) async {
    return _hostApi.getTransCodeUrl(url, headers);
  }
}
