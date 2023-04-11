package com.mycompany.myapp.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.mycompany.myapp.domain.Instance;
import com.mycompany.myapp.repository.InstanceRepository;
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
 * Spring Data Elasticsearch repository for the {@link Instance} entity.
 */
public interface InstanceSearchRepository extends ElasticsearchRepository<Instance, Long>, InstanceSearchRepositoryInternal {}

interface InstanceSearchRepositoryInternal {
    Stream<Instance> search(String query);

    Stream<Instance> search(Query query);

    void index(Instance entity);
}

class InstanceSearchRepositoryInternalImpl implements InstanceSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final InstanceRepository repository;

    InstanceSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, InstanceRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Instance> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery);
    }

    @Override
    public Stream<Instance> search(Query query) {
        return elasticsearchTemplate.search(query, Instance.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Instance entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
