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
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

@Slf4j
public class SocketIo {
    /**
     * Production
     */
    public static final String HOST = "https://www.coinw.com";
    public static final String ENDPOINT = "wss://ws.futurescw.info";

    public static final String PUBLIC_TOKEN_URL = HOST + "/pusher/public-token";


    public static void main(String[] args) throws JsonProcessingException, URISyntaxException {
        connection();
    }

    private static void connection() throws JsonProcessingException, URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(PUBLIC_TOKEN_URL, String.class);
        String body = response.getBody();
        log.info("response body:{}", body);
        //Parse json
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(body);
        JsonNode data = jsonNode.get("data");
        //String endpoint = data.get("endpoint").asText();
        String endpoint = ENDPOINT;
        String token = data.get("token").asText();

        newConnection(endpoint, token);
    }

    private static void newConnection(String endpoint,String token) throws URISyntaxException {
        //todo modify according to actual status of interface (parameters of request: args)
        //String channel = "spot/candle-15m:BTC-USDT";
        String channel = "spot/market-api-ticker:BTC-USDT";
        IO.Options options = new IO.Options();
        options.transports = new String[]{"websocket"};
        options.reconnectionAttempts = 2;
        options.reconnectionDelay = 10000; // time interval of reconnection(ms) after failure
        options.timeout = 200000; // duration of connection timeout (ms)
        options.forceNew = true;
        options.query = "token=" + token;
        //UriComponentsBuilder.fromUriString(endpoint)
        //        .scheme("https")
        String url = endpoint.replaceAll("wss://", "https://").replaceAll("ws://", "http://");
        System.out.println("url:" + url);
        Socket socket = IO.socket(url, options);

        socket.on(Socket.EVENT_CONNECT, args -> {
                    log.info("connected");
                    socket.emit("subscribe", "{\"args\": \"" + channel + "\"}");
                })
                .on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        log.info("reconnect，{}", args);

                    }
                })

                .on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        log.info("connection error，{}", args);
                    }
                }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        log.info("connection timeout，{}", args);
                    }
                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        //io server disconnect
                        log.info("disconnection，{}", args);
                        try {
                            if (Objects.equals("io server disconnect", args[0])) {
                                connection();
                            }
                        } catch (Exception e) {
                            log.error("connection error:{}", e.getMessage());
                        }
                    }
                }).on("subscribe", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        for (int i = 0; i < args.length; i++) {
                            //String data = ((JSONObject) args[0]).getString("data");
                            log.info("client data[{}]:{}", i, args[i]);
                        }
                        socket.disconnect();
                    }
                }).on(Socket.EVENT_PONG, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        log.info("pong:{}", args);
                    }
                });
        socket.connect();
        //socket.disconnect();
    }
}
