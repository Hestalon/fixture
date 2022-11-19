package hestalon.fixturebuilder.processor.model;

public record FixtureVariableModel(
    ClassName className,
    String name,
    String defaultValue,
    boolean primitive
) {
}
