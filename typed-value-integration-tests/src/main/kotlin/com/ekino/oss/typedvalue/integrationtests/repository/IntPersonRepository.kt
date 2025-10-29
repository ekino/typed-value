/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.repository

import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.integrationtests.model.IntPerson
import org.springframework.data.jpa.repository.JpaRepository

interface IntPersonRepository : JpaRepository<IntPerson, TypedInt<IntPerson>>
