package com.jakduk.core.admin;

import com.jakduk.core.repository.AttendanceLeagueRepository;
import com.jakduk.core.util.AbstractSpringTest;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by pyohwan on 17. 1. 3.
 */
public class AttendanceLeagueRepositoryTest extends AbstractSpringTest {

    @Autowired
    private AttendanceLeagueRepository sut;

    @Test
    public void findByCompetitionId() {
        sut.findByCompetitionId("57600cef44ab251f9d64b4f8");
    }
}
