package com.polyglot.persistence.children.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChildRepo extends JpaRepository<Child, UUID> {
}
