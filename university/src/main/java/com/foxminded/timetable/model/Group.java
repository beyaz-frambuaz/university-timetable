package com.foxminded.timetable.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Table(name = "groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group implements Comparable<Group> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "g_seq")
    @SequenceGenerator(name = "g_seq", sequenceName = "group_id_seq")
    @Min(1)
    private Long id;

    @NotBlank
    private String name;

    public Group(String name) {

        this.name = name;
    }

    @Override
    public int compareTo(Group other) {

        return id.compareTo(other.getId());
    }

}
