package edu.bupt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.bupt.domain.History;

import java.util.List;

/**
 * 定义用户历史记录有关业务操作的接口
 */
public interface IHistoryService extends IService<History> {
    /**
     * 根据用户名获取多个历史记录
     *
     * @param username 用户名
     * @return {@code List<History>} 历史记录
     */
    List<History> getHistory(String username);

    /**
     * 获取指定用户指定索引位置的历史记录
     *
     * @param username 用户名
     * @param index    索引
     * @return {@link History} 历史记录
     */
    History getHistoryAtIndex(String username, int index);
}
