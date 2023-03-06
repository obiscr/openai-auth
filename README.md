# OpenAI Auth

[![](https://img.shields.io/maven-central/v/com.obiscr/openai-auth?label=Maven%20Central)](https://search.maven.org/artifact/com.obiscr/openai-auth)

This project is the Java version(Only Auth model) of [revChatGPT](https://github.com/acheong08/ChatGPT).

## How to use

Import library

+ Maven

```xml
<dependency>
    <groupId>com.obiscr</groupId>
    <artifactId>openai-auth</artifactId>
    <version>1.0.0</version>
</dependency>
```

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
