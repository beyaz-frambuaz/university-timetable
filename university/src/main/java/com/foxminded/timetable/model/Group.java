package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group implements Comparable<Group> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "g_seq")
    @SequenceGenerator(name = "g_seq", sequenceName = "group_id_seq")
    private Long id;

    private String name;

    public Group(String name) {

        this.name = name;
    }

    @Override
    public int compareTo(Group other) {

        return id.compareTo(other.getId());
    }

}
