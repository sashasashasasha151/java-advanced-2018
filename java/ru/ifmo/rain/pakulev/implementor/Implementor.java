package ru.ifmo.rain.pakulev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;



public class Implementor implements Impler, JarImpler {
    /**
     * Object that create all implementing class
     */
    private StringBuilder builder;

    /**
     * String of the class package
     */
    private String pack;

    /**
     * String of the class name
     */
    private String name;

    /**
     * File that signifying class
     */
    private File file;

    /**
     * String of file separator
     */
    private String sh = File.separator;

    /**
     * Add throws to your stringbuilder object
     *
     * @param e Array of exceptions
     */
    private void makeThrow(Class<?>[] e) {
        if (e.length != 0) {
            builder.append(" throws ");
        }
        for (int i = 0; i < e.length; i++) {
            builder.append(e[i].getName());
            if (i != e.length - 1) {
                builder.append(", ");
            }
        }
    }

    /**
     * Add returns to your stringbuilder object
     *
     * @param aClass Your class
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
     * Add returns to your stringbuilder object
     *
     * @param method One of methods
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
     * Build class
     *
     * @param aClass Your class
     * @throws ImplerException exception of Implementor
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
     * Make class and write it to java file
     *
     * @param aClass Your class
     * @param path   Path to set your file
     * @throws ImplerException if there is no accessible constructor in superclass or interface
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
     * Implements class by given type-token and put it to .jar file
     *
     * @param aClass Your class
     * @param path   Path to your jar file
     * @throws ImplerException Exception of implementJar
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
}