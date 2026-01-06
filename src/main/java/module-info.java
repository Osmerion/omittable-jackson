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
import com.fasterxml.jackson.databind.Module;
import com.osmerion.omittable.jackson.OmittableModule;
import org.jspecify.annotations.NullMarked;

/** Defines the {@link OmittableModule} that provides Jackson integration for omittable types. */
@NullMarked
module com.osmerion.omittable.jackson {

    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.osmerion.omittable;
    requires transitive org.jspecify;

    exports com.osmerion.omittable.jackson;

    provides Module with OmittableModule;

}
