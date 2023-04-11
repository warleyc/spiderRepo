package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Instance;
import com.mycompany.myapp.repository.InstanceRepository;
import com.mycompany.myapp.repository.search.InstanceSearchRepository;
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
 * Integration tests for the {@link InstanceResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class InstanceResourceIT {

    private static final String DEFAULT_COUNTRY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_COUNTRY_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/instances";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/instances";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private InstanceSearchRepository instanceSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restInstanceMockMvc;

    private Instance instance;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Instance createEntity(EntityManager em) {
        Instance instance = new Instance().countryName(DEFAULT_COUNTRY_NAME);
        return instance;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Instance createUpdatedEntity(EntityManager em) {
        Instance instance = new Instance().countryName(UPDATED_COUNTRY_NAME);
        return instance;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        instanceSearchRepository.deleteAll();
        assertThat(instanceSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        instance = createEntity(em);
    }

    @Test
    @Transactional
    void createInstance() throws Exception {
        int databaseSizeBeforeCreate = instanceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        // Create the Instance
        restInstanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instance)))
            .andExpect(status().isCreated());

        // Validate the Instance in the database
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(instanceSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Instance testInstance = instanceList.get(instanceList.size() - 1);
        assertThat(testInstance.getCountryName()).isEqualTo(DEFAULT_COUNTRY_NAME);
    }

    @Test
    @Transactional
    void createInstanceWithExistingId() throws Exception {
        // Create the Instance with an existing ID
        instance.setId(1L);

        int databaseSizeBeforeCreate = instanceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(instanceSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restInstanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instance)))
            .andExpect(status().isBadRequest());

        // Validate the Instance in the database
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllInstances() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instance);

        // Get all the instanceList
        restInstanceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(instance.getId().intValue())))
            .andExpect(jsonPath("$.[*].countryName").value(hasItem(DEFAULT_COUNTRY_NAME)));
    }

    @Test
    @Transactional
    void getInstance() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instance);

        // Get the instance
        restInstanceMockMvc
            .perform(get(ENTITY_API_URL_ID, instance.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(instance.getId().intValue()))
            .andExpect(jsonPath("$.countryName").value(DEFAULT_COUNTRY_NAME));
    }

    @Test
    @Transactional
    void getNonExistingInstance() throws Exception {
        // Get the instance
        restInstanceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingInstance() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instance);

        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        instanceSearchRepository.save(instance);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(instanceSearchRepository.findAll());

        // Update the instance
        Instance updatedInstance = instanceRepository.findById(instance.getId()).get();
        // Disconnect from session so that the updates on updatedInstance are not directly saved in db
        em.detach(updatedInstance);
        updatedInstance.countryName(UPDATED_COUNTRY_NAME);

        restInstanceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedInstance.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedInstance))
            )
            .andExpect(status().isOk());

        // Validate the Instance in the database
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        Instance testInstance = instanceList.get(instanceList.size() - 1);
        assertThat(testInstance.getCountryName()).isEqualTo(UPDATED_COUNTRY_NAME);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(instanceSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Instance> instanceSearchList = IterableUtils.toList(instanceSearchRepository.findAll());
                Instance testInstanceSearch = instanceSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testInstanceSearch.getCountryName()).isEqualTo(UPDATED_COUNTRY_NAME);
            });
    }

    @Test
    @Transactional
    void putNonExistingInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        instance.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, instance.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(instance))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instance in the database
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        instance.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(instance))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instance in the database
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        instance.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instance)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Instance in the database
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateInstanceWithPatch() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instance);

        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();

        // Update the instance using partial update
        Instance partialUpdatedInstance = new Instance();
        partialUpdatedInstance.setId(instance.getId());

        partialUpdatedInstance.countryName(UPDATED_COUNTRY_NAME);

        restInstanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInstance.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedInstance))
            )
            .andExpect(status().isOk());

        // Validate the Instance in the database
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        Instance testInstance = instanceList.get(instanceList.size() - 1);
        assertThat(testInstance.getCountryName()).isEqualTo(UPDATED_COUNTRY_NAME);
    }

    @Test
    @Transactional
    void fullUpdateInstanceWithPatch() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instance);

        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();

        // Update the instance using partial update
        Instance partialUpdatedInstance = new Instance();
        partialUpdatedInstance.setId(instance.getId());

        partialUpdatedInstance.countryName(UPDATED_COUNTRY_NAME);

        restInstanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInstance.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedInstance))
            )
            .andExpect(status().isOk());

        // Validate the Instance in the database
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        Instance testInstance = instanceList.get(instanceList.size() - 1);
        assertThat(testInstance.getCountryName()).isEqualTo(UPDATED_COUNTRY_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        instance.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, instance.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(instance))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instance in the database
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        instance.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(instance))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instance in the database
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamInstance() throws Exception {
        int databaseSizeBeforeUpdate = instanceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        instance.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanceMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(instance)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Instance in the database
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteInstance() throws Exception {
        // Initialize the database
        instanceRepository.saveAndFlush(instance);
        instanceRepository.save(instance);
        instanceSearchRepository.save(instance);

        int databaseSizeBeforeDelete = instanceRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the instance
        restInstanceMockMvc
            .perform(delete(ENTITY_API_URL_ID, instance.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Instance> instanceList = instanceRepository.findAll();
        assertThat(instanceList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(instanceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchInstance() throws Exception {
        // Initialize the database
        instance = instanceRepository.saveAndFlush(instance);
        instanceSearchRepository.save(instance);

        // Search the instance
        restInstanceMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + instance.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(instance.getId().intValue())))
            .andExpect(jsonPath("$.[*].countryName").value(hasItem(DEFAULT_COUNTRY_NAME)));
    }
}
