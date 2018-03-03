package ru.ifmo.rain.pakulev.student;

import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.stream.Collectors;

public class StudentDB implements StudentQuery {

    private Comparator<Student> comparator = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .thenComparing(Student::getId);

    public List<String> getFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toList());
    }

    public List<String> getLastNames(List<Student> students) {
        return students.stream()
                .map(Student::getLastName)
                .collect(Collectors.toList());
    }

    public List<String> getGroups(List<Student> students) {
        return students.stream()
                .map(Student::getGroup)
                .collect(Collectors.toList());
    }

    public List<String> getFullNames(List<Student> students) {
        return students.stream()
                .map(student -> student.getFirstName() + " " + student.getLastName())
                .collect(Collectors.toList());
    }

    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toSet());
    }

    public String getMinStudentFirstName(List<Student> students) {
        return students.stream()
                .min(Comparator.comparing(Student::getId))
                .get()
                .getFirstName();
    }

    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream()
                .sorted(Comparator.comparing(Student::getId))
                .collect(Collectors.toList());
    }

    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return students.stream()
                .filter(student -> student.getFirstName().equals(name))
                .collect(Collectors.toList());
    }

    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return students.stream()
                .filter(student -> student.getLastName().equals(name))
                .collect(Collectors.toList());
    }

    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .sorted(comparator)
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, (a, b) -> a));
    }
}