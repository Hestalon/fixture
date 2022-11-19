package hestalon.fixturebuilder.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.google.auto.service.AutoService;
import hestalon.fixturebuilder.Fixture;
import hestalon.fixturebuilder.processor.util.Context;
import hestalon.fixturebuilder.processor.util.FixtureBuilderParser;
import hestalon.fixturebuilder.processor.util.FixtureBuilderParserImpl;
import hestalon.fixturebuilder.processor.util.FixtureBuilderWriter;
import hestalon.fixturebuilder.processor.util.FixtureBuilderWriterImpl;

@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {

    private static final Boolean ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS = Boolean.FALSE;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        var supportedTypes = Set.of(Fixture.class.getName());
        return supportedTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver() || annotations.isEmpty()) {
            return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
        }

        var context = new Context(processingEnv, new ArrayList<>());

        try {
            analyse(annotations, roundEnv, context);
        } catch (Exception e) {
            context.logError(null, exceptionToString(e));
        }

        return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
    }

    public String exceptionToString(Exception exception) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        var result = sw.toString();

        return result;
    }

    private void analyse(
        Set<? extends TypeElement> annotations,
        RoundEnvironment roundEnv,
        Context context
    ) throws IOException {
        var parser = createParser(context);
        var writer = createWriter(context);

        for (var annotation : annotations) {
            var annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (var element : annotatedElements) {
                parser.parse(element);
            }
        }

        for (var fixtureObject : context.fixtureObjects()) {
            writer.write(fixtureObject, getClass().getName());
        }
    }

    FixtureBuilderWriter createWriter(Context context) {
        return new FixtureBuilderWriterImpl(context);
    }

    FixtureBuilderParser createParser(Context context) {
        var parser = new FixtureBuilderParserImpl(context);
        return parser;
    }

}
