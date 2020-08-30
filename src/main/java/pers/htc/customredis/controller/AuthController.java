package pers.htc.customredis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pers.htc.customredis.model.User;
import pers.htc.customredis.service.AuthService;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/menu")
    public User getMenu(@RequestParam("name") String name,
                        @RequestParam("email") String email) {
        System.out.println(name);
        User u = new User();
        u.setName("hhh");
        System.out.println("pre---");
        User menu = authService.getMenu(name, email, u);
        System.out.println("menu---");
        return menu;
    }

    @GetMapping("/list")
    public List<User> getList(String me) {
        System.out.println("list pre");
        List<User> list = authService.getList(me);
        System.out.println("list done:" + list);
        return list;
    }
}
