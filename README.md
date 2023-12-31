#  coinw  Api 文档
[English Documentation](README-en.md)

[API 文档](https://www.coinw.com/front/API)

## Web Socket 示例
### Java 
[SocketIo.java](src%2Fmain%2Fjava%2FSocketIo.java)

```java
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
     * 生产
     */
    public static final String HOST = "https://www.coinw.loan";
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
        //解析json
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(body);
        JsonNode data = jsonNode.get("data");
        //String endpoint = data.get("endpoint").asText();
        String endpoint = ENDPOINT;
        String token = data.get("token").asText();

        newConnection(endpoint, token);
    }

    private static void newConnection(String endpoint,String token) throws URISyntaxException {
        //todo 根据接口实际情况修改(请求参数:args)
        //String channel = "spot/candle-15m:BTC-USDT";
        String channel = "spot/market-api-ticker:BTC-USDT";
        IO.Options options = new IO.Options();
        options.transports = new String[]{"websocket"};
        options.reconnectionAttempts = 2;
        options.reconnectionDelay = 10000; // 失败重连的时间间隔(ms)
        options.rememberUpgrade = true;
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
                .on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        log.info("重新连接，{}", args);

                    }
                })
                .on(Socket.EVENT_RECONNECT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        log.info("重新连接错误，{}", args);

                    }
                })
                .on(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        log.info("重新连接失败，{}", args);

                    }
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
                        //io server disconnect
                        log.info("链接断开，{}", args);
                        try {
                            if (Objects.equals("io server disconnect", args[0])) {
                                connection();
                            }
                        } catch (Exception e) {
                            log.error("重连错误:{}", e.getMessage());
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

```
### JS

```html
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>Socket接口测试</title>
</head>

<body>
<script src="https://cdn.bootcss.com/socket.io/2.1.1/socket.io.js"></script>
<script type="text/javascript">

    var host = 'https://www.coinw.com'
    //todo 根据接口实际情况修改(请求参数:args)
    var channel = 'spot/market-api-ticker:ETH-USDT';
    var socket= '';
    function initSocket(url) {
        socket = io(url, {transports: ["websocket"]});
        socket.on("connect",function(
        ){ getData()});

        socket.on('subscribe', (rsp) => {
            console.log(rsp.data)
        });
    };
    function getData () {
        if (socket.connected) {
            // 发起订阅示例
            socket.emit("subscribe", JSON.stringify({"args": channel})
                    ,(ack) => {
                        console.log(ack)
                    });
        } else {
            // 发起重连
        }
    };
    var httpRequest = new XMLHttpRequest();
    httpRequest.open('GET', host + '/pusher/public-token', true);
    httpRequest.send();
    httpRequest.onreadystatechange = function () {
        if (httpRequest.status == 200 && httpRequest.readyState == 4) {
            var response = httpRequest.responseText; //获取到json字符串，还需解析
            response = JSON.parse(response);
            var endpoint = response.data.endpoint;
            var token = response.data.token;
            var url = `${endpoint}?token=${token}`;
            console.log(url)
            initSocket(url);
        }
    };
</script>
</body>

</html>
```

## API 示例   

### Python
<br/>

```python
# -*- coding:utf-8 -*-
import hashlib
import urllib
import urllib.parse
import urllib.request
import requests
import random


class SDK:
    """
    Desc：创建签名
    """

    def create_sign(self, params, secret_key):
        sorted_params = sorted(params.items(), key=lambda d: d[0], reverse=False)
        # encode_params = urllib.parse.urlencode(sorted_params)
        # 进行签名的参数不能进行编码，否则{"ids": "1,2"}这种会出现问题
        encode_params = ""
        for i in sorted_params:
            a = i[0] + "=" + str(i[1]) + "&"
            encode_params = encode_params + a
        sign_params = encode_params + "secret_key=" + secret_key
        input_name = hashlib.md5()
        input_name.update(sign_params.encode("utf-8"))
        print("签名参数", sign_params)
        sign = input_name.hexdigest()
        print("生成的签名：", sign.upper())
        return sign.upper()


def httpreq(host, path, method, api_key, params, secret_key):
    params["api_key"] = api_key
    sign = SDK().create_sign(params, secret_key)
    encode_params_req = urllib.parse.urlencode(params)
    print(encode_params_req)
    host = "{host}{path}&sign={sign}&{encode_params_req}".format(
        host=host, path=path, sign=sign, encode_params_req=encode_params_req
    )
    print("请求地址", host)
    if method.upper() == "POST":
        response = requests.post(
            host, data={}, headers={"Content-type": "application/json"}
        )
        print(response.json())
    if method.upper() == "GET":
        response = requests.get(
            host, params={}, headers={"Content-type": "application/json"}
        )
        print(response.json())


# 2.0线上
host = "https://api.coinw.com"


#用户信息
api_key = ""
secret_key = ""



# 检索币对的信息
# path = "/api/v1/public?command=returnTicker"
# method = "get"
# params = {}

# 检索币种信息
# path = "/api/v1/public?command=returnCurrencies"
# method = "get"
# params = {}

# 交易对深度信息
# path = "/api/v1/public?command=returnOrderBook"
# method = "get"
# params = {"symbol": "LTC_USDT"}

# 交易对最近成交
# path = "/api/v1/public?command=returnTradeHistory"
# method = "get"
# params = {"symbol": "LTC_USDT"}

# k线数据  -----fail
# path = "/api/v1/public?command=returnChartData"
# method = "get"
# params = {"period": 180, "currencyPair": "LTC_USDT"}

# 获取热门市场24小时成交量
# path = "/api/v1/public?command=return24hVolume"
# method = "get"
# params = {}

# 返回指定交易对的未完成订单列表
path = "/api/v1/private?command=returnOpenOrders"
method = "post"
params = {"currencyPair": "EOS_USDT","startAt": "","endAt":""}

# 返回指定订单的详细信息
# path = "/api/v1/private?command=returnOrderTrades"
# method = "post"
# params = {"orderNumber": "4612553533101703208"}

# 返回指定订单的状态信息
# path = "/api/v1/private?command=returnOrderStatus"
# method = "post"
# params = {"orderNumber": "4612102733335378846"}

# 返回指定交易对的成交历史
# path = "/api/v1/private?command=returnUTradeHistory"
# method = "post"
# params = {"currencyPair": "LTC_USDT","startAt":"1631526172583","endAt":"1631526317779"}

# 返回可用余额
# path = "/api/v1/private?command=returnBalances"
# method = "post"
# params = {}


# # 下单
# path = "/api/v1/private?command=doTrade"
# method = "post"
#  #正常用户下单
# params = {"symbol": "LTC_USDT", "type": "0", "amount": "1", "rate": "14" , "out_trade_no" : "351ab3fa-5647-41e7-8305-1f3ecc6b979e"}

# 市价单
# path = "/api/v1/private?command=doTrade"
# method = "post"
# 按金额下单成交
# params = {"symbol": "EOS_USDT", "type": 0, "funds": 10, "isMarket": "true"}
# 按下单数量成交
# params = {"symbol": "SOL_USDT", "type": 1, "amount": 0.1, "isMarket": "true"}

# # 撤销订单
# path = "/api/v1/private?command=cancelOrder"
# method = "post"
# params = {"orderNumber": "4612181898174494829"}



# 公共timestamp接口
# path = "/api/v1/public?command=timestamp"
# method = "get"
# params = {}


# 获取单个订单
# path = "/api/v1/private?command=getOrder"
# method = "post"
# params = {"id":"4612878988543524865"}


# 返回交易对信息
# path = "/api/v1/public?command=returnSymbol"
# method = "get"
# params = {}

httpreq(host, path, method, api_key, params, secret_key)

```