package pers.htc.customredis.service;

import pers.htc.customredis.model.User;

import java.util.List;

public interface IAuthService {

    User getMenu(String name, String email, User u);

    List<User> getList(String me);

    void doSth();
}
