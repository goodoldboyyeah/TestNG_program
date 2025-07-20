package utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;


import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: liuchao22
 * @CreateTime: 2025-07-12
 */


@Slf4j
public class HttpUtils {

    private static HttpClient httpClient;

    static {
        try {
            httpClient = HttpClients.createDefault(); // 确保这里不会抛异常
        } catch (Exception e) {
            throw new RuntimeException("初始化 HttpClient 失败", e);
        }
    }

    public static synchronized HttpClient getHttpClient() {
        if (null == httpClient) {
            httpClient = new DefaultHttpClient();
        }
        return httpClient;
    }
    //发送请求
    public static Response send(Request request){

        if(request.getRequestType().equals("get") && request.getProtocol().equals("http")){
          return sendGet(request);
        }else if(request.getRequestType().equals("post")&& request.getProtocol().equals("http")){
            return sendPost(request);
        }else if(request.getRequestType().equals("post")&& request.getProtocol().equals("https")){
            return sendSSLPost(request);
        }else{
            String result = "请求方式不存在，请添加。";
            Response response = new Response(result);
            return response;
        }
    }

    public static Response sendPost(Request request) {
        PrintWriter out = null;
        BufferedReader in = null;
        Response response = new Response();

        try {
            // 参数校验
            if (request == null || request.getUrl() == null || request.getUrl().isEmpty()) {
                throw new IllegalArgumentException("Request object or URL cannot be null or empty");
            }

            String url = request.getUrl();
            String param = request.getRequest() != null && !request.getRequest().isEmpty() ?
                    request.getRequest().get(0) : "";

            log.info("sendPost - {}", url);
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();

            // 设置默认请求头
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");

            // 添加自定义请求头
            if (request.getHeaders() != null) {
                for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            conn.setDoOutput(true);
            conn.setDoInput(true);

            // 发送请求参数
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();

            // 读取响应
            StringBuilder result = new StringBuilder();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }

            log.info("recv - {}", result);
            response.setResponse(result.toString());

        } catch (ConnectException e) {
            log.error("调用HttpUtils.sendPost ConnectException, url=" + request.getUrl() + ",param=" + request.getRequest(), e);
            response.setResponse("Connection error: " + e.getMessage());
        } catch (SocketTimeoutException e) {
            log.error("调用HttpUtils.sendPost SocketTimeoutException, url=" + request.getUrl() + ",param=" + request.getRequest(), e);
            response.setResponse("Timeout error: " + e.getMessage());
        } catch (IOException e) {
            log.error("调用HttpUtils.sendPost IOException, url=" + request.getUrl() + ",param=" + request.getRequest(), e);
            response.setResponse("IO error: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用HttpsUtil.sendPost Exception, url=" + request.getUrl() + ",param=" + request.getRequest(), e);
            response.setResponse("Error: " + e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                log.error("调用in.close Exception, url=" + request.getUrl(), ex);
            }
        }

        return response;
    }

    public static Response sendSSLPost(Request request) {
        Response response = new Response();
        StringBuilder result = new StringBuilder();

        try {
            // 参数校验
            if (request == null || request.getUrl() == null || request.getUrl().isEmpty()) {
                throw new IllegalArgumentException("Request object or URL cannot be null or empty");
            }

            String url = request.getUrl();
            String param = request.getRequest() != null && !request.getRequest().isEmpty() ?
                    request.getRequest().get(0) : "";

            String urlNameString = url + (param.isEmpty() ? "" : "?" + param);
            log.info("sendSSLPost - {}", urlNameString);

            // 创建SSL上下文
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new SecureRandom());

            // 创建HTTPS连接
            URL console = new URL(urlNameString);
            HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();

            // 设置请求头
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");

            // 添加自定义请求头
            if (request.getHeaders() != null) {
                for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
            conn.connect();

            // 读取响应
            try (InputStream is = conn.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String ret;
                while ((ret = br.readLine()) != null) {
                    if (!ret.trim().isEmpty()) {
                        result.append(new String(ret.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                    }
                }
            }

            log.info("recv - {}", result);
            response.setResponse(result.toString());

        } catch (ConnectException e) {
            log.error("调用HttpUtils.sendSSLPost ConnectException, url=" + request.getUrl(), e);
            response.setResponse("Connection error: " + e.getMessage());
        } catch (SocketTimeoutException e) {
            log.error("调用HttpUtils.sendSSLPost SocketTimeoutException, url=" + request.getUrl(), e);
            response.setResponse("Timeout error: " + e.getMessage());
        } catch (IOException e) {
            log.error("调用HttpUtils.sendSSLPost IOException, url=" + request.getUrl(), e);
            response.setResponse("IO error: " + e.getMessage());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("调用HttpsUtil.sendSSLPost SSL Exception, url=" + request.getUrl(), e);
            response.setResponse("SSL error: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用HttpsUtil.sendSSLPost Exception, url=" + request.getUrl(), e);
            response.setResponse("Error: " + e.getMessage());
        }

        return response;
    }


    public static Response sendGet(Request request) {
        return sendGetSub(request, StandardCharsets.UTF_8.name());
    }

    public static Response sendGetSub(Request request, String contentType) {
        Response response = new Response();
        BufferedReader in = null;

        try {
            // 参数校验
            if (request == null || request.getUrl() == null || request.getUrl().isEmpty()) {
                throw new IllegalArgumentException("Request object or URL cannot be null or empty");
            }

            String url = request.getUrl();
            String param = request.getParams() != null ? buildQueryString(request.getParams()) : "";

            String urlNameString = url + (param.isEmpty() ? "" : "?" + param);
            log.info("sendGet - {}", urlNameString);

            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();

            // 设置默认请求头
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            // 添加自定义请求头
            if (request.getHeaders() != null) {
                for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            connection.connect();

            // 读取响应
            StringBuilder result = new StringBuilder();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), contentType));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }

            log.info("recv - {}", result);
            response.setResponse(result.toString());

        } catch (ConnectException e) {
            log.error("调用HttpUtils.sendGet ConnectException, url=" + request.getUrl(), e);
            response.setResponse("Connection error: " + e.getMessage());
        } catch (SocketTimeoutException e) {
            log.error("调用HttpUtils.sendGet SocketTimeoutException, url=" + request.getUrl(), e);
            response.setResponse("Timeout error: " + e.getMessage());
        } catch (IOException e) {
            log.error("调用HttpUtils.sendGet IOException, url=" + request.getUrl(), e);
            response.setResponse("IO error: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用HttpsUtil.sendGet Exception, url=" + request.getUrl(), e);
            response.setResponse("Error: " + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                log.error("调用in.close Exception, url=" + request.getUrl(), ex);
            }
        }

