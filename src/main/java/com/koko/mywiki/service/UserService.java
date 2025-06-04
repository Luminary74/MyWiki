package com.koko.mywiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.koko.mywiki.domain.User;
import com.koko.mywiki.domain.UserExample;
import com.koko.mywiki.exception.BusinessException;
import com.koko.mywiki.exception.BusinessExceptionCode;
import com.koko.mywiki.mapper.UserMapper;
import com.koko.mywiki.req.UserLoginReq;
import com.koko.mywiki.req.UserQueryReq;
import com.koko.mywiki.req.UserRestPasswordeReq;
import com.koko.mywiki.req.UserSaveReq;
import com.koko.mywiki.resp.PageResp;
import com.koko.mywiki.resp.UserLoginResp;
import com.koko.mywiki.resp.UserQueryResp;
import com.koko.mywiki.util.CopyUtil;
import com.koko.mywiki.util.SnowFlake;
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
            user.setPassword(null);
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

    /*
     * 修改密码
     * */
    public void resetPassword(UserRestPasswordeReq req) {
        User user = CopyUtil.copy(req, User.class);
        userMapper.updateByPrimaryKeySelective(user);
    }

    /*
     * 登录
     * */
    public UserLoginResp login(UserLoginReq req) {
        User userDb = selectByLoginName(req.getLoginName());
        if(ObjectUtils.isEmpty(userDb)) {
            //用户名不存在
            LOG.info("用户名不存在,{}",req.getLoginName());
            throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
        }else {
            if (userDb.getPassword().equals(req.getPassword())) {
                //登录成功
                UserLoginResp userLoginResp = CopyUtil.copy(userDb, UserLoginResp.class);
                return userLoginResp;
            }else{
                //密码不对
                LOG.info("密码不对，输入密码：{}，数据库密码：{}",req.getPassword(),userDb.getPassword());
                throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
            }
        }
    }
}
