package com.mycompany.myapp.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.mycompany.myapp.domain.WMISComponent;
import com.mycompany.myapp.repository.WMISComponentRepository;
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
 * Spring Data Elasticsearch repository for the {@link WMISComponent} entity.
 */
public interface WMISComponentSearchRepository
    extends ElasticsearchRepository<WMISComponent, Long>, WMISComponentSearchRepositoryInternal {}

interface WMISComponentSearchRepositoryInternal {
    Stream<WMISComponent> search(String query);

    Stream<WMISComponent> search(Query query);

    void index(WMISComponent entity);
}

class WMISComponentSearchRepositoryInternalImpl implements WMISComponentSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final WMISComponentRepository repository;

    WMISComponentSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, WMISComponentRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<WMISComponent> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery);
    }

    @Override
    public Stream<WMISComponent> search(Query query) {
        return elasticsearchTemplate.search(query, WMISComponent.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(WMISComponent entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
