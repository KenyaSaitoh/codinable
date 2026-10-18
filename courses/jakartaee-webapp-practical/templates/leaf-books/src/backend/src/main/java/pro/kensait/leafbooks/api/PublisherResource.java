package pro.kensait.leafbooks.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.leafbooks.api.dto.PublisherTO;
import pro.kensait.leafbooks.service.publisher.PublisherService;

/**
 * 出版社APIリソースクラス
 */
@Path("/publishers")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class PublisherResource {
    private static final Logger logger = LoggerFactory.getLogger(PublisherResource.class);

    @Inject
    private PublisherService publisherService;

    /**
     * 出版社一覧取得
     * GET /api/publishers
     */
    @GET
    public Response getAllPublishers() {
        logger.info("[ PublisherResource#getAllPublishers ]");
        List<PublisherTO> publishers = publisherService.getPublishersAll().stream()
                .map(p -> new PublisherTO(p.getPublisherId(), p.getPublisherName()))
                .toList();
        return Response.ok(publishers).build();
    }
}
