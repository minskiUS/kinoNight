package org.home.kinonight.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Entity
@Data
public class UserList {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private long userId;
    private String listName;
}
