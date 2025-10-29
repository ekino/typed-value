/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.repository

import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.integrationtests.model.UuidPerson
import org.springframework.data.jpa.repository.JpaRepository

interface UuidPersonRepository : JpaRepository<UuidPerson, TypedUuid<UuidPerson>>
