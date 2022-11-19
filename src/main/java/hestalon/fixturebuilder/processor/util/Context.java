package hestalon.fixturebuilder.processor.util;

import java.io.IOException;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import hestalon.fixturebuilder.processor.model.FixtureObjectModel;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequiredArgsConstructor
public class Context {
    private final ProcessingEnvironment env;
    private final List<FixtureObjectModel> fixtureObjects;

    public void addFixtureObject(FixtureObjectModel object) {
        fixtureObjects.add(object);
    }

    public JavaFileObject createSourceFile(FixtureObjectModel object) throws IOException {
        var fqcn = object.builderClass().fqcn();

        var result = env.getFiler()
            .createSourceFile(fqcn);

        return result;
    }

    public void logError(@Nullable Element element, String message, Object... args) {
        env.getMessager()
            .printMessage(Diagnostic.Kind.ERROR, message.formatted(args), element);
    }

    public List<FixtureObjectModel> fixtureObjects() {
        return fixtureObjects;
    }

}
