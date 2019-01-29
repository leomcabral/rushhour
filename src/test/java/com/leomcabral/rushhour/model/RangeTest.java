package com.leomcabral.rushhour.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Random;

public class RangeTest {

    @Test
    public void a() {
        for(int i = 0; i < 100; i++) {
            LocalTime[] randomValidRange = createRandomValidRange();
            System.out.println("" + Arrays.deepToString(randomValidRange));
        }
    }

    private LocalTime[] createRandomValidRange() {
        int maxHours = 8;
        int intervalHours = 1;
        int maxScrollWindowMinutes = 1 * 60; // 2 hours

        LocalTime in = LocalTime.of(9, 0);

        int scrollWindowMinutes = new Random(Instant.now().getNano()).nextInt(maxScrollWindowMinutes);
        System.out.println("Scrooling mins: " + scrollWindowMinutes);
        boolean up = new Random(Instant.now().getNano()).nextInt(2) % 2 == 0;
        if (up) {
            in = in.plusMinutes(scrollWindowMinutes);
        } else {
            in = in.minusMinutes(scrollWindowMinutes);
        }

        int intervalVariation = new Random(Instant.now().getNano()).nextInt(30);

        LocalTime intervalOut = in.plusMinutes((3 * 60) + intervalVariation);
        LocalTime intervalIn = intervalOut.plusHours(intervalHours);
        LocalTime out = in.plusHours(9);


        return new LocalTime[]{
                in,
                intervalOut,
                intervalIn,
                out
        };
    }
}
