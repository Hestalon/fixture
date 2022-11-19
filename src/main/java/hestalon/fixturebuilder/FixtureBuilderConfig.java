package hestalon.fixturebuilder;

@FunctionalInterface
public interface FixtureBuilderConfig<T extends FixtureBuilder<?>> {

    void configure(T builder);

}
