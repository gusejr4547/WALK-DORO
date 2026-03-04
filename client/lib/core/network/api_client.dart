import 'package:dio/dio.dart';
import 'package:walkdoro/data/services/token_storage.dart';

class ApiClient {
  final Dio _dio;
  final TokenStorage _tokenStorage;

  ApiClient(this._dio, this._tokenStorage) {
    _dio.options.baseUrl =
        'http://10.0.2.2:8080/api/v1'; // Default emulator localhost to PC localhost
    _dio.options.connectTimeout = const Duration(seconds: 10);
    _dio.options.receiveTimeout = const Duration(seconds: 10);

    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          final token = _tokenStorage.accessToken;
          if (token != null) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          return handler.next(options);
        },
        onError: (DioException e, handler) async {
          // Handle 401 Unauthorized for token refresh logic here later
          return handler.next(e);
        },
      ),
    );
  }

  Dio get dio => _dio;
}
