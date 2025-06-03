package com.koko.mywiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.koko.mywiki.domain.User;
import com.koko.mywiki.domain.UserExample;
import com.koko.mywiki.exception.BusinessException;
import com.koko.mywiki.exception.BusinessExceptionCode;
import com.koko.mywiki.mapper.UserMapper;
import com.koko.mywiki.req.UserQueryReq;
import com.koko.mywiki.req.UserSaveReq;
import com.koko.mywiki.resp.PageResp;
import com.koko.mywiki.resp.UserQueryResp;
import com.koko.mywiki.until.CopyUtil;
import com.koko.mywiki.until.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Resource
    private UserMapper userMapper;

    @Resource
    private SnowFlake snowFlake;

    public PageResp<UserQueryResp> list(UserQueryReq req) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        if(!ObjectUtils.isEmpty(req.getLoginName())) {
            criteria.andLoginNameEqualTo(req.getLoginName());
        }
        PageHelper.startPage(req.getPage(), req.getSize() );
        List<User> userList = userMapper.selectByExample(userExample);

        PageInfo<User> pageinfo = new PageInfo<>(userList);
        LOG.info("总行数：{}" ,pageinfo.getTotal());
        LOG.info("总列数：{}", pageinfo.getPages());

        List<UserQueryResp> respList = new ArrayList<>();
        for (User user : userList) {
//            UserQueryResp userQueryResp = new UserQueryResp();
//            BeanUtils.copyProperties(user, userQueryResp);

            UserQueryResp userQueryResp = CopyUtil.copy(user, UserQueryResp.class);
            respList.add(userQueryResp);
        }

        List<UserQueryResp> list = CopyUtil.copyList(userList, UserQueryResp.class);
        PageResp<UserQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageinfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    /*
    * 保存
    * */
    public void save(UserSaveReq req) {
        User user = CopyUtil.copy(req, User.class);
        if (ObjectUtils.isEmpty(user.getId())) {
            User userDB = selectByLoginName(req.getLoginName());
            if(ObjectUtils.isEmpty(userDB)) {
                // 新增
                user.setId(snowFlake.nextId());
                userMapper.insert(user);
            }else {
                // 用户名已存在
                throw new BusinessException(BusinessExceptionCode.USER_LOGIN_NAME_EXIST);

            }
        }else {
            // 更新
            user.setLoginName(null);
            userMapper.updateByPrimaryKeySelective(user);
        }
    }

    /*
     * 删除
     * */
    public void delete(Long id) {
        userMapper.deleteByPrimaryKey(id);
    }

    public User selectByLoginName(String LoginName) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andLoginNameEqualTo(LoginName);
        List<User> userList = userMapper.selectByExample(userExample);
        if (ObjectUtils.isEmpty(userList)) {
            return null;
        }else {
            return userList.get(0);
        }
    }
}
