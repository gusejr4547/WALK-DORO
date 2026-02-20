package com.walkdoro.global.util;

import org.springframework.stereotype.Component;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DefaultRandomGenerator implements RandomGenerator {
    @Override
    public double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }
}
