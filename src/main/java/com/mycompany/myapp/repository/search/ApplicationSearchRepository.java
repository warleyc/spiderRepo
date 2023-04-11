package com.mycompany.myapp.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.mycompany.myapp.domain.Application;
import com.mycompany.myapp.repository.ApplicationRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data Elasticsearch repository for the {@link Application} entity.
 */
public interface ApplicationSearchRepository extends ElasticsearchRepository<Application, Long>, ApplicationSearchRepositoryInternal {}

interface ApplicationSearchRepositoryInternal {
    Stream<Application> search(String query);

    Stream<Application> search(Query query);

    void index(Application entity);
}

class ApplicationSearchRepositoryInternalImpl implements ApplicationSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final ApplicationRepository repository;

    ApplicationSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, ApplicationRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Application> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery);
    }

    @Override
    public Stream<Application> search(Query query) {
        return elasticsearchTemplate.search(query, Application.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Application entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
