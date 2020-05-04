package com.foxminded.timetable.dao.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import com.foxminded.timetable.dao.GroupDao;
import com.foxminded.timetable.model.Group;

@JdbcTest
@ComponentScan
@Sql("classpath:schema.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class JdbcGroupDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private GroupDao groupRepository;

    private Group groupOne = new Group(1L, "one");
    private Group groupTwo = new Group(2L, "two");
    private Group groupThree = new Group(3L, "three");

    private List<Group> groups = Arrays.asList(groupOne, groupTwo, groupThree);

    @BeforeEach
    private void setUp() {
        this.groupRepository = new JdbcGroupDao(jdbc);
    }

    @Test
    @Sql("classpath:preload_sample_data_group_test.sql")
    public void countShouldReturnCorrectAmountOfGroups() {

        long expected = 3L;
        long actual = groupRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_group_test.sql")
    public void findAllShouldRetrieveCorrectListOfGroups() {

        List<Group> actual = groupRepository.findAll();

        assertThat(actual).hasSameElementsAs(groups);
    }

    @Test
    @Sql("classpath:preload_sample_data_group_test.sql")
    public void findAllByProfessorAndCourseShouldReturnCorrectListOfGroups() {

        Group expectedOne = groups.get(0);
        Group notExpected = groups.get(1);
        Group expectedTwo = groups.get(2);

        List<Group> actual = groupRepository.findAllByProfessorAndCourse(1L,
                1L);

        assertThat(actual).containsOnly(expectedOne, expectedTwo)
                .doesNotContain(notExpected);
    }

    @Test
    @Sql("classpath:preload_sample_data_group_test.sql")
    public void findByIdShouldReturnCorrectGroups() {

        Group expectedGroup = new Group(3L, "three");
        Optional<Group> actualAuditorium = groupRepository.findById(3L);

        assertThat(actualAuditorium).isNotEmpty().contains(expectedGroup);
    }

    @Test
    @Sql("classpath:preload_sample_data_group_test.sql")
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        Optional<Group> actualGroup = groupRepository.findById(999L);

        assertThat(actualGroup).isEmpty();
    }

    @Test
    public void saveShouldAddGroup() {

        Group expected = groupRepository.save(groupOne);

        String sql = "SELECT groups.id, groups.name FROM groups "
                + "WHERE groups.id = :id";
        Group actual = jdbc.queryForObject(sql,
                new MapSqlParameterSource("id", groupOne.getId()),
                (ResultSet rs, int rowNum) -> new Group(rs.getLong(1),
                        rs.getString(2)));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_group_test.sql")
    public void udpateShouldUpdateGroupName() {

        String newName = "new name";
        Group expected = new Group(groupOne.getId(), groupOne.getName());
        expected.setName(newName);

        groupRepository.update(expected);

        String sql = "SELECT groups.id, groups.name FROM groups "
                + "WHERE groups.id = :id";
        Optional<Group> actual = Optional.of(jdbc.queryForObject(sql,
                new MapSqlParameterSource("id", groupOne.getId()),
                (ResultSet rs, int rowNum) -> new Group(rs.getLong(1),
                        rs.getString(2))));

        assertThat(actual).isPresent().contains(expected);
    }

    @Test
    public void saveAllShouldSaveAllGroups() {

        groupRepository.saveAll(groups);

        String sql = "SELECT groups.id, groups.name FROM groups";
        List<Group> actual = jdbc.query(sql, (ResultSet rs,
                int rowNum) -> new Group(rs.getLong(1), rs.getString(2)));

        assertThat(actual).hasSameElementsAs(groups);
    }

}
