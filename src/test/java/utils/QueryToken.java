package utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static utils.HttpUtils.queryTokenPost;
import static utils.JsonFileUtil.transferJsonObject;

public class QueryToken {

    public static String getToken(QueryTokenRequest request){
        Response response = queryTokenPost(request);
        JsonObject obj = transferJsonObject(response);
        String accessToken = obj.get("access_token").getAsString();
        return accessToken;
    }

}
