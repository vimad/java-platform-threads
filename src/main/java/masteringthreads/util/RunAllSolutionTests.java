package masteringthreads.util;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestResult;
import junit.textui.TestRunner;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentSkipListSet;

public class RunAllSolutionTests {
    public static void main(String... args) throws IOException {
        var files = new ConcurrentSkipListSet<Path>();
        Files.walkFileTree(Paths.get("src/main/java"), new SimpleFileVisitor<>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.toString();
                if (name.contains("solution") && containsTest(file) && name.matches(".*[/\\\\]ch.*")) {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        System.out.println("We will run the following unit tests:");
        files.forEach(System.out::println);
        System.out.println();
        for (Path file : files) runTest(file);
    }

    private static boolean containsTest(Path file) throws IOException {
        try (var stream = Files.lines(file)) {
            return stream.anyMatch(line ->
                    line.matches("[ \t]*@Test.*") ||
                            line.matches(".*extends AbstractThreadPool.*Tester.*"));
        }
    }

    private static void runTest(Path file) throws IOException {
        try {
            String className = file.toString()
                    .substring("src/main/java/".length());
            className = className.substring(0, className.length() - 5);
            className = className.replaceAll("[/\\\\]", ".");
            Class<?> clazz = Class.forName(className);
            System.out.println("Running test for " + clazz);
            TestResult result = TestRunner.run(new JUnit4TestAdapter(clazz));
            System.out.println("result.errorCount() = " + result.errorCount());
            System.out.println("result.failureCount() = " + result.failureCount());
            if (result.failureCount() > 0 || result.errorCount() > 0) {
                System.err.println("Oh no!!!");
                System.exit(1);
            }
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}