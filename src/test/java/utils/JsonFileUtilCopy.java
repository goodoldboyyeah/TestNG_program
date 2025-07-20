package utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.FileReader;
import java.io.IOException;
/**
 * @Author: liuchao22
 * @CreateTime: 2025-07-12
 */

public class JsonFileUtilCopy {
    public static JSONArray readJsonArray(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return JSONArray.parseArray(String.valueOf(reader));
        }

    }

    public static JSONObject readJsonObject(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return JSONObject.parseObject(String.valueOf(reader), JSONObject.class);
        }

    }
}
