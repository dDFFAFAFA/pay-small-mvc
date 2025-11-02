package cn.bugstack.test.service;

import cn.bugstack.service.ILoginService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2024-11-03 12:32
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class LoginServiceTest {

    @Resource
    private ILoginService loginService;

    @Test
    public void test() throws IOException {
        // openid，你要让谁接收到模板消息
        // ticket，测试时候可以随意发
        loginService.saveLoginState("111", "111");
    }

    @Test
    public void test_createQrCodeTicket() throws Exception {
        log.info("qrCodeTicket:{}", loginService.createQrCodeTicket());
        log.info("qrCodeTicket:{}", loginService.createQrCodeTicket());
        log.info("qrCodeTicket:{}", loginService.createQrCodeTicket());
        // gQGe7zwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyMnFNbW9KTDBjckcxc2hQdmhEMWQAAgQRZjhnAwQAjScA
        // gQHv7zwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyWkZETG81TDBjckcxc0dQdk5EMXcAAgQqZjhnAwQAjScA
        // gQHG7zwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyNGJCRG9STDBjckcxc0ZQdnhEMW8AAgQpZjhnAwQAjScA
        // gQHY7zwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyZzl0Vm9UTDBjckcxc0ZQdk5EMTcAAgQpZjhnAwQAjScA
    }

}
