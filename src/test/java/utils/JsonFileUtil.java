package utils;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @Author: liuchao22
 * @CreateTime: 2025-07-12
 */


public class JsonFileUtil {

    //读取json文件
    public static Request readRequest(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        return JSON.parseObject(content, Request.class);
    }

    //将响应结果转为json形式
    public static JsonObject transferJsonObject(Response response){
        String json = response.getResponse();
        JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
        return jsonObj;
    }

}
