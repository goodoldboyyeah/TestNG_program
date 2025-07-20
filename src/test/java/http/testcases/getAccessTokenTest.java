package http.testcases;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import report.ZTestReport;
import utils.QueryTokenRequest;

import static utils.QueryToken.getToken;

@Listeners(ZTestReport.class)
public class getAccessTokenTest {
    @BeforeClass
    @Test

    public void test(){
        QueryTokenRequest request = new QueryTokenRequest("password","",
                "",
                "",
                "",
                "",
                "post");
        String result = getToken(request);
//        log.info("success");
//        log.info(result);
//        log.info("two");
    }
}
