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
package com.osmerion.omittable.jackson3;

import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.core.Version;
import tools.jackson.databind.*;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.jsontype.TypeDeserializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.Serializers;
import tools.jackson.databind.type.ReferenceType;
import com.osmerion.omittable.Omittable;
import com.osmerion.omittable.jackson3.internal.*;
import org.jspecify.annotations.Nullable;

/**
 * A Jackson {@link JacksonModule} that adds support for {@link Omittable} types.
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
public final class OmittableModule extends JacksonModule {

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new OmittableSerializers());
        context.addDeserializers(new OmittableDeserializers());

        // And to fully support Omittables, need to modify type info:
        context.addTypeModifier(new OmittableTypeModifier());

        // Allow enabling "treat Optional.empty() like Java nulls"
        context.addSerializerModifier(new OmittableBeanSerializerModifier());
    }

    @Override
    public String getModuleName() {
        return "OmittableModule";
    }

    @Override
    public Version version() {
        return new Version(
            BuildConfig.VERSION_MAJOR,
            BuildConfig.VERSION_MINOR,
            BuildConfig.VERSION_PATCH,
            BuildConfig.SNAPSHOT_INFO,
            BuildConfig.GROUP_ID,
            BuildConfig.ARTIFACT_ID
        );
    }

    private static final class OmittableDeserializers extends Deserializers.Base {

        @Override
        public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
            return valueType.equals(Omittable.class);
        }

        @Override
        public @Nullable ValueDeserializer<?> findReferenceDeserializer(ReferenceType refType, DeserializationConfig config, BeanDescription.Supplier beanDescRef, TypeDeserializer contentTypeDeserializer, ValueDeserializer<?> contentDeserializer) {
            if (refType.hasRawClass(Omittable.class)) {
                return new OmittableDeserializer(refType, null, contentTypeDeserializer, contentDeserializer);
            }

            return null;
        }

    }

    private static final class OmittableSerializers extends Serializers.Base {

        @Override
        public @Nullable ValueSerializer<?> findReferenceSerializer(SerializationConfig config, ReferenceType type, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides, @Nullable TypeSerializer contentTypeSerializer, ValueSerializer<Object> contentValueSerializer) {
            Class<?> raw = type.getRawClass();
            if (Omittable.class.isAssignableFrom(raw)) {
                boolean staticTyping = (contentTypeSerializer == null) && config.isEnabled(MapperFeature.USE_STATIC_TYPING);
                return new OmittableSerializer(type, staticTyping, contentTypeSerializer, contentValueSerializer);
            }

            return null;
        }

    }

}
