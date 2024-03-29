package com.seecooker.recipe.service.pojo.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 菜谱预览VO类
 *
 * @author xueruichen
 * @date 2023.11.27
 */
@Getter
@Setter
@Builder
public class RecipeListVO {
    /**
     * 菜谱id
     */
    private Long recipeId;

    /**
     * 菜谱名
     */
    private String name;

    /**
     * 菜谱封面url
     */
    private String cover;

    /**
     * 作者id
     */
    private Long authorId;

    /**
     * 作者名
     */
    private String authorName;

    /**
     * 作者头像
     */
    private String authorAvatar;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 评分
     */
    private Double score;

    /**
     * 发布时间
     */
    private String publishTime;

    /**
     * 是否收藏，未登陆默认False
     */
    private Boolean favorite;

    /**
     * 收藏数
     */
    private Integer favoriteNum;
}
