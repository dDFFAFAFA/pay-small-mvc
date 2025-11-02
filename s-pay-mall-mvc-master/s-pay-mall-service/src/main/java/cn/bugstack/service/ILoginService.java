package cn.bugstack.service;

import java.io.IOException;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 微信服务
 * @create 2024-09-28 13:44
 */
public interface ILoginService {

    String createQrCodeTicket() throws Exception;

    String checkLogin(String ticket);

    void saveLoginState(String ticket, String openid) throws IOException;

}
