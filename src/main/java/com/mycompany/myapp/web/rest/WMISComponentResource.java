package com.mycompany.myapp.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.mycompany.myapp.domain.WMISComponent;
import com.mycompany.myapp.repository.WMISComponentRepository;
import com.mycompany.myapp.repository.search.WMISComponentSearchRepository;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.WMISComponent}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class WMISComponentResource {

    private final Logger log = LoggerFactory.getLogger(WMISComponentResource.class);

    private static final String ENTITY_NAME = "wMISComponent";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final WMISComponentRepository wMISComponentRepository;

    private final WMISComponentSearchRepository wMISComponentSearchRepository;

    public WMISComponentResource(
        WMISComponentRepository wMISComponentRepository,
        WMISComponentSearchRepository wMISComponentSearchRepository
    ) {
        this.wMISComponentRepository = wMISComponentRepository;
        this.wMISComponentSearchRepository = wMISComponentSearchRepository;
    }

    /**
     * {@code POST  /wmis-components} : Create a new wMISComponent.
     *
     * @param wMISComponent the wMISComponent to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new wMISComponent, or with status {@code 400 (Bad Request)} if the wMISComponent has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/wmis-components")
    public ResponseEntity<WMISComponent> createWMISComponent(@RequestBody WMISComponent wMISComponent) throws URISyntaxException {
        log.debug("REST request to save WMISComponent : {}", wMISComponent);
        if (wMISComponent.getId() != null) {
            throw new BadRequestAlertException("A new wMISComponent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        WMISComponent result = wMISComponentRepository.save(wMISComponent);
        wMISComponentSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/wmis-components/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /wmis-components/:id} : Updates an existing wMISComponent.
     *
     * @param id the id of the wMISComponent to save.
     * @param wMISComponent the wMISComponent to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated wMISComponent,
     * or with status {@code 400 (Bad Request)} if the wMISComponent is not valid,
     * or with status {@code 500 (Internal Server Error)} if the wMISComponent couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/wmis-components/{id}")
    public ResponseEntity<WMISComponent> updateWMISComponent(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody WMISComponent wMISComponent
    ) throws URISyntaxException {
        log.debug("REST request to update WMISComponent : {}, {}", id, wMISComponent);
        if (wMISComponent.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, wMISComponent.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!wMISComponentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        WMISComponent result = wMISComponentRepository.save(wMISComponent);
        wMISComponentSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, wMISComponent.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /wmis-components/:id} : Partial updates given fields of an existing wMISComponent, field will ignore if it is null
     *
     * @param id the id of the wMISComponent to save.
     * @param wMISComponent the wMISComponent to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated wMISComponent,
     * or with status {@code 400 (Bad Request)} if the wMISComponent is not valid,
     * or with status {@code 404 (Not Found)} if the wMISComponent is not found,
     * or with status {@code 500 (Internal Server Error)} if the wMISComponent couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/wmis-components/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<WMISComponent> partialUpdateWMISComponent(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody WMISComponent wMISComponent
    ) throws URISyntaxException {
        log.debug("REST request to partial update WMISComponent partially : {}, {}", id, wMISComponent);
        if (wMISComponent.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, wMISComponent.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!wMISComponentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<WMISComponent> result = wMISComponentRepository
            .findById(wMISComponent.getId())
            .map(existingWMISComponent -> {
                if (wMISComponent.getComponentName() != null) {
                    existingWMISComponent.setComponentName(wMISComponent.getComponentName());
                }
                if (wMISComponent.getDescription() != null) {
                    existingWMISComponent.setDescription(wMISComponent.getDescription());
                }

                return existingWMISComponent;
            })
            .map(wMISComponentRepository::save)
            .map(savedWMISComponent -> {
                wMISComponentSearchRepository.save(savedWMISComponent);

                return savedWMISComponent;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, wMISComponent.getId().toString())
        );
    }

    /**
     * {@code GET  /wmis-components} : get all the wMISComponents.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of wMISComponents in body.
     */
    @GetMapping("/wmis-components")
    public List<WMISComponent> getAllWMISComponents() {
        log.debug("REST request to get all WMISComponents");
        return wMISComponentRepository.findAll();
    }

    /**
     * {@code GET  /wmis-components/:id} : get the "id" wMISComponent.
     *
     * @param id the id of the wMISComponent to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the wMISComponent, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/wmis-components/{id}")
    public ResponseEntity<WMISComponent> getWMISComponent(@PathVariable Long id) {
        log.debug("REST request to get WMISComponent : {}", id);
        Optional<WMISComponent> wMISComponent = wMISComponentRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(wMISComponent);
    }

    /**
     * {@code DELETE  /wmis-components/:id} : delete the "id" wMISComponent.
     *
     * @param id the id of the wMISComponent to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/wmis-components/{id}")
    public ResponseEntity<Void> deleteWMISComponent(@PathVariable Long id) {
        log.debug("REST request to delete WMISComponent : {}", id);
        wMISComponentRepository.deleteById(id);
        wMISComponentSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/wmis-components?query=:query} : search for the wMISComponent corresponding
     * to the query.
     *
     * @param query the query of the wMISComponent search.
     * @return the result of the search.
     */
    @GetMapping("/_search/wmis-components")
    public List<WMISComponent> searchWMISComponents(@RequestParam String query) {
        log.debug("REST request to search WMISComponents for query {}", query);
        return StreamSupport.stream(wMISComponentSearchRepository.search(query).spliterator(), false).collect(Collectors.toList());
    }
}
