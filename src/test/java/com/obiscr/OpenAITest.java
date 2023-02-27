package com.obiscr;

import com.alibaba.fastjson2.JSON;

import java.net.Proxy;

public class OpenAITest {
    public static void main(String[] args) throws OpenAIException {
        // Create proxy if needed
        Proxy proxy = new OpenAIProxy("127.0.0.1",
                10809, Proxy.Type.HTTP).build();

        OpenAIAuth openAIAuth = new OpenAIAuth(
                "<your email>",
                "<your password>",
                proxy);
        OpenAISession auth = openAIAuth.auth();
        System.out.println(JSON.toJSONString(auth));
    }

}
