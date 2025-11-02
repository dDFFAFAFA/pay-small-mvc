# API 接口清单和数据结构

## 一、接口概览

### 基础信息

- **基础路径**: `http://localhost:8080`
- **API版本**: `v1`
- **统一响应格式**: `Response<T>`
- **跨域支持**: 所有接口支持跨域（`@CrossOrigin("*")`）

### 响应码定义

| 响应码 | 说明 |
|--------|------|
| 0000 | 调用成功 |
| 0001 | 调用失败 |
| 0002 | 非法参数 |
| 0003 | 未登录 |

---

## 二、支付宝支付接口

### 2.1 创建支付订单

**接口路径**: `POST /api/v1/alipay/create_pay_order`

**功能描述**: 根据商品ID创建支付订单，返回支付宝支付链接

**请求头**:
```
Content-Type: application/json
```

**请求参数**:

```json
{
    "userId": "10001",
    "productId": "100001"
}
```

**请求DTO**: `CreatePayRequestDTO`

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | 是 | 用户ID |
| productId | String | 是 | 商品ID |

**响应示例**:

成功响应：
```json
{
    "code": "0000",
    "info": "调用成功",
    "data": "<form name=\"punchout_form\" method=\"post\" action=\"https://openapi-sandbox.dl.alipaydev.com/gateway.do?charset=utf-8&method=alipay.trade.page.pay&sign=...\">...</form>"
}
```

失败响应：
```json
{
    "code": "0001",
    "info": "调用失败",
    "data": null
}
```

**业务逻辑**:
1. 检查是否存在未支付订单（状态为 `PAY_WAIT`），存在则直接返回
2. 检查是否存在创建状态订单（状态为 `CREATE`），存在则创建支付单
3. 不存在则创建新订单，查询商品信息，创建支付单
4. 返回支付宝支付表单HTML

**状态码说明**:
- `0000`: 成功，返回支付链接
- `0001`: 失败，系统异常

---

### 2.2 支付宝支付回调

**接口路径**: `POST /api/v1/alipay/alipay_notify_url`

**功能描述**: 接收支付宝异步支付回调通知

**请求方式**: POST（支付宝服务器调用）

**请求参数**: 支付宝回调参数（form-data格式）

| 参数名 | 说明 | 示例 |
|--------|------|------|
| trade_status | 交易状态 | TRADE_SUCCESS |
| out_trade_no | 商户订单号 | 1234567890123456 |
| trade_no | 支付宝交易号 | 202312132200... |
| total_amount | 交易金额 | 1.68 |
| gmt_payment | 支付时间 | 2023-12-13 12:00:00 |
| sign | 签名 | xxxx... |
| ... | 其他支付宝参数 | ... |

**响应**:
- 成功：返回字符串 `"success"`
- 失败：返回字符串 `"false"`

**业务逻辑**:
1. 验证 `trade_status` 是否为 `TRADE_SUCCESS`
2. 验证支付宝签名（RSA256）
3. 签名验证通过后，更新订单状态为 `PAY_SUCCESS`
4. 发布支付成功事件
5. 返回 `"success"` 告知支付宝处理成功

**安全机制**:
- RSA256 签名验证，防止伪造回调
- 验证交易状态，确保只有成功订单才更新

**注意事项**:
- 此接口由支付宝服务器调用，不在前端直接访问
- 必须返回 `"success"`，否则支付宝会重复回调

---

## 三、微信登录接口

### 3.1 生成微信登录二维码

**接口路径**: `GET /api/v1/login/weixin_qrcode_ticket`

**功能描述**: 生成微信扫码登录的二维码 ticket

**请求参数**: 无

**响应示例**:

成功响应：
```json
{
    "code": "0000",
    "info": "调用成功",
    "data": "gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2aFo2Z3mTdSNDhXVFI5bkpWcjB6AAIE2ahZWAUEsAcAenQd"
}
```

失败响应：
```json
{
    "code": "0001",
    "info": "调用失败",
    "data": null
}
```

**业务逻辑**:
1. 从缓存获取微信 `accessToken`，不存在则调用微信API获取
2. 调用微信API创建二维码，获取 `ticket`
3. 返回 `ticket`，客户端可用于生成二维码图片

**二维码生成URL格式**:
```
https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket={ticket}
```

---

### 3.2 检查登录状态

