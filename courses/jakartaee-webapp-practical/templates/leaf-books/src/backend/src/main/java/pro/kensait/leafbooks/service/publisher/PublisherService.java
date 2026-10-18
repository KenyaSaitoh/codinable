package pro.kensait.leafbooks.service.publisher;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pro.kensait.leafbooks.dao.PublisherDao;
import pro.kensait.leafbooks.entity.Publisher;

@ApplicationScoped
public class PublisherService {
    private static final Logger logger = LoggerFactory.getLogger(PublisherService.class);

    @Inject
    private PublisherDao publisherDao;

    public List<Publisher> getPublishersAll() {
        logger.info("[ PublisherService#getPublishersAll ]");
        return publisherDao.findAll();
    }
}
