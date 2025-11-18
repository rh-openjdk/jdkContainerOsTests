//17 and up, originally by mizdebsk@redhat.com.

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.net.URI;
import java.util.*;

public class InProcessCompileDemo {
    public static void main(String[] args) throws Exception {
        String version = System.getProperty("java.version");
        String jvmVersion = version.split("\\.")[0];
        if (args.length>0) { 
           jvmVersion=args[0];
        }
        String className = "Hello";
        String sourceCode = """
            public class Hello {
                public static void main(String[] args) {
                    System.out.println("Hello from in-memory compiled code!");
                }
            }
            """;

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("❌ No system compiler found — you're probably on a stripped JRE.");
            return;
        }
        System.out.println("✅ Compiler loaded: " + compiler.getClass().getName());

        // in-memory source
        JavaFileObject source = new StringJavaFileObject(className, sourceCode);

        // file manager that captures class bytes in memory
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager std = compiler.getStandardFileManager(diagnostics, null, null);
        MemoryFileManager memFM = new MemoryFileManager(std);

        // compile for a specific release
        JavaCompiler.CompilationTask task = compiler.getTask(
                null, memFM, diagnostics,
                //List.of("--release", jvmVersion),
                 new ArrayList<>(),
                 null, List.of(source));

        boolean ok = task.call();
        diagnostics.getDiagnostics().forEach(System.out::println);

        if (!ok) {
            System.out.println("❌ Compilation failed");
            return;
        }
        System.out.println("✅ Compilation succeeded");

        // load and run main()
        ClassLoader loader = new MemoryClassLoader(memFM.getClassBytes());
        Class<?> hello = loader.loadClass(className);
        hello.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
    }

    // ===== Helpers =====

    // Source stored entirely in memory
    static final class StringJavaFileObject extends SimpleJavaFileObject {
        private final String code;
        StringJavaFileObject(String className, String code) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }
        @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) { return code; }
    }

    // Captures compiled class bytes in memory
    static final class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, ByteArrayJavaFileObject> classFiles = new HashMap<>();
        MemoryFileManager(StandardJavaFileManager fileManager) { super(fileManager); }
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) {
            ByteArrayJavaFileObject file = new ByteArrayJavaFileObject(className, kind);
            classFiles.put(className, file);
            return file;
        }
        Map<String, byte[]> getClassBytes() {
            Map<String, byte[]> out = new HashMap<>();
            for (var e : classFiles.entrySet()) out.put(e.getKey(), e.getValue().getBytes());
            return out;
        }
    }

    // Holds class bytes
    static final class ByteArrayJavaFileObject extends SimpleJavaFileObject {
        private final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        ByteArrayJavaFileObject(String name, Kind kind) {
            super(URI.create("mem:///" + name.replace('.', '/') + kind.extension), kind);
        }
        @Override public java.io.OutputStream openOutputStream() { return out; }
        byte[] getBytes() { return out.toByteArray(); }
    }

    // ClassLoader that defines classes from captured bytes
    static final class MemoryClassLoader extends ClassLoader {
        private final Map<String, byte[]> classes;
        MemoryClassLoader(Map<String, byte[]> classes) {
            super(InProcessCompileDemo.class.getClassLoader());
            this.classes = classes;
        }
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classes.get(name);
            if (bytes == null) throw new ClassNotFoundException(name);
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
