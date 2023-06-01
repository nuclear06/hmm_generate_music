package edu.bupt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.bupt.domain.User;

/**
 * 定义用户登陆注册等业务的接口
 */
public interface IUserService extends IService<User> {
    /**
     * 登陆,返回登陆是否成功
     *
     * @param user 用户信息
     * @return 登陆是否成功
     * @see User
     */
    boolean login(User user);

    /**
     * 注册,返回值为注册是否成功
     *
     * @param user 用户信息
     * @return 注册是否成功
     * @see User
     */
    boolean register(User user);
}
