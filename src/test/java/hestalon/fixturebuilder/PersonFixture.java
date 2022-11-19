package hestalon.fixturebuilder;

@Fixture
public interface PersonFixture extends FixtureBuilder<Person> {

    FixtureBuilderConfig<PersonFixtureBuilder> HANS = builder -> builder
        .withAge(20)
        .withName("Hans");

}
