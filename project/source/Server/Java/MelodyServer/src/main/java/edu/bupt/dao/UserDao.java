package edu.bupt.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.bupt.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 实现了用户信息查询的接口
 */
@Mapper
public interface UserDao extends BaseMapper<User> {
    /**
     * 向数据库中添加一个用户记录
     *
     * @param username the username
     * @param password the password
     * @param salt     the salt
     * @return 操作影响数据库的行数
     */
    @Insert("insert into tbl_user (id, username, password, salt) VALUES (null,#{username},#{password},#{salt})")
    int add(@Param("username") String username, @Param("password") String password, @Param("salt") String salt);

    /**
     * 通过username获取<code>id,username,password,salt</code>
     *
     * @param username the username
     * @return 封装在Map&lt;String, Object&gt;中的<code>id,username,password,salt</code>值
     */
    @Select("select id,username,password,salt from tbl_user where username=#{username}")
    Map<String, Object> getByUsername(String username);

    /**
     * 根据用户名获取<code>id</code>。
     *
     * @param username the username
     * @return the id by username
     */
    @Select("select id from tbl_user where username=#{username}")
    Integer getIdByUsername(String username);
}
