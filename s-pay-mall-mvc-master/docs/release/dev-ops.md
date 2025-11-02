# 部署脚本

## 一、前置条件

- linux jdk1.8、maven 3.8.x
- 域名 自行申请和配置。[https://console.bce.baidu.com/dns/#/dns/domain/list?zoneName=xiaofuge.tech](https://console.bce.baidu.com/dns/#/dns/domain/list?zoneName=xiaofuge.tech)
- 云服务器 2c4g https://618.gaga.plus - 1M带宽即可，便宜。
- ssl1 - 京东云（遇到问题，提工单找客服）；https://certificate-console.jdcloud.com/jsecssl/create?fastConfig=false&certBrand=TrustAsia&certType=domainType&protectionType=DV-1&gDomainCount=0
- ssl2 - 第三方；https://bugstack.cn/md/road-map/ssl-httpsok.html

## 二、执行脚本

```java
git clone -b docker-images-v1.0 https://gitcode.net/KnowledgePlanet/s-pay-mall/s-pay-mall-mvc.git

mvn clean install

chmod +x build.sh

docker-compose -f docker-compose-environment.yml up -d

docker-compose -f docker-compose-app.yml up -d
```

## 三、配置地址

- 微信验签：[https://mp.weixin.qq.com/debug/cgi-bin/sandboxinfo?action=showinfo&t=sandbox/index](https://mp.weixin.qq.com/debug/cgi-bin/sandboxinfo?action=showinfo&t=sandbox/index)