package org.home.kinonight.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserList {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private long userId;
    private String listName;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "film_user_list",
            joinColumns = @JoinColumn(name = "user_list_id"),
            inverseJoinColumns = @JoinColumn(name = "film_id"))
    private List<Film> films;
}
