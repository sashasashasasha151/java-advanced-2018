package ru.ifmo.rain.pakulev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;


/**
 * Class implementing {@link JarImpler} interface
 */

public class Implementor implements JarImpler {
    /**
     * {@link StringBuilder} which collect all generated code
     */
    private StringBuilder builder;

    /**
     * {@link String} with package name of generating class
     */
    private String pack;

    /**
     * {@link String} with generating class name
     */
    private String name;

    /**
     * {@link File} of generating class
     */
    private File file;

    /**
     * {@link String} of file separator
     */
    private String sh = File.separator;

    /**
     * Constructor for class {@code Implementor}
     */
    public Implementor() {
    }

    /**
     * Add throws of generated method to {@link #builder}
     *
     * @param exceptions Array of exceptions
     */
    private void makeThrow(Class<?>[] exceptions) {
        if (exceptions.length != 0) {
            builder.append(" throws ");
        }
        for (int i = 0; i < exceptions.length; i++) {
            builder.append(exceptions[i].getName());
            if (i != exceptions.length - 1) {
                builder.append(", ");
            }
        }
    }

    /**
     * Add basic return of generated method to {@link #builder}
     *
     * @param aClass {@link Class} of return value of the method
     */
    private void makeReturn(Class<?> aClass) {
        if (aClass.equals(void.class)) {
            builder.append("");
        } else if (aClass.equals(boolean.class)) {
            builder.append("return true;");
        } else if (aClass.isPrimitive()) {
            builder.append("return 0;");
        } else {
            builder.append("return null;");
        }
    }

    /**
     * Add generated method to {@link #builder}
     *
     * @param method {@link Method} which we need to generate
     */
    private void makeMethod(Method method) {
        builder.append(Modifier.toString(method.getModifiers()).replace("abstract", "")
                .replace("transient", ""))
                .append(" ").append(method.getReturnType().getTypeName())
                .append(" ").append(method.getName()).append("(");
        Class<?>[] p = method.getParameterTypes();
        for (int i = 0; i < p.length; i++) {
            builder.append(p[i].getCanonicalName()).append(" arg").append(i);
            if (i != p.length - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        makeThrow(method.getExceptionTypes());
        builder.append(" {\n");
        makeReturn(method.getReturnType());
        builder.append("\n}\n");
    }

    /**
     * Generate class and add collect it to {@link #builder}
     *
     * @param aClass {@link Class} which we need to generate
     * @throws ImplerException if given class is not an interface
     */
    private void makeInterface(Class<?> aClass) throws ImplerException {
        if (!Modifier.isInterface(aClass.getModifiers())) {
            throw new ImplerException("No interface found");
        }

        if (!pack.isEmpty()) {
            builder.append("package ").append(pack).append(";\n");
        }

        Method[] methods = aClass.getMethods();

        builder.append("public class ").append(name).append(" implements ").append(aClass.getName()).append(" {\n");

        for (Method m : methods) {
            makeMethod(m);
        }

        builder.append("\n}");
    }


    /**
     * Generate class and write it to java file
     *
     * @param aClass {@link Class} which we need to generate
     * @param path   {@link Path} where should be located generated class
     * @throws ImplerException if given class cannot be generated
     */
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        builder = new StringBuilder();
        pack = (aClass.getPackage() == null) ? "" : aClass.getPackage().getName();
        name = aClass.getSimpleName() + "Impl";

        file = new File(path + sh + pack.replace(".", sh) + sh + name + ".java");
        file.getParentFile().mkdirs();

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        makeInterface(aClass);

        try (PrintWriter out = new PrintWriter(file)) {
            out.print(builder);
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Generate class and put it to .jar file
     *
     * @param aClass {@link Class} which we need to generate
     * @param path   {@link Path} where should be located .jar file
     * @throws ImplerException if given class cannot be generated
     */
    public void implementJar(Class<?> aClass, Path path) throws ImplerException {
        Path tmp = Paths.get("." + sh + "tmp");
        implement(aClass, tmp);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, file.getPath());
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream jOut = new JarOutputStream(Files.newOutputStream(path), manifest)) {
            File compiledClass = new File(file.getPath().replace(".java", ".class"));
            String temp = aClass.getName().replace('.', '/') + "Impl.class";
            jOut.putNextEntry(new ZipEntry(temp));
            Files.copy(Paths.get(compiledClass.getPath()), jOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start app from command line
     * @param args arguments of command line
     */
    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Arguments couldn't be null");
            return;
        }

        if (args.length < 2 || args.length > 3) {
            System.err.println("Only two or three arguments allowed");
            return;
        }

        Implementor implementor = new Implementor();

        try {
            if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            }
            if (args.length == 3) {
                if (args[0].equals("-jar")) {
                    implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
                } else {
                    System.err.println("No -jar argument found");
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println(((args.length == 2) ? args[0] : args[1]) + " class not found");
        } catch (InvalidPathException e) {
            System.out.println(((args.length == 2) ? args[1] : args[2]) + " path is invalid");
        } catch (ImplerException e) {
            System.out.println(e.getMessage());
        }
    }
}