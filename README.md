# FixtureBuilder - Type-safe fixture configuration and building!

## What is FixtureBuilder?

FixtureBuilder is a Java [annotation processor](http://docs.oracle.com/javase/6/docs/technotes/guides/apt/index.html)
for the generation of type-safe configuration and builder for Java classes. It saves the time to adapt the test fixtures
when the production class has been modified.

Advantages:

* No usage of reflection
* Compile-time type safety
* No bleeding of test fixture code in production
* Standalone and chainable configurations of fixtures

Example for FixtureBuilder

```java
// Production
public class Person {

    public Person(int age, String name, String gender) {
        this.age = age;
        this.name = name;
        this.gender = gender;
    }

    private int age;
    private String name;
    private String gender;
    // Setter and Getter
}

// Test
@Fixture
public interface PersonFixture extends FixtureBuilder<Person> {

    FixtureBuilderConfig<PersonFixtureBuilder> HANS = builder -> builder
        .withAge(20)
        .withName("Hans");

}

public class PersonFixtureTest {

    @Test
    public void example() {
        var person = new PersonFixtureBuilder()
            .useConfig(PersonFixture.HANS)
            .withAge(25)
            .withName("John")
            .build();
    }
}
```

