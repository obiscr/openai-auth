package com.obiscr;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenAI Auth
 * @author Wuzi
 */
public class OpenAIAuth {

    /**
     * OpenAI Authentication Reverse Engineered
     */
    private Proxy proxy;
    private String email;
    private String password;
    private String sessionToken;
    private final String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36";

    private List<HttpCookie> cookies;

    public OpenAIAuth(String email, String password) {
        new OpenAIAuth(email,password,null);
    }
    public OpenAIAuth(String email, String password, Proxy proxy) {
        this.email = email;
        this.password = password;
        this.proxy = proxy;
        try {
            begin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String urlEncode(String string) throws UnsupportedEncodingException {
        return URLEncoder.encode(string, "UTF-8");
    }

    private void begin() throws UnsupportedEncodingException, OpenAIException {
        String url = "https://explorer.api.openai.com/api/auth/csrf";
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "explorer.api.openai.com");
        headers.put("Accept", "*/*");
        headers.put("Connection", "keep-alive");
        headers.put("User-Agent", this.userAgent);
        headers.put("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8");
        headers.put("Referer", "https://explorer.api.openai.com/auth/login");
        headers.put("Accept-Encoding", "gzip, deflate, br");


        HttpResponse response = HttpUtil.createGet(url).headerMap(headers, true).setProxy(proxy).execute();
        cookies = response.getCookies();
        if (response.getStatus() == 200 && response.header("Content-Type").contains("json")) {
            JSONObject object = JSON.parseObject(response.body());
            step1(object.getString("csrfToken"));
        } else {
            throw new OpenAIException("begin", response.getStatus(), response.body());
        }
    }

    /**
     * We reuse the token from part to make a request to
     * /api/auth/signin/auth0?prompt=login
     *
     * @param token /
     */
    private void step1(String token) throws UnsupportedEncodingException, OpenAIException {
        String url = "https://explorer.api.openai.com/api/auth/signin/auth0?prompt=login";
        String payload = "callbackUrl=%2F&csrfToken=" + token + "&json=true";
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "explorer.api.openai.com");
        headers.put("User-Agent", this.userAgent);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "*/*");
        headers.put("Sec-Gpc", "1");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Origin", "https://explorer.api.openai.com");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Referer", "https://explorer.api.openai.com/auth/login");
        headers.put("Accept-Encoding", "gzip, deflate");

