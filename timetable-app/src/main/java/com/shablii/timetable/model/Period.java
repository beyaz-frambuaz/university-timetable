package com.shablii.timetable.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public enum Period {
    FIRST(LocalTime.of(8, 15), LocalTime.of(9, 45)), SECOND(LocalTime.of(10, 0), LocalTime.of(11, 30)),
    THIRD(LocalTime.of(12, 30), LocalTime.of(14, 0)), FOURTH(LocalTime.of(14, 15), LocalTime.of(15, 45)),
    FIFTH(LocalTime.of(16, 0), LocalTime.of(17, 30));

    private final LocalTime begins;
    private final LocalTime ends;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    Period(LocalTime begins, LocalTime ends) {

        this.begins = begins;
        this.ends = ends;
    }

    public String getTime() {

        return begins.format(formatter) + "-" + ends.format(formatter);
    }

    @Override
    public String toString() {

        return this.name() + " " + this.getTime();
    }
}
