package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
@Slf4j
public class SocketIo {
    /**
     * 生产
     */
    public static final String HOST = "https://www.coinw.com";
    public static final String ENDPOINT = "wss://ws.futurescw.info";

    /**
     * 测试
     */
    //public static final String HOST = "http://www-test02.cwfutures.fun/";
    //public static final String ENDPOINT = "ws://ws.ugukimj.cn";

    public static final String PUBLIC_TOKEN_URL = HOST + "/pusher/public-token";

    public static void main(String[] args) throws JsonProcessingException, URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(PUBLIC_TOKEN_URL, String.class);
        String body = response.getBody();
        log.info("response body:{}", body);
        //解析json
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(body);
        JsonNode data = jsonNode.get("data");
        //String endpoint = data.get("endpoint").asText();
        String endpoint = ENDPOINT;
        String token = data.get("token").asText();

        newConnection(endpoint, token);
        //new Scanner(System.in).nextLine(); // Don't close immediately.

    }

    private static void newConnection(String endpoint,String token) throws URISyntaxException {

        String channel = "spot/market-api-ticker:ETH-USDT";
        IO.Options options = new IO.Options();
        options.transports = new String[]{"websocket"};
        options.reconnectionAttempts = 2;
        options.reconnectionDelay = 10000; // 失败重连的时间间隔(ms)
        options.timeout = 200000; // 连接超时时间(ms)
        options.forceNew = true;
        options.query = "token=" + token;
        //UriComponentsBuilder.fromUriString(endpoint)
        //        .scheme("https")
        String url = endpoint.replaceAll("wss://", "https://").replaceAll("ws://", "http://");
        System.out.println("url:" + url);
        Socket socket = IO.socket(url, options);

        socket.on(Socket.EVENT_CONNECT, args -> {
                    log.info("已连接");
                    socket.emit("subscribe", "{\"args\": \"" + channel + "\"}");
                })
                .on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        log.info("链接异常，{}", args);
                    }
                }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        log.info("链接超时，{}", args);
                    }
                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        log.info("链接断开，{}", args);
                    }
                }).on("subscribe", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            String data = ((JSONObject) args[0]).getString("data");
                            log.info("client data:{}", data);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).on(Socket.EVENT_PONG, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        log.info("pong:{}", args);
                    }
                });
        socket.connect();
    }
}
