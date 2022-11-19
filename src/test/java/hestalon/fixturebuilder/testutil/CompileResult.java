package hestalon.fixturebuilder.testutil;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public record CompileResult(
    boolean success,
    List<Diagnostic<? extends JavaFileObject>> diagnostics,
    List<JavaFileObject> outputFiles
) {
}
