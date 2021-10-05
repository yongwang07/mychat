package test;

import com.mychat.entity.LoginBack;
import com.mychat.imClient.feignClient.UserAction;
import com.mychat.imClient.feignClient.WebOperator;
import com.mychat.util.JsonUtil;
import feign.Feign;
import feign.codec.StringDecoder;
import org.junit.Test;

/**
 * 远程API的本地调用
 * Created by 尼恩 at 疯狂创客圈
 */

public class LoginActionTest
{
    /**
     * 测试登录
     */
    @Test
    public void testLogin()
    {

        UserAction action = Feign.builder()
//                .decoder(new GsonDecoder())
                .decoder(new StringDecoder())
                .target(UserAction.class, "http://localhost:8080/user");

        String s = action.loginAction("zhangsan", "zhangsan");

        LoginBack back = JsonUtil.jsonToPojo(s, LoginBack.class);
        System.out.println("s = " + s);

    }

    /**
     * 测试登录
     */
    @Test
    public void testLogin2()
    {

        LoginBack back = WebOperator.login("lisi", "lisi");
        System.out.println("s = " + "");

    }


    /**
     * 测试获取用户信息
     */
    @Test
    public void testGetById()
    {

        UserAction action = Feign.builder()
//                .decoder(new GsonDecoder())
                .decoder(new StringDecoder())
                .target(UserAction.class, "http://localhost:8080/user");

        String s = action.getById(2);
        System.out.println("s = " + s);

    }
}
