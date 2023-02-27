# OpenAI Auth

This project is the Java version(Only Auth model) of [revChatGPT](https://github.com/acheong08/ChatGPT).

## How to use

Create an auth object:


```java
OpenAIAuth openAIAuth = new OpenAIAuth("<your email>","<your password>");
OpenAISession auth = openAIAuth.auth();
System.out.println(JSON.toJSONString(auth));
```

Create an auth object with proxy:

```java
// Create proxy if needed
Proxy proxy = new OpenAIProxy("127.0.0.1",
        11111, Proxy.Type.HTTP).build();
OpenAIAuth openAIAuth = new OpenAIAuth("<your email>","<your password>",proxy);
OpenAISession auth = openAIAuth.auth();
System.out.println(JSON.toJSONString(auth));
```
