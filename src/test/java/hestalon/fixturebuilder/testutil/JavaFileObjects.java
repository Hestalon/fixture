package hestalon.fixturebuilder.testutil;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import static javax.tools.JavaFileObject.Kind.SOURCE;

public final class JavaFileObjects {

    private JavaFileObjects() {
    }

    public static JavaFileObject forSourceString(String fullyQualifiedName, String source) {
        if (fullyQualifiedName.startsWith("package ")) {
            throw new IllegalArgumentException(
                String.format("fullyQualifiedName starts with \"package\" (%s). Did you forget to "
                              + "specify the name and specify just the source text?", fullyQualifiedName));
        }

        return new InMemoryJavaFileObject(StandardLocation.SOURCE_PATH, fullyQualifiedName, source, SOURCE);
    }

}
