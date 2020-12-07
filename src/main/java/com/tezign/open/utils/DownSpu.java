package com.tezign.open.utils;

import org.apache.http.client.fluent.Request;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @Author: wangyaoyao
 * @Date: 2020/12/7
 */
public class DownSpu {

    public static void main(String[] args){
        System.out.println("呵呵");
        Path path = Paths.get("/Users/tezign/spus.txt");
        Map<Long,String> map = new HashMap<>();
        try {
            Files.lines(path,StandardCharsets.UTF_8).forEach(o->{
                String[] split = o.split(",");
                String s = split[1];
                int i=0;
                try {
                    Request.Get(s).execute().saveContent(new File("/Users/tezign/spu_t1",split[0]));
                    i++;
                    if(i%100==0){
                        System.out.println(i);
                    }
                } catch (IOException e) {
                    System.out.println("失败"+o);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
