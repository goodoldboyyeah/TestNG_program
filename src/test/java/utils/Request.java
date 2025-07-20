package utils;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuchao22
 * @CreateTime: 2025-07-12
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Request {
    private String comments;
    private String protocol;//https or http
    private String requestType;//get or post
    private String url;
    private List<String> request;
    private Map<String,String> headers;
    private Map<String,String> params;

}
