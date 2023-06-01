package edu.bupt.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.bupt.domain.History;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 实现了历史记录查询的接口
 */
@Mapper
public interface HistoryDao extends BaseMapper<History> {
    /**
     * 根据文件md5在数据库中查询对应的文件名
     *
     * @param md5 文件md5
     * @return md5对应的文件名
     */
    @Select("select file_name from tbl_user_history where file_name_md5=#{md5}")
    String getFileNameByMd5(String md5);

    /**
     * 根据<code>id,limit,offset</code>获取指定的文件的所有信息
     *
     * @param userId 用户id
     * @param limit  sql语句中的limit值
     * @param offset sql语句中的offset值
     * @return 文件的所有数据，封装在<code>History</code>类中
     * @see History
     */
    @Select("select * from tbl_user_history where applicant_id=#{id} limit #{limit} offset #{off}")
    History getFileNameByIdLimitOff(@Param("id") int userId, @Param("limit") int limit, @Param("off") int offset);
}
