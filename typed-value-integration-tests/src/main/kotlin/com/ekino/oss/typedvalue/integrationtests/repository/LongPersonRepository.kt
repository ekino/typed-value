/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.repository

import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.integrationtests.model.LongPerson
import org.springframework.data.jpa.repository.JpaRepository

interface LongPersonRepository : JpaRepository<LongPerson, TypedLong<LongPerson>>
