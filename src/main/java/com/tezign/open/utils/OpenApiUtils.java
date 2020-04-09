package com.tezign.open.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * <p>
 * 接口检验
 * </p>
 *
 * @Author: wangyaoyao
 * @Date: 2020/4/9
 */
@Slf4j
@Data
public class OpenApiUtils {

    /**
     * 应用key（由特赞人员提供）
     */
    private static final String clientKey = "adf5d1df73ea4bd39cb1e917b41cc544";


    /**
     * 私钥 （由特赞人员提供）
     */
    private static final String clientSecret = "b8a7e10b630f4b90838ad468b980557e";


    private static final String URI = "https://open-api.tezign.com";
    /**
     * 开放平台令牌header名
     */
    private static final String HEADER_OPEN_API_TOKEN_NAME = "X-Open-Api-Token";
    /**
     * 全局开放平台令牌
     */
    public static String HEADER_OPEN_API_TOKEN_VALUE = "";

    /**
     * 用户登陆后的token
     */
    private static final String HEADER_OPEN_USER_TOKEN_NAME = "X-Open-User-Token";

    /**
     * 注意 测试时需要配置host
     * 47.94.216.194 open-api.tezign.com
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        /**
         * 获取平台调用令牌  -这个令牌有效期只有1小时，请保持频率获取更换
         */
        String openApiToken = askOpenApiToken();
        HEADER_OPEN_API_TOKEN_VALUE = openApiToken;
        System.out.println(openApiToken);

        /**
         * 登录，获取用户特定token
         */
        String userToken = askOpenUserToken("wyyrockking@163.com", "qq111111");
        /**
         * 登陆状态下请求用户信息
         */
        String userInfo = askUserInfoByUserToken(userToken);
        System.out.println(userInfo);

    }

    /**
     * 获取开放平台调用token
     */
    public static String askOpenApiToken() throws IOException {

        /**
         * 时间
         */
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        /**
         * sign生成规则
         */
        String sign = MD5Utils.md5(clientKey + time + clientSecret).toLowerCase();

        JSONObject data = new JSONObject();
        data.put("clientKey", clientKey);
        data.put("time", time);
        data.put("sign", sign);

        String content = Request.Post(URI + "/open-api/v1/common/token/generate")
                .bodyString(data.toJSONString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString(Charsets.UTF_8);
        log.debug("获取OpenApiToken返回结果:{}", content);
        JSONObject result = JSON.parseObject(content);
        if (Objects.equals(result.getString("code"), "0")) {
            return result.getString("result");
        } else {
            throw new RuntimeException("获取OpenApiToken失败:" + content);
        }
    }

    /**
     * 用户登陆
     *
     * @param userName
     * @param password
     * @return
     * @throws IOException
     */
    public static String askOpenUserToken(String userName, String password) throws IOException {

        JSONObject data = new JSONObject();
        data.put("userName", userName);
        data.put("password", password);

        String content = Request.Post(URI + "/open-api/v1/customized/token/generate")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .bodyString(data.toJSONString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString(Charsets.UTF_8);
        log.debug("获取OpenUserToken返回结果:{}", content);
        JSONObject result = JSON.parseObject(content);
        if (Objects.equals(result.getString("code"), "0")) {
            return result.getJSONObject("result").getString("token");
        } else {
            throw new RuntimeException("获取OpenUserToken失败:" + content);
        }
    }

    /**
     * 用户登陆后，获取用户信息
     *
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String askUserInfoByUserToken(String userToken) throws IOException {
        String content = Request.Get(URI + "/open-api/v1/customized/user/base-info")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }

}
