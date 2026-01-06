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
package com.osmerion.omittable.jackson3.internal;

import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueInstantiator;
import tools.jackson.databind.deser.std.ReferenceTypeDeserializer;
import tools.jackson.databind.jsontype.TypeDeserializer;
import com.osmerion.omittable.Omittable;
import org.jspecify.annotations.Nullable;

public final class OmittableDeserializer extends ReferenceTypeDeserializer<Omittable<?>> {

    public OmittableDeserializer(
        JavaType fullType,
        @Nullable ValueInstantiator inst,
        TypeDeserializer typeDeser,
        ValueDeserializer<?> deser
    ) {
        super(fullType, inst, typeDeser, deser);
    }

    @Override
    public OmittableDeserializer withResolved(TypeDeserializer typeDeser, ValueDeserializer<?> valueDeser) {
        return new OmittableDeserializer(_fullType, _valueInstantiator, typeDeser, valueDeser);
    }

    @Override
    public Omittable<?> getNullValue(DeserializationContext ctxt) {
        return Omittable.of(_valueDeserializer.getNullValue(ctxt));
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) {
        return this.getNullValue(ctxt);
    }

    @Override
    public Object getAbsentValue(DeserializationContext ctxt) {
        return Omittable.absent();
    }

    @Override
    public Omittable<?> referenceValue(Object contents) {
        return Omittable.of(contents);
    }

    @Override
    public @Nullable Object getReferenced(Omittable<?> reference) {
        return reference.isPresent() ? reference.orElseThrow() : null;
    }

    @Override
    public Omittable<?> updateReference(Omittable<?> reference, Object contents) {
        return Omittable.of(contents);
    }

    // Default ought to be fine:
//    public Boolean supportsUpdate(DeserializationConfig config) { }

}
