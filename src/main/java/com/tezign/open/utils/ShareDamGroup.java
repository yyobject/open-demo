package com.tezign.open.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.Charsets;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 根据素材组id，自动调用接口创建分享链接
 * 需要用到user_id和token
 * </p>
 *
 * @Author: wangyaoyao
 * @Date: 2020/12/7
 */
public class ShareDamGroup {
    /**
     * 分享落地页基础地址
     */
    private static String TENANT_SHARE_BASE_URL = "https://v.tezign.com/dam_enterprise/share/";
    /**
     * 租户域名地址
     */
    private static String TENANT_BASE_URL = "https://v.tezign.com";

    public static void main(String[] args){
        /**
         * 登录后的用户id，从登录后任一接口的x-user-id请求头
         */
        String userId = "1";
        /**
         * 登录后的token，x-token请求头值
         */
        String token = "7b049034ffa9ba699cc8bffdcb223b9d";
        /**
         * 执行sql获取数据列表后存入本地
         * 相关sql，其中390是顶级素材组id，可以打开素材组在地址栏查看：select concat(id,",",group_name) from vms_dam_group where status=0 and root_id=390
         * 注意，素材名不要有双引号
         */
        Path path = Paths.get("/Users/tezign/tallzz.txt");
        Path resultPath = Paths.get("/Users/tezign/tallzz_result.txt");
        List<String> result = new ArrayList<>();
        try {
            Files.lines(path,StandardCharsets.UTF_8).forEach(o->{
                String[] split = o.split(",",2);
                String group_id = split[0];
                String name = split[1];
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("title",name);
                    jo.put("selectType",2);
                    jo.put("validityType",1);
                    jo.put("watermarkOpen",0);
                    jo.put("attachmentOpen",0);
                    jo.put("viewPassword","");
                    jo.put("needPassword",0);
                    jo.put("originalUrl",TENANT_SHARE_BASE_URL);
                    jo.put("damIds",new JSONArray());
                    JSONArray ja = new JSONArray();
                    ja.add(group_id);
                    jo.put("groupIds",ja);
                    String shareResult = Request.Post("https://vms-service.tezign.com/material/share/createShare")
                            .addHeader("referer", TENANT_BASE_URL+"/")
                            .addHeader("origin", TENANT_BASE_URL)
                            .addHeader("x-token", token)
                            .addHeader("x-user-id", userId)
                            .addHeader("content-type", "application/json;charset=UTF-8")
                            .bodyString(jo.toJSONString(), ContentType.APPLICATION_JSON)
                            .execute().returnContent().asString(Charsets.UTF_8);
                    String shareUrl = JSON.parseObject(shareResult).getJSONObject("result").getString("linkUrl");
                    result.add(o+","+shareUrl);
                    if(result.size() % 100==0){
                        System.out.println(result.size());
                    }
                } catch (Exception e) {
                    System.out.println("失败"+o);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Files.write(resultPath,result,StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("保存失败："+JSON.toJSONString(result));
        }
        System.out.println("end");
    }
}
