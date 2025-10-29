/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.controller

import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.TypedValue
import com.ekino.oss.typedvalue.integrationtests.model.Person
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class PersonResponse(val id: String, val name: String)

@RestController
@RequestMapping("/api/persons")
class PersonController {

  // ===== TypedUuid endpoints =====

  @GetMapping("/uuid/{id}")
  fun getByUuidPath(@PathVariable id: TypedUuid<Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }

  @GetMapping("/uuid")
  fun getByUuidParam(@RequestParam id: TypedUuid<Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }

  // ===== TypedString endpoints =====

  @GetMapping("/string/{id}")
  fun getByStringPath(@PathVariable id: TypedString<Person>): PersonResponse {
    return PersonResponse(id = id.value, name = "Person ${id.value}")
  }

  @GetMapping("/string")
  fun getByStringParam(@RequestParam id: TypedString<Person>): PersonResponse {
    return PersonResponse(id = id.value, name = "Person ${id.value}")
  }

  // ===== TypedInt endpoints =====

  @GetMapping("/int/{id}")
  fun getByIntPath(@PathVariable id: TypedInt<Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }

  @GetMapping("/int")
  fun getByIntParam(@RequestParam id: TypedInt<Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }

  // ===== TypedLong endpoints =====

  @GetMapping("/long/{id}")
  fun getByLongPath(@PathVariable id: TypedLong<Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }

  @GetMapping("/long")
  fun getByLongParam(@RequestParam id: TypedLong<Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }

  // ===== Generic TypedValue endpoints =====

  @GetMapping("/generic-string/{id}")
  fun getByGenericStringPath(@PathVariable id: TypedValue<String, Person>): PersonResponse {
    return PersonResponse(id = id.value, name = "Person ${id.value}")
  }

  @GetMapping("/generic-string")
  fun getByGenericStringParam(@RequestParam id: TypedValue<String, Person>): PersonResponse {
    return PersonResponse(id = id.value, name = "Person ${id.value}")
  }

  @GetMapping("/generic-int/{id}")
  fun getByGenericIntPath(@PathVariable id: TypedValue<Int, Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }

  @GetMapping("/generic-int")
  fun getByGenericIntParam(@RequestParam id: TypedValue<Int, Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }

  @GetMapping("/generic-long/{id}")
  fun getByGenericLongPath(@PathVariable id: TypedValue<Long, Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }

  @GetMapping("/generic-long")
  fun getByGenericLongParam(@RequestParam id: TypedValue<Long, Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }

  @GetMapping("/generic-uuid/{id}")
  fun getByGenericUuidPath(@PathVariable id: TypedValue<UUID, Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }

  @GetMapping("/generic-uuid")
  fun getByGenericUuidParam(@RequestParam id: TypedValue<UUID, Person>): PersonResponse {
    return PersonResponse(id = id.value.toString(), name = "Person ${id.value}")
  }
}
