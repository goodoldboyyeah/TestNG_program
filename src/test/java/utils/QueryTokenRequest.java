package utils;

import com.google.gson.JsonObject;
import lombok.*;

import static utils.HttpUtils.queryTokenPost;
import static utils.JsonFileUtil.transferJsonObject;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class QueryTokenRequest {

    private String grantType;
    private String clientId;
    private String clientSecret;
    private String userName;
    private String password;
    private String url;
    private String queryType;


}
