import cn.hutool.core.date.DateUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.xclhove.common.model.User;
import com.xclhove.common.service.UserService;
import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.config.RpcConfig;
import com.xclhove.rpc.proxy.ServiceProxyFactory;
import com.xclhove.rpc.serializer.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xclhove
 */
@Slf4j
public class Consumer {
    private static final UserService userService = ServiceProxyFactory.getProxy(UserService.class);
    
    public static void main(String[] args) {
        consume();
        CronUtil.schedule("*/5 * * * * *", (Task) Consumer::consume);
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }
    
    private static void consume() {
        User user = userService.getUser(new User().setName("xclhove1"));
        System.out.printf("%s | %s%n",DateUtil.now(), user);
    }
}
