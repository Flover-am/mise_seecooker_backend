package com.seecooker.controller.recipe;

import cn.dev33.satoken.stp.StpUtil;
import com.aliyuncs.exceptions.ClientException;
import com.seecooker.pojo.vo.recipe.PublishRecipeVO;
import com.seecooker.pojo.vo.recipe.RecipeDetailVO;
import com.seecooker.pojo.vo.recipe.RecipeListVO;

import com.seecooker.common.core.Result;
import com.seecooker.common.core.exception.BizException;
import com.seecooker.common.core.exception.ErrorType;
import com.seecooker.service.RecipeService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 菜谱控制层类
 *
 * @author xueruichen
 * @date 2023.11.27
 */
@Slf4j
@RestController
@RequestMapping("/v1/")
public class RecipeController {
    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * 发布菜谱
     *
     * @param publishRecipe 发布菜谱VO类
     * @param cover 菜谱封面图
     * @param stepImages 菜谱步骤图
     * @return 响应结果
     */
    @PostMapping("recipe")
    public Result<Long> publishRecipe(PublishRecipeVO publishRecipe, MultipartFile cover, MultipartFile[] stepImages) throws IOException, ClientException {
        // 未登陆不能发布菜谱
        StpUtil.checkLogin();
        if (stepImages == null) {
            throw new BizException(ErrorType.ILLEGAL_ARGUMENTS, "步骤图不能为空");
        }
        // 检查图片数量是否与步骤数量相等
        if (publishRecipe.getStepContents().size() != stepImages.length) {
            throw new BizException(ErrorType.RECIPE_STEP_MATCH_ERROR);
        }

        if (publishRecipe.getIngredients().size() != publishRecipe.getAmounts().size()) {
            throw new BizException(ErrorType.ILLEGAL_ARGUMENTS, "配料与量不匹配");
        }
        Long id = recipeService.addRecipe(publishRecipe, cover, stepImages);
        return Result.success(id);
    }

    /**
     * 获取菜谱列表
     *
     * @return 响应结果
     */
    @GetMapping("recipes")
    public Result<List<RecipeListVO>> getRecipes() {
        List<RecipeListVO> recipes = recipeService.getRecipes();
        return Result.success(recipes);
    }

    /**
     * 根据菜谱id获取菜谱细节
     *
     * @param recipeId 菜谱id
     * @return 菜谱细节VO类
     */
    @GetMapping("recipe/{recipeId}")
    public Result<RecipeDetailVO> getRecipeDetail(@PathVariable @NotNull Long recipeId) {
        RecipeDetailVO recipeDetail = recipeService.getRecipeDetailById(recipeId);
        return Result.success(recipeDetail);
    }

    /**
     * 搜索菜谱
     *
     * @param query 搜索关键词
     * @return 响应结果
     */
    @GetMapping("recipes/search")
    public Result<List<RecipeListVO>> searchRecipes(@RequestParam @NotNull String query) {
        List<RecipeListVO> recipes = recipeService.getRecipesByNameLike(query);
        return Result.success(recipes);
    }

    /**
     * 收藏或取消收藏菜谱
     *
     * @param recipeId 菜谱id
     * @return 当前用户是否收藏菜谱
     */
    @PutMapping("recipe/favorite/{recipeId}")
    public Result<Boolean> favoriteRecipe(@PathVariable @NotNull Long recipeId) {
        // 检查是否登陆，未登陆不能收藏
        StpUtil.checkLogin();
        Boolean result = recipeService.favoriteRecipe(recipeId);
        return Result.success(result);
    }

    /**
     * 菜谱评分
     *
     * @param recipeId 菜谱id
     * @param score 评分
     * @return 当前均分
     */
    @PostMapping("recipe/score")
    public Result<Double> scoreRecipe(Long recipeId, Double score) {
        StpUtil.checkLogin();
        double result = recipeService.scoreRecipe(recipeId, score);
        return Result.success(result);
    }
}
