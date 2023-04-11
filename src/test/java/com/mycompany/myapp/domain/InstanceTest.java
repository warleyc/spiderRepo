package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InstanceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Instance.class);
        Instance instance1 = new Instance();
        instance1.setId(1L);
        Instance instance2 = new Instance();
        instance2.setId(instance1.getId());
        assertThat(instance1).isEqualTo(instance2);
        instance2.setId(2L);
        assertThat(instance1).isNotEqualTo(instance2);
        instance1.setId(null);
        assertThat(instance1).isNotEqualTo(instance2);
    }
}
