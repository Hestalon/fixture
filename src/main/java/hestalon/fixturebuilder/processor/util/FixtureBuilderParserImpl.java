package hestalon.fixturebuilder.processor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import hestalon.fixturebuilder.Fixture;
import hestalon.fixturebuilder.FixtureBuilder;
import hestalon.fixturebuilder.FixtureDefault;
import hestalon.fixturebuilder.processor.model.ClassName;
import hestalon.fixturebuilder.processor.model.FixtureObjectModel;
import hestalon.fixturebuilder.processor.model.FixtureVariableModel;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FixtureBuilderParserImpl implements FixtureBuilderParser {

    private static final String DEFAULT_FIELD_VALUE = "null";

    //	https://medium.com/@jintin/annotation-processing-in-java-3621cb05343a
    //  https://github.com/gunnarmorling/awesome-annotation-processing
    //  https://github.com/six2six/fixture-factory
    private final Context context;

    @Override
    public void parse(Element annotatedElement) {
        validateElement(annotatedElement)
            .ifPresent(this::handleValidatedElement);
    }

    private record ValidationResult(
        Element annotatedElement,
        TypeMirror fixtureType,
        List<? extends Element> fixtureElements,
        TypeMirror builderInterfaceType
    ) {
    }

    private Optional<ValidationResult> validateElement(Element annotatedElement) {
        if (annotatedElement.getKind() != ElementKind.INTERFACE) {
            context.logError(
                annotatedElement,
                "must be of type %s to be annotated with @%s",
                ElementKind.INTERFACE,
                Fixture.class.getSimpleName()
            );
            return Optional.empty();
        }

        if (annotatedElement.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            context.logError(
                annotatedElement,
                "must be a top level interface to be annotated with @%s",
                Fixture.class.getSimpleName()
            );
            return Optional.empty();
        }

        var builderInterface = (TypeElement) annotatedElement;
        var implementedInterfaces = builderInterface.getInterfaces();
        if (implementedInterfaces.size() != 1) {
            context.logError(
                annotatedElement,
                "only one interface is allowed to be implemented"
            );
            return Optional.empty();
        }

        var implementedInterface = (DeclaredType) implementedInterfaces.get(0);
        var implementedType = (TypeElement) implementedInterface.asElement();

        var isFixtureBase = implementedType.getQualifiedName()
            .contentEquals(FixtureBuilder.class.getName());

        if (!isFixtureBase) {
            context.logError(
                implementedType,
                "only %s is allowed to extend",
                FixtureBuilder.class.getSimpleName()
            );
            return Optional.empty();
        }

        var interfaceArguments = implementedInterface.getTypeArguments();
        if (interfaceArguments.size() != 1) {
            context.logError(
                implementedType,
                "only one type argument expected for %s but found %s",
                FixtureBuilder.class.getSimpleName(),
                interfaceArguments.size()
            );
            return Optional.empty();
        }

        var fixtureType = interfaceArguments.get(0);
        return Optional.of(new ValidationResult(
            annotatedElement,
            fixtureType,
            ((DeclaredType) fixtureType).asElement().getEnclosedElements(),
            builderInterface.asType()
        ));
    }

    private void handleValidatedElement(ValidationResult validationResult) {
        var fixtureElements = validationResult.fixtureElements;

        var variableElements = fixtureElements.stream()
            .filter(element -> element.getKind() == ElementKind.FIELD)
            .map(element -> (VariableElement) element)
            .toList();

        var constructors = fixtureElements.stream()
            .filter(this::isPublicConstructor)
            .map(element -> (ExecutableElement) element)
            .toList();

        var noArgsConstructor = constructors.stream()
            .filter(constructor -> constructor.getParameters().isEmpty())
            .findAny();

        var allArgsConstructor = constructors.stream()
            .filter(constructor -> constructor.getParameters().size() == variableElements.size())
            .findAny();

        if (noArgsConstructor.isEmpty() && allArgsConstructor.isEmpty()) {
            context.logError(
                validationResult.annotatedElement,
                "no args or all args constructor required, but none found"
            );
            return;
        }

        var annotation = validationResult.annotatedElement.getAnnotation(Fixture.class);

        var variables = allArgsConstructor
            .map(constructor -> buildAllArgsVariables(variableElements, constructor))
            .orElseGet(() -> buildNoArgsVariables(variableElements, fixtureElements));
        if (variables.isEmpty()) {
            return;
        }

        var variableModels = buildVariables(validationResult, variables.get());
        if (variableModels.isEmpty()) {
            return;
        }

        var builderInterfaceName = buildClassName(validationResult.builderInterfaceType);
        var builderName = new ClassName(
            builderInterfaceName.packageName(),
            builderInterfaceName.className() + annotation.builderSuffix()
        );
        var instanceName = buildClassName(validationResult.fixtureType);

        context.addFixtureObject(new FixtureObjectModel(
            builderInterfaceName,
            builderName,
            instanceName,
            allArgsConstructor.isPresent(),
            variableModels.get()
        ));
    }

    private Optional<List<VariableElement>> buildAllArgsVariables(
        List<VariableElement> variableElements,
        ExecutableElement constructor
    ) {
        var variables = new ArrayList<VariableElement>();
        for (VariableElement variable : constructor.getParameters()) {
            var allArgsParameterNameMismatch = variableElements.stream()
                .noneMatch(fixtureField -> fixtureField.getSimpleName().contentEquals(variable.getSimpleName()));
            if (allArgsParameterNameMismatch) {
                context.logError(
                    variable,
                    "no field for constructor param %s found",
                    variable.getSimpleName()
                );
                return Optional.empty();
            }
            variables.add(variable);
        }

        return Optional.of(variables);
    }

    private Optional<List<VariableElement>> buildNoArgsVariables(
        List<VariableElement> variableElements,
        List<? extends Element> fixtureElements
    ) {
        var variables = variableElements.stream()
            .filter(variable -> fixtureElements.stream().anyMatch(element -> isSetter(element, variable)))
            .toList();

        return Optional.of(variables);
    }

    private Optional<List<FixtureVariableModel>> buildVariables(
        ValidationResult validationResult,
        List<VariableElement> variables
    ) {
        var defaults = validationResult.annotatedElement.getAnnotationsByType(FixtureDefault.class);
        for (var objectDefault : defaults) {
            var source = objectDefault.source();
            var unkownSource = variables.stream()
                .noneMatch(variable -> variable.getSimpleName().contentEquals(source));
            if (unkownSource) {
                context.logError(
                    validationResult.annotatedElement,
                    "field %s does not exist, can't set default",
                    source
                );
                return Optional.empty();
            }
        }

        var defaultMap = Arrays.stream(defaults)
            .collect(Collectors.toMap(FixtureDefault::source, FixtureDefault::value));

        var result = variables.stream()
            .map(variable -> {
                var variableName = variable.toString();
                var defaultValue = defaultMap.getOrDefault(variableName, DEFAULT_FIELD_VALUE);
                var variableType = variable.asType();
                return new FixtureVariableModel(
                    buildClassName(variableType),
                    variable.toString(),
                    defaultValue,
                    variableType.getKind().isPrimitive()
                );
            }).toList();

        return Optional.of(result);
    }

    private boolean isPublicConstructor(Element element) {
        if (element.getKind() != ElementKind.CONSTRUCTOR) {
            return false;
        }
        return element.getModifiers().contains(Modifier.PUBLIC);
    }

    private boolean isSetter(Element method, Element variable) {
        if (method.getKind() != ElementKind.METHOD) {
            return false;
        }
        if (!method.getModifiers().contains(Modifier.PUBLIC)) {
            return false;
        }
        var parameterTypes = ((ExecutableType) method.asType())
            .getParameterTypes();
        if (parameterTypes.size() != 1) {
            return false;
        }
        var parameterType = parameterTypes.get(0);
        if (!parameterType.equals(variable.asType())) {
            return false;
        }

        var methodName = method.getSimpleName().toString();
        return methodName.equalsIgnoreCase("set" + variable.getSimpleName().toString());
    }

    private ClassName buildClassName(TypeMirror typeMirror) {
        var rawString = typeMirror.toString();
        var dotPosition = rawString.lastIndexOf('.');

        if (dotPosition == -1) {
            return new ClassName("", rawString);
        }

        var packageName = rawString.substring(0, dotPosition);
        var className = rawString.substring(dotPosition + 1);

        var optimizedPackageName = packageName.equals("java.lang")
            ? ""
            : packageName;

        return new ClassName(optimizedPackageName, className);
    }

}
