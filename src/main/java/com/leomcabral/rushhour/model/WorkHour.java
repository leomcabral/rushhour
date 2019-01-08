package com.leomcabral.rushhour.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

//@Value
@Builder
@ToString
@Getter
public class WorkHour implements Comparable<WorkHour> {

    private final LocalDate date;
    private final LocalTime in;
    private final LocalTime intervalOut;
    private final LocalTime intervalIn;
    private final LocalTime out;
    private final boolean registered;

    public static WorkHour createNotRegisteredHour(LocalDate date) {
        return new WorkHour(date, null, null, null, null, false) {
            @Override
            public String toString() {
                return "Work Hour not registered for " + getDate();
            }
        };
    }

    @Override
    public int compareTo(WorkHour o) {
        return this.date.compareTo(o.date);
    }
}
