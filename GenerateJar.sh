#!/bin/bash

javac -d out -cp 'lib/*':'artifacts/JarImplementorTest.jar' java/ru/ifmo/rain/pakulev/implementor/Implementor.java

cd out
jar xf ../artifacts/JarImplementorTest.jar info/kgeorgiy/java/advanced/implementor/Impler.class info/kgeorgiy/java/advanced/implementor/JarImpler.class info/kgeorgiy/java/advanced/implementor/ImplerException.class
jar cfm Implementor.jar ../manifest.txt ru/ifmo/rain/pakulev/implementor/*.class info/kgeorgiy/java/advanced/implementor/*.class