package com.back.ourlog.domain.ott.repository

import com.back.ourlog.domain.ott.entity.Ott
import org.springframework.data.jpa.repository.JpaRepository

interface OttRepository : JpaRepository<Ott, Int>
