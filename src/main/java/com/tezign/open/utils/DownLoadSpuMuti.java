package com.tezign.open.utils;

import com.alibaba.fastjson.JSON;
import org.apache.http.client.fluent.Request;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>
 *
 * </p>
 *
 * @Author: wangyaoyao
 * @Date: 2020/12/7
 */
public class DownLoadSpuMuti {

    private static ConcurrentLinkedQueue<String> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    private static ConcurrentLinkedQueue<String> errorRet = new ConcurrentLinkedQueue<>();

    public static void main(String[] args){
        Path path = Paths.get("/Users/tezign/spus.txt");
        try {
            Files.lines(path,StandardCharsets.UTF_8).forEach(o->{
                concurrentLinkedQueue.add(o);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.execute(()->{
            List<String> erroraa = new ArrayList<>();
            while (!concurrentLinkedQueue.isEmpty()){
                String poll = concurrentLinkedQueue.poll();
                System.out.println(poll);
                if(!StringUtils.isEmpty(poll)){
                    String[] split = poll.split(",");
                    try {
                        Request.Get(split[1]).execute().saveContent(new File("/Users/tezign/spu_t1",split[0]));
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorRet.add(poll);
                    }
                }
            }
            System.out.println("错误"+JSON.toJSONString(erroraa));
        });
        executorService.shutdown();
    }
}
