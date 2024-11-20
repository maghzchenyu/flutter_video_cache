import 'package:video_cache/plugin/pigeon.g.dart';

class VideoCache {
  VideoCache._internal();

  factory VideoCache() => _instance;

  static final VideoCache _instance = VideoCache._internal();

  final LXFVideoCacheHostApi _hostApi = LXFVideoCacheHostApi();

  /// 转换为缓存代理URL
  Future<String> convertToCacheProxyUrl(String url) async {
    return _hostApi.convertToCacheProxyUrl(url);
  }
  
  /// 获取转码地址
  Future<String> getTransCodeUrl(String url, Map<String, String> headers) async {
    return _hostApi.getTransCodeUrl(url, headers);
  }
}
