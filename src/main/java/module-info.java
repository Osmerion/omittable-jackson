import org.jspecify.annotations.NullMarked;

@NullMarked
module com.osmerion.omittable.jackson {

    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.osmerion.omittable;
    requires transitive org.jspecify;

    exports com.osmerion.omittable.jackson;

}
