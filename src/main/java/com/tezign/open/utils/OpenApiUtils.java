package com.tezign.open.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import java.io.IOException;
import java.net.URLEncoder;
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
    private static final String clientKey = "39d97c9a0b2bcd2844577d5375a22720";


    /**
     * 私钥 （由特赞人员提供）
     */
    private static final String clientSecret = "71d1b5fdac7963d76567d5f145e17a80";


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
        String userName = "aaa333@qq.com";
        String pwd = null;

        /**
         * 获取平台调用令牌  -这个令牌有效期只有1小时，请保持频率获取更换
         */
        String openApiToken = askOpenApiToken();
        HEADER_OPEN_API_TOKEN_VALUE = openApiToken;
        log.info("成功获取「X-Open-Api-Token」: {}",openApiToken);

//        /**
//         * 重置供应商密码
//         * @deprecated
//         */
//        String s = resetPwd(userName, "123456");
//        log.info("重置供应商密码返回:{}",s);
        //移除用户白名单
//        whitelistremove(userName);
        //新增用户白名单
        whitelistAdd(userName, "", "张三");
        //修改信息
//        whitelistModify(userName,"18300000000","李四");


        /**
         * 登录，获取用户特定token
         * 当前Unidos系统不做密码校验
         */
        String userToken = askOpenUserToken(userName,null);
        log.info("成功获取「X-Open-User-Token」:{}",userToken);

//        /**
//         * 登陆状态下请求用户信息
//         * @deprecated
//         */
//        String userInfo = askUserInfoByUserToken(userToken);
//        log.info("用户信息:{}",userInfo);

//        /**
//         * 用户修改密
//         * @deprecated
//         */
//        boolean b = changePwd(userToken,userName, "123456", pwd);
//        log.info("修改用户密码:{}",b);

        /**
         * 获取跳转地址
         */
        String tokenUrl = accessTokenUrl("aaa", userToken);
        log.info("用户跳转地址:{}",tokenUrl);

//        /**
//         * rfp消息列表
//         * @deprecated
//         */
//        String s1 = rfpNotice(userToken);
//        log.info("rfp消息列表:{}",s1);

        /**
         * 个人收藏的产品列表
         */
        String s2 = starSpuList(userToken);
        log.info("个人收藏的产品列表:{}",s2);

