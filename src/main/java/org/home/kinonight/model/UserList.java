package org.home.kinonight.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
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
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userList")
    private List<FilmUserList> filmUserLists;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserList userList = (UserList) o;
        return userId == userList.userId && Objects.equals(id, userList.id) && Objects.equals(listName, userList.listName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, listName);
    }
}
