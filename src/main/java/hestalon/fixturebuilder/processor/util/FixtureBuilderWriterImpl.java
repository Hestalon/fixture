package hestalon.fixturebuilder.processor.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.IntStream;

import com.github.mustachejava.DefaultMustacheFactory;
import hestalon.fixturebuilder.processor.model.ClassName;
import hestalon.fixturebuilder.processor.model.FixtureObjectModel;
import hestalon.fixturebuilder.processor.model.FixtureVariableModel;
import lombok.AllArgsConstructor;

import static java.util.function.Predicate.not;

@AllArgsConstructor
public class FixtureBuilderWriterImpl implements FixtureBuilderWriter {

    private final Context context;

    private record MustacheObject(
        String processorClassName,
        boolean defaultPackage,
        String packageName,
        String builderClassName,
        String builderInterfaceName,
        String instanceClass,
        boolean allArgsConstructor,
        List<ClassName> imports,
        List<MustacheVariableObject> variables
    ) {
    }

    private record MustacheVariableObject(
        String builderClassName,
        String camelCaseVariable,
        String pascalCaseVariable,
        String variableClassName,
        String defaultValue,
        boolean last,
        boolean primitive
    ) {
    }

    @Override
    public void write(FixtureObjectModel object, String processorName) throws IOException {
        var mustacheObject = buildMustacheObject(object, processorName);

        var builderFile = context.createSourceFile(object);
        try (var out = new PrintWriter(builderFile.openWriter())) {

            var mf = new DefaultMustacheFactory();
            var mustache = mf.compile("templates/builder.mustache");
            mustache.execute(out, mustacheObject).flush();
        }
    }

    private MustacheObject buildMustacheObject(FixtureObjectModel object, String processorName) {
        var entries = object.variables();
        var variables = IntStream.range(0, entries.size())
            .mapToObj(index -> buildMustacheVariableObject(object, entries, index))
            .toList();

        var packageName = object.builderClass().packageName();

        var variableImports = entries.stream()
            .map(FixtureVariableModel::className)
            .filter(not(ClassName::hasDefaultPackage))
            .filter(not(className -> className.hasPackage(packageName)))
            .toList();

        var mustacheObject = new MustacheObject(
            processorName,
            packageName.isEmpty(),
            packageName,
            object.builderClass().className(),
            object.builderInterface().className(),
            object.instanceClass().className(),
            object.allArgsConstructor(),
            variableImports,
            variables
        );
        return mustacheObject;
    }

    private MustacheVariableObject buildMustacheVariableObject(
        FixtureObjectModel object,
        List<FixtureVariableModel> entries,
        int index
    ) {
        var entry = entries.get(index);
        var originalName = entry.name();
        var firstCharacter = originalName.substring(0, 1);
        var remainingName = originalName.substring(1);

        var pascalCaseVariable = firstCharacter.toUpperCase() + remainingName;
        var camelCaseVariable = firstCharacter.toLowerCase() + remainingName;
        var variableClassName = entry.className().className();

        return new MustacheVariableObject(
            object.builderClass().className(),
            camelCaseVariable,
            pascalCaseVariable,
            variableClassName,
            entry.defaultValue(),
            index == entries.size() - 1,
            entry.primitive()
        );
    }

}
