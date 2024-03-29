package com.seecooker.user.service.service.impl;


import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.seecooker.common.core.enums.ImageType;
import com.seecooker.common.core.exception.BizException;
import com.seecooker.common.core.exception.ErrorType;
import com.seecooker.user.service.dao.UserDao;
import com.seecooker.user.service.pojo.po.UserPO;
import com.seecooker.user.service.pojo.vo.UserInfoVO;
import com.seecooker.user.service.service.UserService;
import com.seecooker.util.oss.AliOSSUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;


/**
 * 用户业务服务层实现
 *
 * @author xueruichen
 * @date 2023.11.17
 */
@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private static final String DEFAULT_AVATAR = "https://dummyimage.com/100x100";
    private final RabbitTemplate rabbitTemplate;
    public UserServiceImpl(UserDao userDao, RabbitTemplate rabbitTemplate) {
        this.userDao = userDao;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Long addUser(String username, String password, String avatar) {
        UserPO user = userDao.findByUsername(username);
        // 用户已存在，抛出异常
        if (user != null) {
            log.error("The user already exists");
            throw new BizException(ErrorType.USER_ALREADY_EXIST);
        }
        user = userDao.save(UserPO.builder()
                        .username(username)
                        .password(BCrypt.hashpw(password))
                        .avatar(avatar == null ? DEFAULT_AVATAR : avatar)
                        .posts(Collections.emptyList())
                        .postRecipes(Collections.emptyList())
                        .favoriteRecipes(Collections.emptyList())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build());
        return user.getId();
    }

    @Override
    public void login(String username, String password) {
        UserPO user = userDao.findByUsername(username);
        // 用户名不存在
        if (user == null) {
            log.error("The username does not exist");
            throw new BizException(ErrorType.USER_NOT_EXIST);
        }
        // 密码错误
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BizException(ErrorType.PASSWORD_ERROR);
        }
        StpUtil.login(user.getId());
    }

    @Override
    public UserInfoVO getUserInfoById(Long id) {
        return getUserInfoVOById(id);
    }

    @Override
    public void logout() {
        // 检查用户是否登陆
        StpUtil.checkLogin();
        // 若已登陆，则登出
        StpUtil.logout();
    }

    @Override
    public UserInfoVO getCurrentLoginUser() {
        return getUserInfoById(getCurrentLoginUserId());
    }

    @Override
    public String uploadAvatar(MultipartFile avatar) throws Exception {
        if (avatar == null) {
            return null;
        }
        return AliOSSUtil.uploadFile(avatar, ImageType.AVATAR);
    }

    private UserInfoVO getUserInfoVOById(Long id) {
        Optional<UserPO> user = userDao.findById(id);
        if (user.isEmpty()) {
            log.error("The user dos not exist");
            throw new BizException(ErrorType.USER_NOT_EXIST);
        }
        return UserInfoVO.builder()
                .username(user.get().getUsername())
                .avatar(user.get().getAvatar())
                .postNum(user.get().getPosts().size())
                .getLikedNum(0)
                .signature(user.get().getSignature())
                .build();
    }

    @Override
    public Long getCurrentLoginUserId() {
        return StpUtil.getLoginIdAsLong();
    }
    @Override
    public void modifyUsername(String username,String newUsername){
        // 检查空值
        if(username==null|| username.isEmpty()){
            throw new BizException(ErrorType.ILLEGAL_ARGUMENTS, "用户名不能为空");
        }
        if(newUsername==null||newUsername.isEmpty()){
            throw new BizException(ErrorType.ILLEGAL_ARGUMENTS, "新用户名不能为空");
        }
        UserPO user = userDao.findByUsername(newUsername);
        if (user != null) {
            throw new BizException(ErrorType.ILLEGAL_ARGUMENTS, "该用户名已存在");
        }
        // 检查相同值
        if(username.equals(newUsername)){
            throw new BizException(ErrorType.ILLEGAL_ARGUMENTS, "新用户名不能与原用户名相同");
        }
        user = userDao.findByUsername(username);
        // 用户名不存在
        if (user == null) {
            log.error("The username does not exist");
            throw new BizException(ErrorType.USER_NOT_EXIST);
        }

        user.setUsername(newUsername);
        userDao.save(user);
    }
    @Override
    public void modifyPassword(String username,String password,String newPassword){
        UserPO user = userDao.findByUsername(username);
        // 用户名不存在，抛出异常
        if (user == null) {
            log.error("The username does not exist");
            throw new BizException(ErrorType.USER_NOT_EXIST);
        }
        // 密码错误，抛出异常
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BizException(ErrorType.PASSWORD_ERROR);
        }
        user.setPassword(BCrypt.hashpw(newPassword));
        userDao.save(user);
    }
    @Override
    public void modifyAvatar(String username,String avatar){
        UserPO user = userDao.findByUsername(username);
        // 用户名不存在，抛出异常
        if (user == null) {
            log.error("The username does not exist");
            throw new BizException(ErrorType.USER_NOT_EXIST);
        }
        if(avatar==null||avatar.isEmpty())avatar=null;
        user.setAvatar(avatar);
        userDao.save(user);
    }

    @Override
    public void modifySignature(String signature) {
        Long userId = StpUtil.getLoginIdAsLong();
        rabbitTemplate.convertAndSend("modifySignature", userId+":"+signature);
    }
}
