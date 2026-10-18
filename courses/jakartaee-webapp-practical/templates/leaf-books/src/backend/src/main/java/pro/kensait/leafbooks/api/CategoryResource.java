package pro.kensait.leafbooks.api;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.leafbooks.service.category.CategoryService;

/**
 * カテゴリAPIリソースクラス
 * 直接DBアクセスでカテゴリ情報を提供する
 */
@Path("/categories")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {
    private static final Logger logger = LoggerFactory.getLogger(CategoryResource.class);

    @Inject
    private CategoryService categoryService;

    /**
     * カテゴリ一覧取得（マップ形式：カテゴリ名→ID）
     * GET /api/categories
     */
    @GET
    public Response getAllCategories() {
        logger.info("[ CategoryResource#getAllCategories ]");
        Map<String, Integer> categoryMap = categoryService.getCategoryMap();
        return Response.ok(categoryMap).build();
    }
}
