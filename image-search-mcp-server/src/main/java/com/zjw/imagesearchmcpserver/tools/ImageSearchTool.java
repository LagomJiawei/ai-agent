package com.zjw.imagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 图片搜索 MCP 服务
 *
 * @author ZhangJw
 * @date 2026年05月10日 7:10
 */
@Service
public class ImageSearchTool {

    // 替换为你的 Pexels API 密钥（需从官网申请）
    private static final String API_KEY = "你的 API Key";

    // Pexels 常规搜索接口（以文档为准）
    private static final String API_URL = "https://api.pexels.com/v1/search";

    // HTTP请求超时时间（毫秒）
    private static final int HTTP_TIMEOUT = 10000; // 10秒
    // 每页默认图片数量
    private static final int DEFAULT_PER_PAGE = 10;
    // 每页最大图片数量
    private static final int MAX_PER_PAGE = 30;


    /**
     * 分页搜索图片
     *
     * @param query 搜索关键词
     * @param perPage 每页数量
     * @param page 页码
     * @return 图片URL列表
     */
    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query,
                              @ToolParam(description = "Number of images to return (default: 10, max: 30)", required = false) Integer perPage,
                              @ToolParam(description = "Page number for pagination (default: 1)", required = false) Integer page) {
        try {
            int validPerPage = this.validatePerPage(perPage);
            int validPage = this.validatePage(page);
            List<String> images = this.searchMediumImages(query, validPerPage, validPage);
            return String.join(",", images);
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    /**
     * 异步搜索图片
     *
     * @param query 搜索关键词
     * @param perPage 每页数量
     * @param page 页码
     * @return 图片URL列表
     */
    @Tool(description = "search image from web asynchronously")
    public CompletableFuture<String> searchImageAsync(
            @ToolParam(description = "Search query keyword") String query,
            @ToolParam(description = "Number of images to return (default: 10, max: 30)", required = false) Integer perPage,
            @ToolParam(description = "Page number for pagination (default: 1)", required = false) Integer page) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                int validPerPage = this.validatePerPage(perPage);
                int validPage = this.validatePage(page);
                List<String> images = this.searchMediumImages(query, validPerPage, validPage);
                return String.join(",", images);
            } catch (Exception e) {
                return "Error search image: " + e.getMessage();
            }
        });
    }

    /**
     * 验证每页数量参数
     *
     * @param perPage 原始每页数量
     * @return 有效的每页数量
     */
    private int validatePerPage(Integer perPage) {
        if (perPage == null || perPage <= 0) {
            return DEFAULT_PER_PAGE;
        }
        return Math.min(perPage, MAX_PER_PAGE);
    }

    /**
     * 验证页码参数
     *
     * @param page 原始页码
     * @return 有效的页码
     */
    private int validatePage(Integer page) {
        if (page == null || page <= 0) {
            return 1;
        }
        return page;
    }

    /**
     * 搜索中等尺寸的图片列表（带分页支持）
     *
     * @param query
     * @return
     */
    public List<String> searchMediumImages(String query, int perPage, int page) {
        // 设置请求头（包含API密钥）
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);

        // 设置请求参数（包含query，分页参数）
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("per_page", perPage);
        params.put("page", page);

        // 发送 GET 请求
        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .timeout(HTTP_TIMEOUT) // 设置HTTP请求超时时间
                .execute()
                .body();

        // 解析响应JSON（假设响应结构包含"photos"数组，每个photo包含"medium"字段）
        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    /**
     * 搜索中等尺寸的图片列表（默认分页参数）
     *
     * @param query 搜索关键词
     * @return 图片URL列表
     */
    public List<String> searchMediumImages(String query) {
        return searchMediumImages(query, DEFAULT_PER_PAGE, 1);
    }
}
