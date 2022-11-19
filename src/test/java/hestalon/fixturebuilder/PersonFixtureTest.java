package hestalon.fixturebuilder;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonFixtureTest {

    @Test
    public void whenBuildPersonWithBuilder_thenObjectHasPropertyValues() {
        var person = new PersonFixtureBuilder()
            .withAge(25)
            .withName("John")
            .build();

        assertThat(person)
            .isNotNull();
        assertThat(person.getAge())
            .isEqualTo(25);
        assertThat(person.getName())
            .isEqualTo("John");
    }

    @Test
    public void whenBuildPersonWithConfig_thenObjectHasPropertyValues() {
        var person = new PersonFixtureBuilder()
            .useConfig(PersonFixture.HANS)
            .build();

        assertThat(person)
            .isNotNull();
        assertThat(person.getAge())
            .isEqualTo(20);
        assertThat(person.getName())
            .isEqualTo("Hans");
    }

}
