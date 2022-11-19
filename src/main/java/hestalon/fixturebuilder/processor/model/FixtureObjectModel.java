package hestalon.fixturebuilder.processor.model;

import java.util.List;

public record FixtureObjectModel(
    ClassName builderInterface,
    ClassName builderClass,
    ClassName instanceClass,
    boolean allArgsConstructor,
    List<FixtureVariableModel> variables
) {
}
