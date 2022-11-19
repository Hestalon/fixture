package hestalon.fixturebuilder.testutil;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class InMemoryJavaFileManager extends ForwardingStandardJavaFileManager {
    private final Map<URI, JavaFileObject> inMemoryOutputs = new HashMap<>();

    InMemoryJavaFileManager(StandardJavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return a.toUri().equals(b.toUri());
    }

    @Override
    public @Nullable FileObject getFileForInput(
        Location location,
        String packageName,
        String relativeName
    ) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public @Nullable JavaFileObject getJavaFileForInput(
        Location location,
        String className,
        JavaFileObject.Kind kind
    ) throws IOException {
        // aparently the location.isModuleOrientedLocation() seems to be buggy implemented
        // => we check it on our own
        if (!location.getName().contains("MODULE")) {
            if (location.isOutputLocation()) {
                var uri = fileUri(location, className, kind);
                return inMemoryOutputs.get(uri);
            }
        }
        return super.getJavaFileForInput(location, className, kind);
    }

    @Override
    public FileObject getFileForOutput(
        Location location,
        String packageName,
        String relativeName,
        FileObject sibling
    ) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
        Location location,
        String className,
        JavaFileObject.Kind kind,
        FileObject sibling
    ) {
        var uri = fileUri(location, className, kind);
        return inMemoryOutputs.computeIfAbsent(
            uri,
            fileUri -> new InMemoryJavaFileObject(fileUri, null, kind)
        );
    }

    private URI fileUri(Location location, String className, JavaFileObject.Kind kind) {
        return InMemoryJavaFileObject.createURI(location, className, kind);
    }

    List<JavaFileObject> getOutputFiles() {
        return List.copyOf(inMemoryOutputs.values());
    }

}
