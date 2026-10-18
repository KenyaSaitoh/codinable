package pro.kensait.leafbooks.api;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.leafbooks.api.dto.StockTO;
import pro.kensait.leafbooks.api.dto.StockUpdateRequest;
import pro.kensait.leafbooks.entity.Stock;
import pro.kensait.leafbooks.service.stock.StockService;

/**
 * 在庫API リソースクラス
 * 不存在は NotFoundExceptionMapper（404）、versionの不一致は OptimisticLockExceptionMapper（409）が応答を作る
 */
@Path("/stocks")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class StockResource {
    private static final Logger logger = LoggerFactory.getLogger(StockResource.class);

    @Inject
    private StockService stockService;

    @GET
    public Response getAllStocks() {
        logger.info("[ StockResource#getAllStocks ]");
        List<Stock> stocks = stockService.getStocksAll();
        List<StockTO> stockTOs = stocks.stream()
                .map(this::convertToStockTO)
                .collect(Collectors.toList());
        return Response.ok(stockTOs).build();
    }

    @GET
    @Path("{bookId}")
    public Response getStock(@PathParam("bookId") Integer bookId) {
        logger.info("[ StockResource#getStock ] bookId: {}", bookId);
        Stock stock = stockService.getStock(bookId);
        return Response.ok(convertToStockTO(stock)).build();
    }

    /**
     * 在庫数の更新
     * version の照合と更新は StockService が 1 つのトランザクションで行う
     */
    @PUT
    @Path("{bookId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateStock(
            @PathParam("bookId") Integer bookId,
            @Valid StockUpdateRequest request) {
        logger.info("[ StockResource#updateStock ] bookId: {}, version: {}, quantity: {}",
                bookId, request.getVersion(), request.getQuantity());
        Stock stock = stockService.updateStock(bookId, request.getVersion(), request.getQuantity());
        return Response.ok(convertToStockTO(stock)).build();
    }

    private StockTO convertToStockTO(Stock stock) {
        return new StockTO(stock.getBookId(), stock.getQuantity(), stock.getVersion());
    }
}
