/*
 * Copyright 2025-2026 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.osmerion.omittable.jackson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osmerion.omittable.Omittable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public final class OmittableJacksonJavaTest {

    private static final ObjectMapper manualMapper, autoDiscoveryMapper;

    static {
        manualMapper = new ObjectMapper();
        manualMapper.registerModule(new OmittableModule());

        autoDiscoveryMapper = new ObjectMapper();
        autoDiscoveryMapper.findAndRegisterModules();
    }

    private static Stream<ObjectMapper> objectMapper() {
        return Stream.of(manualMapper, autoDiscoveryMapper);
    }

    static class TestDto {
        private Omittable<String> name = Omittable.absent();
        private Omittable<Integer> count = Omittable.absent();
        private Omittable<String> nullableValue = Omittable.absent();

        // Getters and setters are needed for Jackson in Java
        public Omittable<String> getName() { return name; }
        public void setName(Omittable<String> name) { this.name = name; }
        public Omittable<Integer> getCount() { return count; }
        public void setCount(Omittable<Integer> count) { this.count = count; }
        public Omittable<String> getNullableValue() { return nullableValue; }
        public void setNullableValue(Omittable<String> nullableValue) { this.nullableValue = nullableValue; }
    }

    @ParameterizedTest
    @MethodSource("objectMapper")
    void shouldSerializePresentValuesCorrectly(ObjectMapper objectMapper) throws Exception {
        TestDto dto = new TestDto();
        dto.setName(Omittable.of("Test"));
        dto.setCount(Omittable.of(123));

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"name\":\"Test\"");
        assertThat(json).contains("\"count\":123");
        assertThat(json).doesNotContain("nullableValue");
    }

    @ParameterizedTest
    @MethodSource("objectMapper")
    void shouldOmitAbsentValuesFromJsonOutput(ObjectMapper objectMapper) throws Exception {
        TestDto dto = new TestDto();
        dto.setName(Omittable.of("Only Name"));

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).isEqualTo("{\"name\":\"Only Name\"}");
    }

    @ParameterizedTest
    @MethodSource("objectMapper")
    void shouldSerializeAPresentButNullValue(ObjectMapper objectMapper) throws Exception {
        TestDto dto = new TestDto();
        dto.setNullableValue(Omittable.of(null));

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).isEqualTo("{\"nullableValue\":null}");
    }

    @ParameterizedTest
    @MethodSource("objectMapper")
    void shouldDeserializePresentValuesCorrectly(ObjectMapper objectMapper) throws Exception {
        String json = "{\"name\":\"Test\",\"count\":123,\"nullableValue\":null}";
        TestDto dto = objectMapper.readValue(json, TestDto.class);

        assertThat(dto.getName()).isEqualTo(Omittable.of("Test"));
        assertThat(dto.getCount()).isEqualTo(Omittable.of(123));
        assertThat(dto.getNullableValue()).isEqualTo(Omittable.of(null));
    }

    @ParameterizedTest
    @MethodSource("objectMapper")
    void shouldDeserializeMissingFieldsToAbsent(ObjectMapper objectMapper) throws Exception {
        String json = "{\"name\":\"Only Name\"}";
        TestDto dto = objectMapper.readValue(json, TestDto.class);

        assertThat(dto.getName()).isEqualTo(Omittable.of("Only Name"));
        assertThat(dto.getCount()).isEqualTo(Omittable.absent());
        assertThat(dto.getNullableValue()).isEqualTo(Omittable.absent());
    }

    static class Child {
        private Omittable<String> inner = Omittable.absent();
        public Omittable<String> getInner() { return inner; }
        public void setInner(Omittable<String> inner) { this.inner = inner; }
    }

    static class Parent {
        private final Child child = new Child();
        @JsonUnwrapped
        public Child getChild() { return child; }
    }

    @ParameterizedTest
    @MethodSource("objectMapper")
    void shouldOmitAbsentValuesWhenUnwrapped(ObjectMapper objectMapper) throws Exception {
        Parent parent = new Parent();
        String json = objectMapper.writeValueAsString(parent);

        assertThat(json).isEqualTo("{}");

        parent.getChild().setInner(Omittable.of("value"));
        json = objectMapper.writeValueAsString(parent);
        assertThat(json).isEqualTo("{\"inner\":\"value\"}");
    }

    @Test
    void shouldRespectPropertyNamingStrategy() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new OmittableModule());
        objectMapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);

        TestDto dto = new TestDto();
        dto.setNullableValue(Omittable.of("test"));

        String json = objectMapper.writeValueAsString(dto);

        // Ensures OmittableBeanPropertyWriter._new(PropertyName) was called correctly
        assertThat(json).contains("\"nullable_value\":\"test\"");
    }

    static class CollectionDto {
        private Omittable<java.util.List<String>> items = Omittable.absent();
        public Omittable<java.util.List<String>> getItems() { return items; }
        public void setItems(Omittable<java.util.List<String>> items) { this.items = items; }
    }

    @ParameterizedTest
    @MethodSource("objectMapper")
    void shouldHandleGenericCollectionsCorrectly(ObjectMapper objectMapper) throws Exception {
        CollectionDto dto = new CollectionDto();
        dto.setItems(Omittable.of(java.util.List.of("A", "B")));

        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).isEqualTo("{\"items\":[\"A\",\"B\"]}");

        CollectionDto deserialized = objectMapper.readValue(json, CollectionDto.class);
        assertThat(deserialized.getItems().orElseThrow()).containsExactly("A", "B");
    }

    static class NestedDto {
        private Omittable<TestDto> nested = Omittable.absent();
        public Omittable<TestDto> getNested() { return nested; }
        public void setNested(Omittable<TestDto> nested) { this.nested = nested; }
    }

    @ParameterizedTest
    @MethodSource("objectMapper")
    void shouldHandleEmptyOmittableObjectsCorrectly(ObjectMapper objectMapper) throws Exception {
        NestedDto dto = new NestedDto();
        // The property itself is present, but the object it points to is empty
        dto.setNested(Omittable.of(new TestDto()));

        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).isEqualTo("{\"nested\":{}}");
    }

    // This test ensures that findReferenceSerializer passed the TypeSerializer correctly
    static class PolyDto {
        private Omittable<? extends Shape> shape = Omittable.absent();
        public Omittable<? extends Shape> getShape() { return shape; }
        public void setShape(Omittable<? extends Shape> shape) { this.shape = shape; }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Circle.class, name = "circle")
    })
    interface Shape {}
    static class Circle implements Shape {
        @SuppressWarnings("unused")
        public int radius = 5;
    }

    @ParameterizedTest
    @MethodSource("objectMapper")
    void shouldHandlePolymorphicTypesInsideOmittable(ObjectMapper objectMapper) throws Exception {
        PolyDto dto = new PolyDto();
        dto.setShape(Omittable.of(new Circle()));

        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).contains("\"@type\":\"circle\"");
    }

    @ParameterizedTest
    @MethodSource("objectMapper")
    void shouldSupportGenericReferenceTypeResolution(ObjectMapper objectMapper) {
        // This ensures the TypeModifier is working and the ReferenceType is correctly resolved
        java.lang.reflect.Type type = new com.fasterxml.jackson.core.type.TypeReference<Omittable<String>>() {}.getType();
        JavaType javaType = objectMapper.getTypeFactory().constructType(type);

        assertThat(javaType.isReferenceType()).isTrue();
        assertThat(javaType.getReferencedType().getRawClass()).isEqualTo(String.class);
    }

}
