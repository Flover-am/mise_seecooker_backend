package com.mise.seecooker.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.aliyuncs.exceptions.ClientException;
import com.github.javafaker.Faker;
import com.mise.seecooker.dao.RecipeDao;
import com.mise.seecooker.dao.UserDao;
import com.mise.seecooker.entity.po.RecipePO;
import com.mise.seecooker.entity.po.UserPO;
import com.mise.seecooker.entity.vo.recipe.PublishRecipeVO;
import com.mise.seecooker.entity.vo.recipe.RecipeDetailVO;
import com.mise.seecooker.entity.vo.recipe.RecipeVO;
import com.mise.seecooker.service.RecipeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@SpringBootTest
public class RecipeServiceImplTest {
    @Autowired
    private RecipeService recipeService;
    @Autowired
    private RecipeDao recipeDao;
    @Autowired
    private UserDao userDao;

    private final Faker faker = new Faker(Locale.CHINA);

    @BeforeEach
    void registerAndLogin() {
        String username = "testUser222";
        String password = "12345678abc";
        Long id = userDao.save(UserPO.builder()
                .username(username)
                .password(password)
                .posts(List.of())
                .postRecipes(List.of())
                .build()).getId();
        StpUtil.login(id);
    }

    @AfterEach
    void clear() {
        recipeDao.deleteAll();
        StpUtil.logout();
        userDao.deleteAll();
    }

    @Test
    void addRecipeTest() throws IOException, ClientException {
        PublishRecipeVO publishRecipe = PublishRecipeVO.builder()
                .name(faker.app().name())
                .introduction(faker.name().title())
                .stepContents(Collections.emptyList())
                .build();
        Long id = recipeService.addRecipe(publishRecipe, null, new MultipartFile[]{});
        RecipePO recipe = recipeDao.findById(id).get();
        assertEquals(publishRecipe.getName(), recipe.getName());
    }

    @Test
    void getRecipesTest() throws IOException, ClientException {
        PublishRecipeVO publishRecipe1 = PublishRecipeVO.builder()
                .name(faker.app().name())
                .introduction(faker.name().title())
                .stepContents(Collections.emptyList())
                .build();
        PublishRecipeVO publishRecipe2 = PublishRecipeVO.builder()
                .name(faker.app().name())
                .introduction(faker.name().title())
                .stepContents(Collections.emptyList())
                .build();
        // TODO: 此处不应当有recipeService，后续需要mock掉
        Long id1 = recipeService.addRecipe(publishRecipe1, null, new MultipartFile[]{});
        Long id2 = recipeService.addRecipe(publishRecipe2, null, new MultipartFile[]{});
        List<RecipeVO> recipes = recipeService.getRecipes();
        assertEquals(publishRecipe1.getName(), recipes.get(0).getName());
        assertEquals(publishRecipe2.getName(), recipes.get(1).getName());
    }

    @Test
    void getRecipeDetailByIdTest() throws IOException, ClientException {
        PublishRecipeVO publishRecipe = PublishRecipeVO.builder()
                .name(faker.app().name())
                .introduction(faker.name().title())
                .stepContents(Collections.emptyList())
                .build();
        // TODO: 此处不应当有recipeService，后续mock掉
        Long id = recipeService.addRecipe(publishRecipe, null, new MultipartFile[]{});
        RecipeDetailVO recipeDetail = recipeService.getRecipeDetailById(id);
        assertEquals(recipeDetail.getName(), publishRecipe.getName());
        assertEquals(recipeDetail.getIntroduction(), publishRecipe.getIntroduction());
    }

    @Test
    void getRecipesByNameLike() throws IOException, ClientException {
        PublishRecipeVO publishRecipe1 = PublishRecipeVO.builder()
                .name("老八秘制小汉堡")
                .introduction(faker.name().title())
                .stepContents(Collections.emptyList())
                .build();
        PublishRecipeVO publishRecipe2 = PublishRecipeVO.builder()
                .name("铁锅炖大鹅")
                .introduction(faker.name().title())
                .stepContents(Collections.emptyList())
                .build();
        recipeService.addRecipe(publishRecipe1, null, new MultipartFile[]{});
        recipeService.addRecipe(publishRecipe2, null, new MultipartFile[]{});
        List<RecipeVO> recipes = recipeService.getRecipesByNameLike("汉堡");
        assertEquals(1, recipes.size());
        assertEquals("老八秘制小汉堡", recipes.get(0).getName());
        recipes = recipeService.getRecipesByNameLike("大鹅");
        assertEquals(1, recipes.size());
        assertEquals("铁锅炖大鹅", recipes.get(0).getName());
        // 模糊匹配
        recipes = recipeService.getRecipesByNameLike("铁鹅");
        assertEquals(1, recipes.size());
        assertEquals("铁锅炖大鹅", recipes.get(0).getName());
    }

}