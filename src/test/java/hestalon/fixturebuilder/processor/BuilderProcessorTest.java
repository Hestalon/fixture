package hestalon.fixturebuilder.processor;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static hestalon.fixturebuilder.testutil.Compiler.javac;
import static hestalon.fixturebuilder.testutil.JavaFileObjects.forSourceString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BuilderProcessorTest {

    private JavaFileObject object = null;
    private JavaFileObject fixture = null;

    @BeforeEach
    void setUp() {
        object = forSourceString("HelloWorld", """
            public class HelloWorld {
                private String foo;
                
                public HelloWorld(String foo){
                    this.foo = foo;
                }
            }
            """);
        fixture = forSourceString("HelloWorldFixture", """
            import hestalon.fixturebuilder.*;
                        
            @Fixture
            public interface HelloWorldFixture extends FixtureBuilder<HelloWorld> {
            }
            """);

    }

    @Test
    void returns_false_whithout_any_annotations() {
        var processor = new BuilderProcessor();
        var environment = mock(RoundEnvironment.class);

        var actual = processor.process(Set.of(), environment);

        assertThat(actual)
            .isFalse();
    }

    @Test
    void returns_false_after_processing() {
        var processor = new BuilderProcessor();
        var environment = mock(RoundEnvironment.class);
        var annotation = mock(TypeElement.class);

        var actual = processor.process(Set.of(annotation), environment);

        assertThat(actual)
            .isFalse();
    }

    @Test
    void creates_builder_for_fixtures() {
        var compiler = javac()
            .withProcessors(new BuilderProcessor())
            .build();

        var actual = compiler.compile(List.of(object, fixture));

        assertThat(actual.success())
            .isTrue();
        assertThat(actual.outputFiles())
            .filteredOn(file -> file.getKind() == JavaFileObject.Kind.SOURCE)
            .hasSize(1)
            .extracting(file -> file.getCharContent(true))
            .singleElement()
            .isEqualTo("""
                import hestalon.fixturebuilder.*;
                                
                import javax.annotation.processing.Generated;
                                
                @Generated("hestalon.fixturebuilder.processor.BuilderProcessor")
                public final class HelloWorldFixtureBuilder implements HelloWorldFixture {
                                
                    private String foo = null;
                                
                    @Override
                    public HelloWorld build() {
                        var instance = new HelloWorld(
                            foo
                        );
                        return instance;
                    }
                                
                    public HelloWorldFixtureBuilder useConfig(FixtureBuilderConfig<HelloWorldFixtureBuilder> config) {
                        config.configure(this);
                        return this;
                    }
                                
                    public HelloWorldFixtureBuilder withFoo(String foo) {
                        this.foo = foo;
                        return this;
                    }
                                
                }
                """);
    }
}