**接口路径**: `GET /api/v1/login/check_login`

**功能描述**: 轮询检查微信扫码登录状态

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ticket | String | 是 | 二维码ticket |

**请求示例**:
```
GET /api/v1/login/check_login?ticket=gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2aFo2Z3mTdSNDhXVFI5bkpWcjB6AAIE2ahZWAUEsAcAenQd
```

**响应示例**:

登录成功：
```json
{
    "code": "0000",
    "info": "调用成功",
    "data": "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o"
}
```

未登录：
```json
{
    "code": "0003",
    "info": "未登录",
    "data": null
}
```

**业务逻辑**:
1. 从缓存中查找 `ticket` 对应的 `openid`
2. 如果找到，返回 `openid`（登录成功）
3. 如果未找到，返回未登录状态

**使用场景**: 客户端需要轮询此接口，直到获取到 `openid` 为止

---

## 四、微信公众号回调接口

### 4.1 微信服务器验证

**接口路径**: `GET /api/v1/weixin/portal/receive`

**功能描述**: 微信服务器验证接口（首次配置公众号时调用）

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| signature | String | 是 | 微信加密签名 |
| timestamp | String | 是 | 时间戳 |
| nonce | String | 是 | 随机数 |
| echostr | String | 是 | 随机字符串 |

**响应**: 直接返回 `echostr` 字符串

**业务逻辑**:
1. 验证微信签名
2. 签名通过，返回 `echostr`，完成服务器验证

---

### 4.2 接收微信消息

**接口路径**: `POST /api/v1/weixin/portal/receive`

**功能描述**: 接收微信公众号推送的消息（扫码事件、文本消息等）

**请求头**:
```
Content-Type: application/xml
```

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| signature | String | 是 | 微信加密签名 |
| timestamp | String | 是 | 时间戳 |
| nonce | String | 是 | 随机数 |
| openid | String | 是 | 用户openid |
| msg_signature | String | 否 | 消息签名（加密模式） |
| encrypt_type | String | 否 | 加密类型 |

**请求体**: XML格式

扫码事件示例：
```xml
<xml>
    <ToUserName><![CDATA[gh_e067c267e056]]></ToUserName>
    <FromUserName><![CDATA[oUpF8uMuAJO_M2pxb1Q9zNjWeS6o]]></FromUserName>
    <CreateTime>1234567890</CreateTime>
    <MsgType><![CDATA[event]]></MsgType>
    <Event><![CDATA[SCAN]]></Event>
    <Ticket><![CDATA[gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2aFo2Z3mTdSNDhXVFI5bkpWcjB6AAIE2ahZWAUEsAcAenQd]]></Ticket>
</xml>
```

**响应**: XML格式

```xml
<xml>
    <ToUserName><![CDATA[oUpF8uMuAJO_M2pxb1Q9zNjWeS6o]]></ToUserName>
    <FromUserName><![CDATA[gh_e067c267e056]]></FromUserName>
    <CreateTime>1234567890</CreateTime>
    <MsgType><![CDATA[text]]></MsgType>
    <Content><![CDATA[登录成功]]></Content>
</xml>
```

**业务逻辑**:
1. 解析微信推送的XML消息
2. 如果是扫码事件（`event=SCAN`）：
   - 保存登录状态：`ticket` -> `openid` 映射
   - 发送模板消息通知用户登录成功
   - 返回文本消息："登录成功"
3. 如果是文本消息：
   - 返回回复消息："你好，{消息内容}"

---

## 五、数据结构定义

### 5.1 统一响应结构

```java
public class Response<T> {
    private String code;      // 响应码
    private String info;      // 响应信息
    private T data;          // 响应数据（泛型）
}
```

**JSON示例**:
```json
{
    "code": "0000",
    "info": "调用成功",
    "data": { ... }
}
```

---

### 5.2 请求对象

#### CreatePayRequestDTO
```java
public class CreatePayRequestDTO {
    private String userId;      // 用户ID
    private String productId;   // 商品ID
}
```

#### ShopCartReq
```java
public class ShopCartReq {
    private String userId;      // 用户ID
    private String productId;   // 商品ID
}
```

---

### 5.3 响应对象

