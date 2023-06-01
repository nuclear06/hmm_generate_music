package edu.bupt.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.bupt.dao.UserDao;
import edu.bupt.domain.User;
import edu.bupt.utils.ServerBaseUtils;
import edu.bupt.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Map;

import static edu.bupt.utils.PwdSaltUtils.encryptUserPwd;
import static edu.bupt.utils.PwdSaltUtils.md5Verify;

/**
 * The type User service.
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements IUserService {
    @Autowired
    private ServerBaseUtils serverBaseUtils;
    @Autowired
    private UserDao userDao;

    @Override
    public boolean login(User user) {
        String username = user.getUsername();
        String password = user.getPassword();

        Map<String, Object> map = userDao.getByUsername(username);    //查询盐和密码
        if (map == null) return false;

        String salt = (String) map.get("salt");
        String targetPwd = (String) map.get("password");
        if (md5Verify(password, salt, targetPwd)) {
            log.info(username + "-登陆-" + serverBaseUtils.currentTime("yyyy-MM-dd hh:mm"));
            return true;
        }
        return false;
    }

    @Override
    public boolean register(User user) {
        String username = user.getUsername();
        String password = user.getPassword();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User u = getOne(wrapper);
        if (u != null) return false;  //数据已存在
        try {
            String salt = encryptUserPwd(user);
            int col = userDao.add(username, user.getPassword(), salt);
            boolean flag = col > 0;
            if (flag) log.info(username + "-注册-" + serverBaseUtils.currentTime("yyyy-MM-dd hh:mm"));
            return flag;
        } catch (DuplicateKeyException e) {
            return false;
        } catch (Exception e) {
            log.warn(String.valueOf(e));
            return false;
        }
    }
}
