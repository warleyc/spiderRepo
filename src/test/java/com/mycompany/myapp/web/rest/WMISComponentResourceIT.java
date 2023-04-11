package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.WMISComponent;
import com.mycompany.myapp.repository.WMISComponentRepository;
import com.mycompany.myapp.repository.search.WMISComponentSearchRepository;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link WMISComponentResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class WMISComponentResourceIT {

    private static final String DEFAULT_COMPONENT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_COMPONENT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/wmis-components";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/wmis-components";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private WMISComponentRepository wMISComponentRepository;

    @Autowired
    private WMISComponentSearchRepository wMISComponentSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restWMISComponentMockMvc;

    private WMISComponent wMISComponent;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WMISComponent createEntity(EntityManager em) {
        WMISComponent wMISComponent = new WMISComponent().componentName(DEFAULT_COMPONENT_NAME).description(DEFAULT_DESCRIPTION);
        return wMISComponent;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WMISComponent createUpdatedEntity(EntityManager em) {
        WMISComponent wMISComponent = new WMISComponent().componentName(UPDATED_COMPONENT_NAME).description(UPDATED_DESCRIPTION);
        return wMISComponent;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        wMISComponentSearchRepository.deleteAll();
        assertThat(wMISComponentSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        wMISComponent = createEntity(em);
    }

    @Test
    @Transactional
    void createWMISComponent() throws Exception {
        int databaseSizeBeforeCreate = wMISComponentRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        // Create the WMISComponent
        restWMISComponentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(wMISComponent)))
            .andExpect(status().isCreated());

        // Validate the WMISComponent in the database
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        WMISComponent testWMISComponent = wMISComponentList.get(wMISComponentList.size() - 1);
        assertThat(testWMISComponent.getComponentName()).isEqualTo(DEFAULT_COMPONENT_NAME);
        assertThat(testWMISComponent.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void createWMISComponentWithExistingId() throws Exception {
        // Create the WMISComponent with an existing ID
        wMISComponent.setId(1L);

        int databaseSizeBeforeCreate = wMISComponentRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restWMISComponentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(wMISComponent)))
            .andExpect(status().isBadRequest());

        // Validate the WMISComponent in the database
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllWMISComponents() throws Exception {
        // Initialize the database
        wMISComponentRepository.saveAndFlush(wMISComponent);

        // Get all the wMISComponentList
        restWMISComponentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(wMISComponent.getId().intValue())))
            .andExpect(jsonPath("$.[*].componentName").value(hasItem(DEFAULT_COMPONENT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getWMISComponent() throws Exception {
        // Initialize the database
        wMISComponentRepository.saveAndFlush(wMISComponent);

        // Get the wMISComponent
        restWMISComponentMockMvc
            .perform(get(ENTITY_API_URL_ID, wMISComponent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(wMISComponent.getId().intValue()))
            .andExpect(jsonPath("$.componentName").value(DEFAULT_COMPONENT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingWMISComponent() throws Exception {
        // Get the wMISComponent
        restWMISComponentMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingWMISComponent() throws Exception {
        // Initialize the database
        wMISComponentRepository.saveAndFlush(wMISComponent);

        int databaseSizeBeforeUpdate = wMISComponentRepository.findAll().size();
        wMISComponentSearchRepository.save(wMISComponent);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());

        // Update the wMISComponent
        WMISComponent updatedWMISComponent = wMISComponentRepository.findById(wMISComponent.getId()).get();
        // Disconnect from session so that the updates on updatedWMISComponent are not directly saved in db
        em.detach(updatedWMISComponent);
        updatedWMISComponent.componentName(UPDATED_COMPONENT_NAME).description(UPDATED_DESCRIPTION);

        restWMISComponentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedWMISComponent.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedWMISComponent))
            )
            .andExpect(status().isOk());

        // Validate the WMISComponent in the database
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeUpdate);
        WMISComponent testWMISComponent = wMISComponentList.get(wMISComponentList.size() - 1);
        assertThat(testWMISComponent.getComponentName()).isEqualTo(UPDATED_COMPONENT_NAME);
        assertThat(testWMISComponent.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<WMISComponent> wMISComponentSearchList = IterableUtils.toList(wMISComponentSearchRepository.findAll());
                WMISComponent testWMISComponentSearch = wMISComponentSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testWMISComponentSearch.getComponentName()).isEqualTo(UPDATED_COMPONENT_NAME);
                assertThat(testWMISComponentSearch.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
            });
    }

    @Test
    @Transactional
    void putNonExistingWMISComponent() throws Exception {
        int databaseSizeBeforeUpdate = wMISComponentRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        wMISComponent.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWMISComponentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, wMISComponent.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(wMISComponent))
            )
            .andExpect(status().isBadRequest());

        // Validate the WMISComponent in the database
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchWMISComponent() throws Exception {
        int databaseSizeBeforeUpdate = wMISComponentRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        wMISComponent.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWMISComponentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(wMISComponent))
            )
            .andExpect(status().isBadRequest());

        // Validate the WMISComponent in the database
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamWMISComponent() throws Exception {
        int databaseSizeBeforeUpdate = wMISComponentRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        wMISComponent.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWMISComponentMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(wMISComponent)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the WMISComponent in the database
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateWMISComponentWithPatch() throws Exception {
        // Initialize the database
        wMISComponentRepository.saveAndFlush(wMISComponent);

        int databaseSizeBeforeUpdate = wMISComponentRepository.findAll().size();

        // Update the wMISComponent using partial update
        WMISComponent partialUpdatedWMISComponent = new WMISComponent();
        partialUpdatedWMISComponent.setId(wMISComponent.getId());

        partialUpdatedWMISComponent.description(UPDATED_DESCRIPTION);

        restWMISComponentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWMISComponent.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedWMISComponent))
            )
            .andExpect(status().isOk());

        // Validate the WMISComponent in the database
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeUpdate);
        WMISComponent testWMISComponent = wMISComponentList.get(wMISComponentList.size() - 1);
        assertThat(testWMISComponent.getComponentName()).isEqualTo(DEFAULT_COMPONENT_NAME);
        assertThat(testWMISComponent.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void fullUpdateWMISComponentWithPatch() throws Exception {
        // Initialize the database
        wMISComponentRepository.saveAndFlush(wMISComponent);

        int databaseSizeBeforeUpdate = wMISComponentRepository.findAll().size();

        // Update the wMISComponent using partial update
        WMISComponent partialUpdatedWMISComponent = new WMISComponent();
        partialUpdatedWMISComponent.setId(wMISComponent.getId());

        partialUpdatedWMISComponent.componentName(UPDATED_COMPONENT_NAME).description(UPDATED_DESCRIPTION);

        restWMISComponentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWMISComponent.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedWMISComponent))
            )
            .andExpect(status().isOk());

        // Validate the WMISComponent in the database
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeUpdate);
        WMISComponent testWMISComponent = wMISComponentList.get(wMISComponentList.size() - 1);
        assertThat(testWMISComponent.getComponentName()).isEqualTo(UPDATED_COMPONENT_NAME);
        assertThat(testWMISComponent.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void patchNonExistingWMISComponent() throws Exception {
        int databaseSizeBeforeUpdate = wMISComponentRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        wMISComponent.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWMISComponentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, wMISComponent.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(wMISComponent))
            )
            .andExpect(status().isBadRequest());

        // Validate the WMISComponent in the database
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchWMISComponent() throws Exception {
        int databaseSizeBeforeUpdate = wMISComponentRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        wMISComponent.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWMISComponentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(wMISComponent))
            )
            .andExpect(status().isBadRequest());

        // Validate the WMISComponent in the database
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamWMISComponent() throws Exception {
        int databaseSizeBeforeUpdate = wMISComponentRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        wMISComponent.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWMISComponentMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(wMISComponent))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the WMISComponent in the database
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteWMISComponent() throws Exception {
        // Initialize the database
        wMISComponentRepository.saveAndFlush(wMISComponent);
        wMISComponentRepository.save(wMISComponent);
        wMISComponentSearchRepository.save(wMISComponent);

        int databaseSizeBeforeDelete = wMISComponentRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the wMISComponent
        restWMISComponentMockMvc
            .perform(delete(ENTITY_API_URL_ID, wMISComponent.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<WMISComponent> wMISComponentList = wMISComponentRepository.findAll();
        assertThat(wMISComponentList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(wMISComponentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchWMISComponent() throws Exception {
        // Initialize the database
        wMISComponent = wMISComponentRepository.saveAndFlush(wMISComponent);
        wMISComponentSearchRepository.save(wMISComponent);

        // Search the wMISComponent
        restWMISComponentMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + wMISComponent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(wMISComponent.getId().intValue())))
            .andExpect(jsonPath("$.[*].componentName").value(hasItem(DEFAULT_COMPONENT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }
}
