package com.hampcode.pagoya.unit.shared;

import com.hampcode.pagoya.shared.util.MaskUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskUtilTest {

    @Test
    void maskName_hidesEachWordButFirstLetter() {
        assertThat(MaskUtil.maskName("Juan Carlos Perez")).isEqualTo("J*** C*** P***");
    }

    @Test
    void maskName_singleWord() {
        assertThat(MaskUtil.maskName("Yape")).isEqualTo("Y***");
    }

    @Test
    void maskName_nullOrBlank_returnsEmpty() {
        assertThat(MaskUtil.maskName(null)).isEmpty();
        assertThat(MaskUtil.maskName("   ")).isEmpty();
    }
}
