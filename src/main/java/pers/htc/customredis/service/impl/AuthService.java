package pers.htc.customredis.service.impl;

import org.springframework.stereotype.Service;
import pers.htc.customredis.annotation.CustomRedis;
import pers.htc.customredis.model.User;
import pers.htc.customredis.service.IAuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService implements IAuthService {

    private final String PREFIX = "0test:menu:";

    @CustomRedis(key = PREFIX + "{#name}:{#email}:{#u.name}",
            expireTme = 1,
            expireTimeUnit = TimeUnit.DAYS)
    public User getMenu(String name,
                        String email,
                        User u) {
        System.out.println("远程getMenu");
        User user = new User();
        user.setName(name);
        user.setAge(17);
        return user;
    }

    @CustomRedis(key = "0test:{#me}",
            expireTme = 1, expireTimeUnit = TimeUnit.HOURS,
            extendTime = 1, extendTimeUnit = TimeUnit.HOURS)
    public List<User> getList(String me) {
        System.out.println("远程调用...");
        List<User> list = new ArrayList<>();
        User u1 = new User();
        u1.setName(me + "1");
        list.add(u1);
        User u2 = new User();
        u2.setName(me + "2");
        list.add(u2);
        return list;
    }

    @CustomRedis(key = "void")
    public void doSth() {

    }
}