        HttpResponse response = HttpUtil.createPost(url).headerMap(headers, true).body(payload).cookie(cookies).setProxy(proxy).execute();
        cookies = response.getCookies();
        if (response.getStatus() == 200 && response.header("Content-Type").contains("json")) {
            JSONObject object = JSON.parseObject(response.body());
            url = object.getString("url");
            if (url.equals("https://explorer.api.openai.com/api/auth/error?error=OAuthSignin") || url.contains("error")) {
                throw new OpenAIException("__part_one", response.getStatus(), "You have been rate limited. Please try again later.");
            }
            step2(url);
        } else {
            throw new OpenAIException("__part_one", response.getStatus(), response.body());
        }
    }

    /**
     * We make a GET request to url
     *
     * @param url /
     * @throws UnsupportedEncodingException /
     * @throws OpenAIException /
     */
    private void step2(String url) throws UnsupportedEncodingException, OpenAIException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "auth0.openai.com");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Connection", "keep-alive");
        headers.put("User-Agent", this.userAgent);
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Referer", "https://explorer.api.openai.com/");
        HttpResponse response = HttpUtil.createGet(url).headerMap(headers, true).cookie(cookies).setProxy(proxy).execute();
        cookies = response.getCookies();
        if (response.getStatus() == 302 || response.getStatus() == 200) {
            String regex = "state=([\\w\\d]+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(response.body());
            if (matcher.find()) {
                String stateValue = matcher.group(1);
                step3(stateValue);
            } else {
                throw new OpenAIException("__part_two", response.getStatus(), "state not matched in:" + response.body());
            }
        } else {
            throw new OpenAIException("__part_two", response.getStatus(), response.body());
        }
    }

    /**
     * We use the state to get the login page
     *
     * @param state /
     */
    private void step3(String state) throws UnsupportedEncodingException, OpenAIException {
        String url = "https://auth0.openai.com/u/login/identifier?state=" + state;
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "auth0.openai.com");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Connection", "keep-alive");
        headers.put("User-Agent", this.userAgent);
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Referer", "https://explorer.api.openai.com/");
        HttpResponse response = HttpUtil.createGet(url).headerMap(headers, true).cookie(cookies).setProxy(proxy).execute();
        cookies = response.getCookies();
        if (response.getStatus() == 200) {
            step4(state);
        } else {
            throw new OpenAIException("__part_three", response.getStatus(), response.body());
        }
    }

    /**
     * We make a POST request to the login page with the captcha, email
     *
     * @param state /
     */
    private void step4(String state) throws UnsupportedEncodingException, OpenAIException {
        String url = "https://auth0.openai.com/u/login/identifier?state=" + state;
        //String payload = "state=" + state + "&username" + this.email + "&js-available=false&webauthn-available=true&is-brave=false&webauthn-platform-available=true&action=default ";
        Map<String,Object> payload = new HashMap<>();
        payload.put("state", state);
        payload.put("username", this.email);
        payload.put("js-available", false);
        payload.put("webauthn-available", true);
        payload.put("is-brave", false);
        payload.put("webauthn-platform-available", true);
        payload.put("action", "default");
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "auth0.openai.com");
        headers.put("Origin", "https://auth0.openai.com");
        headers.put("Connection", "keep-alive");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("User-Agent", this.userAgent);
        headers.put("Referer", "https://auth0.openai.com/u/login/identifier?state=" + state);
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        HttpResponse response = HttpUtil.createPost(url).headerMap(headers, true).form(payload).cookie(cookies).setProxy(proxy).execute();
        cookies = response.getCookies();
        if (response.getStatus() == 200 || response.getStatus() == 302) {
            step5(state);
        } else {
            throw new OpenAIException("__part_four", response.getStatus(), "Your email address is invalid.");
        }
    }

    /**
     * We enter the password
     *
     * @param state /
     * @throws UnsupportedEncodingException /
     */
    private void step5(String state) throws UnsupportedEncodingException, OpenAIException {
        String url = "https://auth0.openai.com/u/login/password?state=" + state;
        String emailUrlEncoded = urlEncode(this.email);
        String passwordUrlEncoded = urlEncode(this.password);
        //String payload = "state=" + state + "&username=" + emailUrlEncoded + "&password=" + passwordUrlEncoded + "&action=default";
        Map<String, Object> payload = new HashMap<>();
        payload.put("state", state);
        payload.put("username", emailUrlEncoded);
        payload.put("password", passwordUrlEncoded);
        payload.put("action", "default");
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "auth0.openai.com");
        headers.put("Origin", "https://auth0.openai.com");
        headers.put("Connection", "keep-alive");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("User-Agent", this.userAgent);
        headers.put("Referer", "https://auth0.openai.com/u/login/password?state=" + state);
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        HttpResponse response = HttpUtil.createPost(url)
                .headerMap(headers, true).setFollowRedirects(false).form(payload).cookie(cookies).setProxy(proxy).execute();
        cookies = response.getCookies();
        if (response.getStatus() == 200 || response.getStatus() == 302) {
            String regex = "state=([\\w\\d]+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(response.body());
            if (matcher.find()) {
                String newState = matcher.group(1);
                step6(state, newState);
            } else {
                throw new OpenAIException("__part_five", response.getStatus(), "state not matched in:" + response.body());
            }
        } else {
            throw new OpenAIException("__part_five", response.getStatus(), "Your credentials are invalid.");
        }
    }

    private void step6(String oldState, String newState) throws OpenAIException {
        String url = "https://auth0.openai.com/authorize/resume?state=" + newState;
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "auth0.openai.com");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Connection", "keep-alive");
        headers.put("User-Agent", this.userAgent);
        headers.put("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8");
        headers.put("Referer", "https://auth0.openai.com/u/login/password?state=" + oldState);
        HttpResponse response = HttpUtil.createGet(url)
                .headerMap(headers, true)
                .setFollowRedirects(false)
                .cookie(cookies)
                .setProxy(proxy)
                .execute();
        cookies = response.getCookies();
        if (response.getStatus() == 302) {
            String redirectUrl = response.header("location");
            step7(redirectUrl, url);
        } else {
            throw new OpenAIException("__part_six", response.getStatus(), response.body());
        }
    }

    private void step7(String redirectUrl, String previousUrl) throws OpenAIException {
        String url = redirectUrl;
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "explorer.api.openai.com");
        headers.put("Accept", "application/json");
        headers.put("Connection", "keep-alive");
        headers.put("User-Agent", this.userAgent);
        headers.put("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8");
        headers.put("Referer", previousUrl);
        HttpResponse response = HttpUtil.createGet(url).setFollowRedirects(false).headerMap(headers, true)
                // 这一步不需要携带cookies
                //.cookie(cookies)
                .setProxy(proxy).execute();
        cookies = response.getCookies();
        if (response.getStatus() == 200 || response.getStatus() == 302) {
            this.sessionToken = response.getCookieValue("__Secure-next-auth.session-token");
        } else {
            throw new OpenAIException("__part_seven", response.getStatus(), response.body());
        }
    }

    /**
     * Gets access token
     */
    public OpenAISession auth() throws OpenAIException {
        HttpResponse response = HttpUtil.createGet("https://explorer.api.openai.com/api/auth/session").
                cookie("__Secure-next-auth.session-token=" + this.sessionToken).cookie(cookies).setProxy(proxy).execute();
        cookies = response.getCookies();
        if (response.getStatus() == 200) {
            return new OpenAISession().parse(response.body());
        } else {
            throw new OpenAIException("getAccessToken", response.getStatus(), response.body());
        }
    }
}
