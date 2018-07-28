package cucumber.runtime;

import java.io.PrintStream;
import java.util.List;

import cucumber.api.SummaryPrinter;

public class CucumberSummaryPrinter implements SummaryPrinter
{
    private final PrintStream out;
    
    public CucumberSummaryPrinter( PrintStream out )
    {
        this.out = out;
    }
    
    @Override
    public void print( Runtime runtime) {
        out.println();
        printStats(runtime);
        out.println();
        printErrors(runtime);
        printSnippets(runtime);
        out.flush();
    }

    private void printStats( Runtime runtime) {
        runtime.printStats( out );
    }

    private void printErrors( Runtime runtime) {
        for (Throwable error : runtime.getErrors()) {
            error.printStackTrace(out);
            out.println();
        }
    }

    private void printSnippets( Runtime runtime) {
        List<String> snippets = runtime.getSnippets();
        if (!snippets.isEmpty()) {
            out.append("\n");
            out.println("You can implement missing steps with the snippets below:");
            out.println();
            for (String snippet : snippets) {
                out.println(snippet);
            }
        }
    }
}
