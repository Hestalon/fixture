package hestalon.fixturebuilder.processor.util;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import hestalon.fixturebuilder.Fixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FixtureBuilderParserImplTest {

    @ParameterizedTest
    @EnumSource(value = ElementKind.class, mode = EnumSource.Mode.EXCLUDE, names = "INTERFACE")
    void only_interfaces_allowed(ElementKind kind) {
        var context = mock(Context.class);
        var parser = new FixtureBuilderParserImpl(context);

        var element = mock(Element.class);
        doReturn(kind)
            .when(element)
            .getKind();

        parser.parse(element);

        verify(context)
            .logError(
                eq(element),
                any(),
                eq(ElementKind.INTERFACE),
                eq(Fixture.class.getSimpleName())
            );
    }

    @ParameterizedTest
    @EnumSource(value = ElementKind.class, mode = EnumSource.Mode.EXCLUDE, names = "PACKAGE")
    void enclosed_element_must_be_the_package(ElementKind kind) {
        var context = mock(Context.class);
        var parser = new FixtureBuilderParserImpl(context);

        var element = mock(Element.class);
        doReturn(ElementKind.INTERFACE)
            .when(element)
            .getKind();
        var enclosingElement = mock(Element.class);
        doReturn(enclosingElement)
            .when(element)
            .getEnclosingElement();
        doReturn(kind)
            .when(enclosingElement)
            .getKind();

        parser.parse(element);

        verify(context)
            .logError(
                eq(element),
                any(),
                eq(Fixture.class.getSimpleName())
            );
    }

    @Test
    void only_one_implemented_interface_allowed() {
        var context = mock(Context.class);
        var parser = new FixtureBuilderParserImpl(context);

        var enclosingElement = mock(Element.class);
        doReturn(ElementKind.PACKAGE)
            .when(enclosingElement)
            .getKind();
        var element = mock(TypeElement.class);
        doReturn(ElementKind.INTERFACE)
            .when(element)
            .getKind();
        doReturn(enclosingElement)
            .when(element)
            .getEnclosingElement();
        doReturn(List.of(mock(TypeMirror.class), mock(TypeMirror.class)))
            .when(element)
            .getInterfaces();

        parser.parse(element);

        verify(context)
            .logError(
                eq(element),
                any()
            );
    }

}