//        try {
//            JSONArray result = JSON.parseObject(s2).getJSONObject("result").getJSONArray("list");
//            Long spuId = result.getJSONObject(0).getLong("spuId");
//            if(Objects.nonNull(spuId)){
//                /**
//                 * 根据spuId查询详情
//                 */
//                String s3 = spuDetail(spuId, userToken);
//                log.info("根据spuId查询详情，spuId={}:{}",spuId,s3);
//
//                /**
//                 * 取消收藏
//                 */
//                String s4 = unStar(spuId, userToken);
//                log.info("取消收藏，spuId={}:{}",spuId,s4);
//
//                /**
//                 * 收藏
//                 */
//                String star = star(spuId, userToken);
//                log.info("收藏，spuId={}:{}",spuId,star);
//
//                Long designerId = result.getJSONObject(0).getLong("belongId");
//                String s5 = designerInfo(designerId, userToken);
//                log.info("产品供应商信息，designerId={}:{}",designerId,s5);
//
//                String s6 = spuTags(spuId, userToken);
//                log.info("产品标签，spuId={}:{}",spuId,s6);
//
//                String s7 = querySimilarSpu(spuId, 0, 3, userToken);
//                log.info("分页查询相似产品，spuId={}:{}",spuId,s7);
//
//                String s8 = queryRecommendSpu(spuId, userToken);
//                log.info("系统、供应商推荐产品，spuId={}:{}",spuId,s8);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        /**
         * 分页查询收藏的案例列表
         */
        String s3 = queryPortfolioList(10,1,0,userToken);
        log.info("收藏的案例列表:{}",s3);

        try{
            JSONArray jsonArray = JSON.parseObject(s3).getJSONObject("result").getJSONArray("portfolioList");
            Long id = jsonArray.getJSONObject(0).getLong("portfolioId");
            String s4 = portfolioDetail(id, userToken);
            log.info("案例详情，id={}:{}",id,s4);

            String s5 = unStarPortfolio(id, userToken);
            log.info("取消收藏案例，id={}:{}",id,s5);

            String s6 = starPortfolio(id, userToken);
            log.info("收藏案例，id={}:{}",id,s6);

        }catch (Exception e){
            e.printStackTrace();
        }

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
     * 创建白名单
     * @param userName
     * @param phone
     * @param nickName
     * @return
     * @throws IOException
     */
    public static String whitelistAdd(String userName, String phone,String nickName) throws IOException {

        JSONObject data = new JSONObject();
        data.put("userName", userName);
        data.put("phone", phone);
        data.put("nickName", nickName);

        String content = Request.Post(URI + "/open-api/v1/customized/whitelist/add")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .bodyString(data.toJSONString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString(Charsets.UTF_8);
        log.debug("whitelistAdd返回结果:{}", content);
        JSONObject result = JSON.parseObject(content);
        if (Objects.equals(result.getString("code"), "0")) {
            return result.toString();
        } else {
            throw new RuntimeException("whitelistAdd失败:" + content);
        }
    }
    public static String whitelistremove(String userName) throws IOException {

        JSONObject data = new JSONObject();
        data.put("userName", userName);

        String content = Request.Post(URI + "/open-api/v1/customized/whitelist/remove")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .bodyString(data.toJSONString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString(Charsets.UTF_8);
        log.debug("whitelistRemove返回结果:{}", content);
        JSONObject result = JSON.parseObject(content);
        if (Objects.equals(result.getString("code"), "0")) {
            return result.toString();
        } else {
            throw new RuntimeException("whitelistRemove失败:" + content);
        }
    }
    public static String whitelistModify(String userName, String phone,String nickName) throws IOException {

        JSONObject data = new JSONObject();
        data.put("userName", userName);
        data.put("phone", phone);
        data.put("nickName", nickName);

        String content = Request.Post(URI + "/open-api/v1/customized/whitelist/modify")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .bodyString(data.toJSONString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString(Charsets.UTF_8);
        log.debug("whitelistModify返回结果:{}", content);
        JSONObject result = JSON.parseObject(content);
        if (Objects.equals(result.getString("code"), "0")) {
            return result.toString();
        } else {
            throw new RuntimeException("whitelistModify失败:" + content);
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

    /**
     * 系统重置密码
     * 用于忘记密码
     *
     * @param userName
     * @param password
     * @return
     * @throws IOException
     */
    public static String resetPwd(String userName, String password) throws IOException {

        JSONObject data = new JSONObject();
        data.put("userName", userName);
        data.put("password", password);

        String content = Request.Post(URI + "/open-api/v1/customized/reset/password")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .bodyString(data.toJSONString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString(Charsets.UTF_8);
        log.debug("获取resetPwd返回结果:{}", content);
        JSONObject result = JSON.parseObject(content);
        if (Objects.equals(result.getString("code"), "0")) {
            return result.getJSONObject("result").getString("token");
        } else {
            throw new RuntimeException("resetPwd返回结果失败:" + content);
        }
    }

    /**
     * 用户登陆状态下修改自己的密码
     *
     * @param userName
     * @param password
     * @return
     * @throws IOException
     */
    public static boolean changePwd(String userToken, String userName, String oldPassword, String password) throws IOException {

        JSONObject data = new JSONObject();
        data.put("oldPassword", oldPassword);
        data.put("password", password);

        String content = Request.Post(URI + "/open-api/v1/customized/user/change-pwd")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .bodyString(data.toJSONString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString(Charsets.UTF_8);
        log.debug("获取changePwd返回结果:{}", content);
        JSONObject result = JSON.parseObject(content);
        if (Objects.equals(result.getString("code"), "0")) {
            return true;
        } else {
            throw new RuntimeException("changePwd返回结果失败:" + content);
        }
    }

    /**
     * 免密跳转获取授权地址
     *
     * @param path
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String accessTokenUrl(String path, String userToken) throws IOException {
        if(Objects.nonNull(path)){
            path = URLEncoder.encode(path,"UTF-8");
        }
        String content = Request.Get(URI + "/open-api/v1/customized/user/redirect-url?path=" + path)
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .execute().returnContent().asString(Charsets.UTF_8);
        log.debug("获取accessToken返回结果:{}", content);
        JSONObject result = JSON.parseObject(content);
        if (Objects.equals(result.getString("code"), "0")) {
            return result.getString("result");
        } else {
            throw new RuntimeException("accessToken返回结果失败:" + content);
        }
    }

    /**
     * 获取rfp消息
     *
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String rfpNotice(String userToken) throws IOException {
        String content = Request.Get(URI + "/open-api/v1/customized/user/rfp/notice/unread-proposal")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }

    /**
     * 查询用户收藏的产品列表
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String starSpuList(String userToken) throws IOException {
        String content = Request.Get(URI + "/open-api/v1/customized/user/product/product-collection-item/query-default-collection-items")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }
    /**
     * 产品详情
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String spuDetail(Long spuId,String userToken) throws IOException {
        String content = Request.Get(URI + "/open-api/v1/customized/user/product/query-details?spuId="+spuId)
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }

    /**
     * 查询用户收藏的产品列表
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String unStar(Long spuId,String userToken) throws IOException {
        JSONObject data = new JSONObject();
        data.put("id", spuId);
        String content = Request.Post(URI + "/open-api/v1/customized/user/product/product-collection-item/delete-item-from-default-collection")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .bodyString(data.toJSONString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }

    /**
     * 查询用户收藏的产品列表
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String star(Long spuId,String userToken) throws IOException {
        JSONObject data = new JSONObject();
        data.put("id", spuId);
        String content = Request.Post(URI + "/open-api/v1/customized/user/product/product-collection-item/add-item-2default")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .bodyString(data.toJSONString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }

    /**
     * 查询用户收藏的案例列表
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String queryPortfolioList(Integer pageSize,Integer sort,Integer row,String userToken) throws IOException {
        String content = Request.Get(URI + "/open-api/v1/customized/user/collection/portfolioList?pageSize="+pageSize+"&sort="+sort+"&row="+row)
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }
    /**
     * 案例详情
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String portfolioDetail(Long id,String userToken) throws IOException {
        String content = Request.Get(URI + "/open-api/v1/customized/user/portfolio/manager/portfolio/detail?id="+id)
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }

    /**
     * 查询用户收藏的产品列表
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String unStarPortfolio(Long id,String userToken) throws IOException {
        JSONObject data = new JSONObject();
        data.put("id", id);
        String content = Request.Post(URI + "/open-api/v1/customized/user/collection/cancelPortfolio")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .bodyString(data.toJSONString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }

    /**
     * 查询用户收藏的产品列表
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String starPortfolio(Long id,String userToken) throws IOException {
        JSONObject data = new JSONObject();
        data.put("id", id);
        String content = Request.Post(URI + "/open-api/v1/customized/user/collection/addPortfolio")
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .bodyString(data.toJSONString(), ContentType.APPLICATION_JSON)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }

    /**
     * 查询产品附带的供应商信息
     * @param designerId
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String designerInfo(Long designerId,String userToken) throws IOException {
        String content = Request.Get(URI + "/open-api/v1/customized/user/product/designer-simple-info?designerId="+designerId)
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }

    /**
     * 产品标签
     * @param spuId
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String spuTags(Long spuId,String userToken) throws IOException {
        String content = Request.Get(URI + "/open-api/v1/customized/user/product/spu/tag/query-tags-by-spu-id?spuId="+spuId)
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }

    /**
     * 查询相似产品
     * @param spuId
     * @param startPoint    从startPoint开始查询  0
     * @param endPoint      查到第endPoint个     10
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String querySimilarSpu(Long spuId,Integer startPoint,Integer endPoint,String userToken) throws IOException {
        String content = Request.Get(URI + "/open-api/v1/customized/user/product/search/query-by-spu-id-similar-attr?spuId="+spuId
        +"&startPoint="+startPoint+"&endPoint="+endPoint)
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }

    /**
     * 产品详情页 系统推荐和供应商推荐的产品
     * @param spuId
     * @param userToken
     * @return
     * @throws IOException
     */
    public static String queryRecommendSpu(Long spuId,String userToken) throws IOException {
        String content = Request.Get(URI + "/open-api/v1/customized/user/product/user/get-recommends?spuId="+spuId)
                .addHeader(HEADER_OPEN_API_TOKEN_NAME, HEADER_OPEN_API_TOKEN_VALUE)
                .addHeader(HEADER_OPEN_USER_TOKEN_NAME, userToken)
                .execute().returnContent().asString(Charsets.UTF_8);
        return content;
    }
}
