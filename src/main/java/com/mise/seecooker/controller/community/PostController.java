package com.mise.seecooker.controller.community;

import cn.dev33.satoken.stp.StpUtil;
import com.mise.seecooker.entity.Result;
import com.mise.seecooker.entity.vo.community.PostDetailVO;
import com.mise.seecooker.entity.vo.community.PostVO;
import com.mise.seecooker.service.PostService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 帖子相关业务控制层类
 *
 * @author xueruichen
 * @date 2023.11.25
 */
@Slf4j
@RestController
@RequestMapping("/v1")
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 发布帖子
     *
     * @return 响应结果
     */
    @PostMapping("/post")
    public Result<Long> publishPost(@NotNull String title, @NotNull String content, MultipartFile[] images) throws Exception{
        // 未登录不能发布帖子
        StpUtil.checkLogin();
        Long postId = postService.addPost(title, content, images);
        return Result.success(postId);
    }

    /**
     * 获取帖子，每次至多获取10条
     *
     * @return 响应结果
     */
    @GetMapping("/posts")
    public Result<List<PostVO>> getPosts() {
        // TODO: 目前为直接获取所有帖子，后续迭代中修改为获取分页推荐10条帖子
        List<PostVO> posts = postService.getPosts();
        return Result.success(posts);
    }

    /**
     * 获取帖子详情
     *
     * @param id 帖子id
     * @return 响应结果
     */
    @GetMapping("/post/{id}")
    public Result<PostDetailVO> getPostDetail(@PathVariable @NotNull Long id) {
        PostDetailVO post = postService.getPostDetail(id);
        return Result.success(post);
    }

}
