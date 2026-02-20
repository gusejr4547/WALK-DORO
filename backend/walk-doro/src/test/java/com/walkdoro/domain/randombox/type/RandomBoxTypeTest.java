package com.walkdoro.domain.randombox.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RandomBoxTypeTest {

    @Test
    @DisplayName("RandomBoxType Enum 의 getPrice() 가 올바른 가격을 반환해야 한다")
    void testGetPrice() {
        assertThat(RandomBoxType.BASIC.getPrice()).isEqualTo(1L);
        assertThat(RandomBoxType.PREMIUM.getPrice()).isEqualTo(5L);
    }
}
