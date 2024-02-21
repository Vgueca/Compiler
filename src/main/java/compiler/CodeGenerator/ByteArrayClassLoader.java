package compiler.CodeGenerator;

public class ByteArrayClassLoader extends ClassLoader {
    public Class<?> loadClass(String className, byte[] code) {
        return defineClass(className, code, 0, code.length);
    }
}
