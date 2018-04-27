package com.study.wumu.netty4springboot.benchmarkTest;

import org.openjdk.jmh.annotations.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dydy on 2018/1/18.
 */
@BenchmarkMode(Mode.Throughput)
public class Netty4SpringbootBeachmark {

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 20)
    public void getByFetch(){
        getUrl("http://localhost:8080/loan/kaleidoscope/proxy/credit/fetch?uid=1234", false);
    }

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 10)
    public void getByNetty() {
        getUrl("http://127.0.0.1:8080/test", false);
    }

    private String getUrl(String url, boolean read) {
        BufferedReader br = null;
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            URL reqURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) reqURL.openConnection(); // 进行连接，但是实际上getrequest要在下一句的connection.getInputStream() 函数中才会真正发到服务器
            connection.setDoOutput(false);
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setDoInput(true);
            connection.connect();
            if (read) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } else {
                is = connection.getInputStream();

            }
        } catch (IOException e) {
            System.out.println("连接服务器'" + url + "'时发生错误：" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (null != br) {
                    br.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
