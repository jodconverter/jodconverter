/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jodconverter.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import org.jodconverter.test.util.AssertUtil;

public class JsonUtilsTest {

  @Test
  public void ctor_ClassWellDefined() throws Exception {

    AssertUtil.assertUtilityClassWellDefined(JsonUtils.class);
  }

  @Test
  public void toList_NullArray_ReturnNull() {

    assertThat(JsonUtils.toList(null)).isNull();
  }

  @Test
  public void toList_ArrayWithObjects_ReturnListWithObjects() {

    assertThat(JsonUtils.toList(new JSONArray("[value1, value2]"))).contains("value1", "value2");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void toList_ArrayWithMap_ReturnListWithInnerMap() {

    final List<Object> list =
        JsonUtils.toList(new JSONArray("[{prop1: 'value1', prop2: 'value2'}]"));
    assertThat(list).hasSize(1);
    final Object o = list.iterator().next();
    assertThat(o).isInstanceOf(Map.class);
    final Map<String, String> map = (Map<String, String>) o;
    assertThat(map).hasSize(2).containsEntry("prop1", "value1").containsEntry("prop2", "value2");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void toList_ArrayWithList_ReturnListWithInnerList() {

    final List<Object> list =
        JsonUtils.toList(new JSONArray("[['value1', 'value2'], ['value3', 'value4']]"));
    assertThat(list).hasSize(2);
    Iterator<Object> it = list.iterator();
    Object o = it.next();
    assertThat(o).isInstanceOf(List.class);
    List<String> innerList = (List<String>) o;
    assertThat(innerList).hasSize(2).contains("value1", "value2");
    o = it.next();
    assertThat(o).isInstanceOf(List.class);
    innerList = (List<String>) o;
    assertThat(innerList).hasSize(2).contains("value3", "value4");
  }

  @Test
  public void toMap_ObjectWithObjects_ReturnMapWithObjects() {

    final Map<String, Object> map =
        JsonUtils.toMap(new JSONObject("{prop1: 'value1', prop2: 'value2'}"));
    assertThat(map).hasSize(2).containsEntry("prop1", "value1").containsEntry("prop2", "value2");
  }

  @Test
  public void toMap_ObjectWithList_ReturnMapWithInnerList() {

    final Map<String, Object> map =
        JsonUtils.toMap(
            new JSONObject("{prop1: ['value1', 'value2'], prop2: ['value3', 'value4']}"));
    assertThat(map).hasSize(2).containsKeys("prop1", "prop2");
    Object o = map.get("prop1");
    assertThat(o).isInstanceOf(List.class).asList().contains("value1", "value2");
    o = map.get("prop2");
    assertThat(o).isInstanceOf(List.class).asList().contains("value3", "value4");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void toMap_ObjectWithMap_ReturnMapWithInnerMap() {

    final Map<String, Object> map =
        JsonUtils.toMap(
            new JSONObject(
                "{prop1: { innerprop1: 'value1', innerprop2: 'value2' },"
                    + "prop2: { innerprop1: 'value3', innerprop2: 'value4' }}"));
    assertThat(map).hasSize(2).containsKeys("prop1", "prop2");
    Object o = map.get("prop1");
    assertThat(o).isInstanceOf(Map.class);
    assertThat((Map<String, Object>) o)
        .hasSize(2)
        .containsEntry("innerprop1", "value1")
        .containsEntry("innerprop2", "value2");
    o = map.get("prop2");
    assertThat(o).isInstanceOf(Map.class);
    assertThat((Map<String, Object>) o)
        .hasSize(2)
        .containsEntry("innerprop1", "value3")
        .containsEntry("innerprop2", "value4");
  }

  @Test
  public void toMap_NullObject_ReturnNull() {

    assertThat(JsonUtils.toMap(null)).isNull();
  }
}
