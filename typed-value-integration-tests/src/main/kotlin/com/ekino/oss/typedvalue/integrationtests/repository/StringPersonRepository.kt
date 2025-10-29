/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.repository

import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.integrationtests.model.StringPerson
import org.springframework.data.jpa.repository.JpaRepository

interface StringPersonRepository : JpaRepository<StringPerson, TypedString<StringPerson>>
