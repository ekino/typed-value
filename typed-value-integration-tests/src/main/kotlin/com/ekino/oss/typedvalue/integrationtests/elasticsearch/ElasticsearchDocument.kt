/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.elasticsearch

import com.ekino.oss.typedvalue.TypedValue

interface ElasticsearchDocument<DOCUMENT : Any, ID : Comparable<ID>> {
  val id: TypedValue<ID, DOCUMENT>
}