        return response;
    }

    private static String buildQueryString(Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(URLEncoder.encode(entry.getKey(), String.valueOf(StandardCharsets.UTF_8)))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), String.valueOf(StandardCharsets.UTF_8)));
        }
        return queryString.toString();
    }

    public static Response queryTokenPost(QueryTokenRequest request) {
        Response response = new Response();
        StringBuilder result = new StringBuilder();

        try {
            // Parameter validation
            if (request == null || request.getUrl() == null || request.getUrl().isEmpty()) {
                throw new IllegalArgumentException("Request object or URL cannot be null or empty");
            }

            // Determine request method (default to POST if not specified)
            String requestMethod = (request.getQueryType() != null && !request.getQueryType().isEmpty())
                    ? request.getQueryType().toUpperCase()
                    : "POST";

            // Build the request parameters
            Map<String, String> parameters = new LinkedHashMap<>();
            if (request.getGrantType() != null) parameters.put("grant_type", request.getGrantType());
            if (request.getClientId() != null) parameters.put("client_id", request.getClientId());
            if (request.getClientSecret() != null) parameters.put("client_secret", request.getClientSecret());
            if (request.getUserName() != null) parameters.put("username", request.getUserName());
            if (request.getPassword() != null) parameters.put("password", request.getPassword());

            String url = request.getUrl();
            String urlWithParams = url;
            String requestBody = "";

            if ("GET".equals(requestMethod)) {
                // For GET requests, add parameters to URL
                String queryString = parameters.entrySet().stream()
                        .map(entry -> {
                            try {
                                return URLEncoder.encode(entry.getKey(), "UTF-8") + "=" +
                                        URLEncoder.encode(entry.getValue(), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            return null;
                        })
                        .collect(Collectors.joining("&"));

                if (!queryString.isEmpty()) {
                    urlWithParams = url + (url.contains("?") ? "&" : "?") + queryString;
                }
            } else {
                // For POST/PUT/etc., create request body
                requestBody = parameters.entrySet().stream()
                        .map(entry -> {
                            try {
                                return URLEncoder.encode(entry.getKey(), "UTF-8") + "=" +
                                        URLEncoder.encode(entry.getValue(), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            return null;
                        })
                        .collect(Collectors.joining("&"));
            }

            log.info("sendSSLRequest - {} {}", requestMethod, urlWithParams);

            // Create SSL context
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new SecureRandom());

            // Create HTTPS connection
            URL console = new URL(urlWithParams);
            HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();

            // Set request method and headers
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");

            if ("POST".equals(requestMethod) || "PUT".equals(requestMethod)) {
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
            }

            conn.setDoInput(true);
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new TrustAnyHostnameVerifier());

            // For POST/PUT requests, write the request body
            if (("POST".equals(requestMethod) || "PUT".equals(requestMethod)) && !requestBody.isEmpty()) {
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            // Read the response
            int responseCode = conn.getResponseCode();
            InputStream inputStream = responseCode < 400 ? conn.getInputStream() : conn.getErrorStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String ret;
                while ((ret = br.readLine()) != null) {
                    if (!ret.trim().isEmpty()) {
                        result.append(ret);
                    }
                }
            }

            log.info("Received response - Status: {}, Body: {}", responseCode, result);
            response.setResponse(result.toString());


        } catch (ConnectException e) {
            log.error("Calling HttpUtils.sendSSLPost ConnectException, url=" + (request != null ? request.getUrl() : "null"), e);
            response.setResponse("Connection error: " + e.getMessage());

        } catch (SocketTimeoutException e) {
            log.error("Calling HttpUtils.sendSSLPost SocketTimeoutException, url=" + (request != null ? request.getUrl() : "null"), e);
            response.setResponse("Timeout error: " + e.getMessage());

        } catch (IOException e) {
            log.error("Calling HttpUtils.sendSSLPost IOException, url=" + (request != null ? request.getUrl() : "null"), e);
            response.setResponse("IO error: " + e.getMessage());

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Calling HttpsUtil.sendSSLPost SSL Exception, url=" + (request != null ? request.getUrl() : "null"), e);
            response.setResponse("SSL error: " + e.getMessage());

        } catch (Exception e) {
            log.error("Calling HttpsUtil.sendSSLPost Exception, url=" + (request != null ? request.getUrl() : "null"), e);
            response.setResponse("Error: " + e.getMessage());

        }

        return response;
    }
    private static class TrustAnyTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{};
    }
}

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }











}
