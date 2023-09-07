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
