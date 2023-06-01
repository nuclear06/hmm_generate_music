package edu.bupt.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.bupt.dao.HistoryDao;
import edu.bupt.dao.UserDao;
import edu.bupt.domain.History;
import edu.bupt.service.IHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.bupt.domain.User;

import java.util.List;

/**
 * The type History service.
 */
@Slf4j
@Service
public class HistoryServiceImpl extends ServiceImpl<HistoryDao, History> implements IHistoryService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private HistoryDao historyDao;

    @Override
    public List<History> getHistory(String username) {
        Integer id = userDao.getIdByUsername(username);
        LambdaQueryWrapper<History> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(id != null, History::getApplicantId, id);
        List<History> list = list(wrapper);
        log.info(username + "查询了history,共" + list.size() + "条.");
        return list;
    }

    @Override
    public History getHistoryAtIndex(String username,int index) {
        Integer id = userDao.getIdByUsername(username);
        return historyDao.getFileNameByIdLimitOff(id, 1, index - 1);
    }
}
