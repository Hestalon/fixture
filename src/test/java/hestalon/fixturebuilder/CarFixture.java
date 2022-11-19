package hestalon.fixturebuilder;

@Fixture
@FixtureDefault(source = "name", value = "\"foobar\"")
public interface CarFixture extends FixtureBuilder<Car> {
}
