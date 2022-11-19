package hestalon.fixturebuilder.testutil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.processing.Processor;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

public record Compiler(
    JavaCompiler compiler,
    List<Processor> processors,
    List<String> options
) {

    public static Builder javac() {
        return new Builder();
    }

    public CompileResult compile(Iterable<? extends JavaFileObject> files) {
        var diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
        var standardFileManager = compiler.getStandardFileManager(
            diagnosticCollector,
            Locale.getDefault(),
            StandardCharsets.UTF_8
        );
        var fileManager = new InMemoryJavaFileManager(standardFileManager);
        var task = compiler.getTask(
            null,
            fileManager,
            diagnosticCollector,
            options,
            List.of(),
            files
        );

        task.setProcessors(processors);
        var success = task.call();
        var diagnosticResult = diagnosticCollector.getDiagnostics();

        return new CompileResult(
            success,
            diagnosticResult,
            fileManager.getOutputFiles()
        );
    }

    public static class Builder {

        private Builder() {
        }

        private List<Processor> processors = new ArrayList<>();

        public Builder withProcessors(Processor... processor) {
            processors = List.of(processor);
            return this;
        }

        public Compiler build() {
            return new Compiler(
                ToolProvider.getSystemJavaCompiler(),
                List.copyOf(processors),
                List.of()
            );
        }
    }
}
