package hestalon.fixturebuilder.processor.util;

import java.io.IOException;

import hestalon.fixturebuilder.processor.model.FixtureObjectModel;

public interface FixtureBuilderWriter {
    void write(FixtureObjectModel object, String processorName) throws IOException;
}