#### PayOrderRes
```java
public class PayOrderRes {
    private String userId;              // 用户ID
    private String orderId;             // 订单ID
    private String payUrl;              // 支付链接
    private OrderStatusEnum orderStatusEnum; // 订单状态枚举
}
```

#### WeixinQrCodeRes
```java
public class WeixinQrCodeRes {
    private String ticket;       // 二维码ticket
    private Integer expire_seconds; // 过期时间（秒）
    private String url;          // 二维码图片URL
}
```

#### WeixinTokenRes
```java
public class WeixinTokenRes {
    private String access_token;  // 访问令牌
    private Integer expires_in;   // 过期时间（秒）
}
```

---

### 5.4 领域对象

#### PayOrder (PO - 持久化对象)
```java
public class PayOrder {
    private Long id;                    // 自增ID
    private String userId;              // 用户ID
    private String productId;           // 商品ID
    private String productName;         // 商品名称
    private String orderId;              // 订单ID
    private Date orderTime;              // 下单时间
    private BigDecimal totalAmount;      // 订单金额
    private String status;               // 订单状态
    private String payUrl;               // 支付链接
    private Date payTime;                // 支付时间
    private Date createTime;             // 创建时间
    private Date updateTime;             // 更新时间
}
```

#### ProductVO (VO - 值对象)
```java
public class ProductVO {
    private String productId;      // 商品ID
    private String productName;    // 商品名称
    private String productDesc;    // 商品描述
    private BigDecimal price;      // 商品价格
}
```

---

## 六、接口调用示例

### 6.1 创建支付订单示例

**cURL命令**:
```bash
curl -X POST http://localhost:8080/api/v1/alipay/create_pay_order \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "10001",
    "productId": "100001"
  }'
```

**Java代码**:
```java
RestTemplate restTemplate = new RestTemplate();
CreatePayRequestDTO request = new CreatePayRequestDTO();
request.setUserId("10001");
request.setProductId("100001");

Response<String> response = restTemplate.postForObject(
    "http://localhost:8080/api/v1/alipay/create_pay_order",
    request,
    Response.class
);
```

---

### 6.2 微信登录示例

**1. 生成二维码**
```bash
curl http://localhost:8080/api/v1/login/weixin_qrcode_ticket
```

**2. 轮询检查登录状态**
```bash
curl "http://localhost:8080/api/v1/login/check_login?ticket=xxx"
```

---

## 七、接口安全说明

### 7.1 支付宝回调安全

- **签名验证**: 使用RSA256算法验证支付宝回调签名
- **状态验证**: 验证交易状态为 `TRADE_SUCCESS`
- **幂等性**: 订单状态已更新时，重复回调不会重复处理

### 7.2 微信接口安全

- **签名验证**: 验证微信服务器签名
- **Token缓存**: AccessToken 使用 Guava Cache 缓存，减少API调用

### 7.3 跨域支持

所有接口使用 `@CrossOrigin("*")` 支持跨域访问（开发环境），生产环境建议配置具体域名。

---

## 八、错误处理

### 8.1 统一异常处理

所有接口使用统一的 `Response` 封装响应：
- 成功：`code="0000"`
- 失败：`code="0001"`，`info` 包含错误信息

### 8.2 常见错误场景

| 错误码 | 场景 | 处理建议 |
|--------|------|----------|
| 0001 | 系统异常 | 检查日志，排查业务逻辑 |
| 0002 | 参数非法 | 检查请求参数格式和必填项 |
| 0003 | 未登录 | 重新登录获取token |

---

## 九、接口版本说明

当前版本：`v1`

如需升级接口，建议：
- 保持旧版本接口兼容
- 新增 `v2` 版本接口
- 通过路径前缀区分版本：`/api/v1/`、`/api/v2/`

---

## 十、接口测试建议

### 10.1 测试环境

- 支付宝使用沙箱环境（`https://openapi-sandbox.dl.alipaydev.com`）
- 微信使用测试号或正式公众号

### 10.2 测试工具

- **Postman**: 测试 REST API
- **cURL**: 命令行测试
- **支付宝沙箱**: 测试支付流程
- **微信开发者工具**: 测试微信功能

### 10.3 测试流程

1. **支付流程测试**:
   - 创建订单 → 获取支付链接 → 跳转支付 → 查看回调 → 验证订单状态

2. **登录流程测试**:
   - 生成二维码 → 扫码 → 检查登录状态 → 验证token

