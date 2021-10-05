import com.mychat.im.common.bean.UserDTO;
import com.mychat.imClient.ClientSender.ChatSender;
import com.mychat.imClient.client.ClientSession;
import org.junit.Test;

/**
 * Created by 尼恩 at 疯狂创客圈
 */

public class client
{

    @Test
    public void sendChatMsg()
    {
        ChatSender sender = new ChatSender();
        UserDTO user = new UserDTO();
        user.setUserId("1");
        user.setNickName("张三");
        user.setSessionId("-1");
        sender.setSession(new ClientSession(null));
        sender.setUser(user);
        sender.sendChatMsg("dd", "1");

        try
        {
            Thread.sleep(1000000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
