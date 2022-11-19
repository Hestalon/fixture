package hestalon.fixturebuilder.testutil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import org.checkerframework.checker.nullness.qual.Nullable;

final class InMemoryJavaFileObject extends SimpleJavaFileObject implements JavaFileObject {
    private long lastModified = 0L;
    private @Nullable String source = null;

    static URI createURI(
        JavaFileManager.Location location,
        String className,
        JavaFileObject.Kind kind
    ) {
        var fileUri = URI.create(className.replace('.', '/') + kind.extension);
        var result = URI.create("mem:/" + location.getName())
            .resolve(fileUri);
        return result;
    }

    InMemoryJavaFileObject(
        JavaFileManager.Location location,
        String fullyQualifiedName,
        @Nullable String source,
        Kind kind
    ) {
        this(createURI(location, fullyQualifiedName, kind), source, kind);
    }

    InMemoryJavaFileObject(URI uri, @Nullable String source, Kind kind) {
        super(uri, kind);
        this.source = source;
        lastModified = System.currentTimeMillis();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        if (source != null) {
            return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
        }
        throw new FileNotFoundException();
    }

    @Override
    public OutputStream openOutputStream() {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                source = toString();
                lastModified = System.currentTimeMillis();
            }
        };
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        if (source != null) {
            return new StringReader(source);
        }
        throw new FileNotFoundException();
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
        throws IOException {
        if (source != null) {
            return source;
        }
        throw new FileNotFoundException();
    }

    @Override
    public Writer openWriter() {
        return new StringWriter() {
            @Override
            public void close() throws IOException {
                super.close();
                source = toString();
                lastModified = System.currentTimeMillis();
            }
        };
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public boolean delete() {
        source = null;
        lastModified = 0L;
        return true;
    }

}
