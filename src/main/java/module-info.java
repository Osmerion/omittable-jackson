import com.osmerion.omittable.jackson.OmittableModule;
import org.jspecify.annotations.NullMarked;

/** Defines the {@link OmittableModule} that provides Jackson integration for omittable types. */
@NullMarked
module com.osmerion.omittable.jackson {

    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.osmerion.omittable;
    requires transitive org.jspecify;

    exports com.osmerion.omittable.jackson;

}
