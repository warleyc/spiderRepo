package com.mycompany.myapp.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.mycompany.myapp.domain.Instance;
import com.mycompany.myapp.repository.InstanceRepository;
import com.mycompany.myapp.repository.search.InstanceSearchRepository;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Instance}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class InstanceResource {

    private final Logger log = LoggerFactory.getLogger(InstanceResource.class);

    private static final String ENTITY_NAME = "instance";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InstanceRepository instanceRepository;

    private final InstanceSearchRepository instanceSearchRepository;

    public InstanceResource(InstanceRepository instanceRepository, InstanceSearchRepository instanceSearchRepository) {
        this.instanceRepository = instanceRepository;
        this.instanceSearchRepository = instanceSearchRepository;
    }

    /**
     * {@code POST  /instances} : Create a new instance.
     *
     * @param instance the instance to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new instance, or with status {@code 400 (Bad Request)} if the instance has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/instances")
    public ResponseEntity<Instance> createInstance(@RequestBody Instance instance) throws URISyntaxException {
        log.debug("REST request to save Instance : {}", instance);
        if (instance.getId() != null) {
            throw new BadRequestAlertException("A new instance cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Instance result = instanceRepository.save(instance);
        instanceSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/instances/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /instances/:id} : Updates an existing instance.
     *
     * @param id the id of the instance to save.
     * @param instance the instance to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated instance,
     * or with status {@code 400 (Bad Request)} if the instance is not valid,
     * or with status {@code 500 (Internal Server Error)} if the instance couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/instances/{id}")
    public ResponseEntity<Instance> updateInstance(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Instance instance
    ) throws URISyntaxException {
        log.debug("REST request to update Instance : {}, {}", id, instance);
        if (instance.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, instance.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!instanceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Instance result = instanceRepository.save(instance);
        instanceSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, instance.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /instances/:id} : Partial updates given fields of an existing instance, field will ignore if it is null
     *
     * @param id the id of the instance to save.
     * @param instance the instance to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated instance,
     * or with status {@code 400 (Bad Request)} if the instance is not valid,
     * or with status {@code 404 (Not Found)} if the instance is not found,
     * or with status {@code 500 (Internal Server Error)} if the instance couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/instances/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Instance> partialUpdateInstance(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Instance instance
    ) throws URISyntaxException {
        log.debug("REST request to partial update Instance partially : {}, {}", id, instance);
        if (instance.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, instance.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!instanceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Instance> result = instanceRepository
            .findById(instance.getId())
            .map(existingInstance -> {
                if (instance.getCountryName() != null) {
                    existingInstance.setCountryName(instance.getCountryName());
                }

                return existingInstance;
            })
            .map(instanceRepository::save)
            .map(savedInstance -> {
                instanceSearchRepository.save(savedInstance);

                return savedInstance;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, instance.getId().toString())
        );
    }

    /**
     * {@code GET  /instances} : get all the instances.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of instances in body.
     */
    @GetMapping("/instances")
    public List<Instance> getAllInstances() {
        log.debug("REST request to get all Instances");
        return instanceRepository.findAll();
    }

    /**
     * {@code GET  /instances/:id} : get the "id" instance.
     *
     * @param id the id of the instance to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the instance, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/instances/{id}")
    public ResponseEntity<Instance> getInstance(@PathVariable Long id) {
        log.debug("REST request to get Instance : {}", id);
        Optional<Instance> instance = instanceRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(instance);
    }

    /**
     * {@code DELETE  /instances/:id} : delete the "id" instance.
     *
     * @param id the id of the instance to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/instances/{id}")
    public ResponseEntity<Void> deleteInstance(@PathVariable Long id) {
        log.debug("REST request to delete Instance : {}", id);
        instanceRepository.deleteById(id);
        instanceSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/instances?query=:query} : search for the instance corresponding
     * to the query.
     *
     * @param query the query of the instance search.
     * @return the result of the search.
     */
    @GetMapping("/_search/instances")
    public List<Instance> searchInstances(@RequestParam String query) {
        log.debug("REST request to search Instances for query {}", query);
        return StreamSupport.stream(instanceSearchRepository.search(query).spliterator(), false).collect(Collectors.toList());
    }
}
