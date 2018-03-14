package ru.ifmo.rain.pakulev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Implementor implements Impler {
    private StringBuilder builder;
    private String pack;
    private String name;
    private Set<Package> packages = new HashSet<>();

    private void getThrowsPart(Class<?>[] e) {
        if (e.length != 0) {
            builder.append(" throws ");
            for (int i = 0; i < e.length - 1; i++) {
                builder.append(e[i].getName());
                builder.append(", ");
            }
            builder.append(e[e.length - 1].getName());
        }
    }

    private String getDefaultValue(Class<?> clazz) {
        switch (clazz.getSimpleName()) {
            case "byte":
                return " 0 ";
            case "short":
                return " 0 ";
            case "int":
                return " 0 ";
            case "long":
                return " 0L ";
            case "float":
                return " 0.0f ";
            case "double":
                return " 0.0d ";
            case "char":
                return " '0' ";
            case "boolean":
                return " false ";
            case "void":
                return " ";
            default:
                return " null ";
        }
    }

    private void getReturnSection(Class<?> returnType) {
        String s = getDefaultValue(returnType);
        if (!s.equals(" ")) {
            builder.append("return ").append(s).append(";");
        }
    }

    private void makeMethod(Method method) {
        builder.append(Modifier.toString(method.getModifiers()).replace("abstract", "")
                .replace("transient", "")).append(" ").append(method.getReturnType()
                .getTypeName()).append(" ").append(method.getName()).append("(");
        Class<?>[] p = method.getParameterTypes();
        for (int i = 0; i < p.length; i++) {
            builder.append(p[i].getSimpleName()).append(" ").append("arg").append(i);
            if (i != p.length - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        getThrowsPart(method.getExceptionTypes());

        builder.append(" {").append("\n");
        getReturnSection(method.getReturnType());
        builder.append("\n").append("}").append("\n");
    }

    private void makeConstructor(Constructor<?> constructor) throws ImplerException {
        builder.append(name).append("(");
        Class<?>[] p = constructor.getParameterTypes();
        for (int i = 0; i < p.length; i++) {
            builder.append(p[i].getSimpleName()).append(" ").append("arg").append(i);
            if (i != p.length - 1) {
                builder.append(", ");
            }
        }
        getThrowsPart(constructor.getExceptionTypes());
        builder.append(" {").append("}").append("\n");
    }

    private void makeInterface(Class<?> aClass) throws ImplerException {
        if (!Modifier.isInterface(aClass.getModifiers())) {
            throw new ImplerException("No interface found");
        }

        if (!pack.isEmpty()) {
            builder.append("package ").append(pack).append(";").append("\n");
        }

        Method[] methods = aClass.getDeclaredMethods();
        Constructor<?>[] constructors = aClass.getDeclaredConstructors();

        for (Method m : methods) {
            Class<?>[] parameters = m.getParameterTypes();
            for (Class<?> p : parameters) {
                packages.add(p.getPackage());
            }
        }

        for (Constructor<?> c : constructors) {
            Class<?>[] parameters = c.getParameterTypes();
            for (Class<?> p : parameters) {
                packages.add(p.getPackage());
            }
        }

        for (Package p : packages) {
            if (p != null && !p.getName().equals("")) {
                builder.append("import ").append(p.getName()).append(".*;").append("\n");
            }
        }

        builder.append("public class ").append(name).append(" ").append("implements ")
                .append(aClass.getName()).append(" {").append("\n");

        for (Constructor<?> c : constructors) {
            makeConstructor(c);
        }

        for (Method m : methods) {
            makeMethod(m);
        }

        builder.append("\n").append("}");
    }


    public void implement(Class<?> aClass, Path path) throws ImplerException {
        builder = new StringBuilder();
        pack = (aClass.getPackage() == null) ? "" : aClass.getPackage().getName();
        name = aClass.getSimpleName() + "Impl";

        File file = new File(path + "/" + pack.replace(".", "/") + "/" + name + ".java");
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
}