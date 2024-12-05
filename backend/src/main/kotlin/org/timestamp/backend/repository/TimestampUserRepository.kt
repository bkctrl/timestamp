package org.timestamp.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.timestamp.backend.model.User

interface TimestampUserRepository: JpaRepository<User, String>