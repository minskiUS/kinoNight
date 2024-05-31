package org.home.kinonight.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

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
}
