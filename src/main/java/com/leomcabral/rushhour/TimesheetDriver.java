package com.leomcabral.rushhour;

import com.leomcabral.rushhour.model.WorkHour;
import io.vavr.control.Either;

import java.time.LocalDate;

public interface TimesheetDriver {

    /**
     * Logs in to the target timesheet system.
     *
     * @param username
     * @param password
     * @return
     */
    void login(String username, String password);

    WorkHour getRegisteredHours(LocalDate date);

    Either<Exception, WorkHour> register(LocalDate date);

}
