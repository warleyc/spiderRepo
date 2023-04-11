package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WMISComponentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(WMISComponent.class);
        WMISComponent wMISComponent1 = new WMISComponent();
        wMISComponent1.setId(1L);
        WMISComponent wMISComponent2 = new WMISComponent();
        wMISComponent2.setId(wMISComponent1.getId());
        assertThat(wMISComponent1).isEqualTo(wMISComponent2);
        wMISComponent2.setId(2L);
        assertThat(wMISComponent1).isNotEqualTo(wMISComponent2);
        wMISComponent1.setId(null);
        assertThat(wMISComponent1).isNotEqualTo(wMISComponent2);
    }
}
