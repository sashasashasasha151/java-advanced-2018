//package ru.ifmo.rain.pakulev.student;
//
//import info.kgeorgiy.java.advanced.student.Student;
//import info.kgeorgiy.java.advanced.student.StudentQuery;
//
//import java.util.*;
//import java.util.function.Function;
//import java.util.function.Predicate;
//import java.util.stream.Collectors;
//
//public class StudentDB implements StudentQuery {
//
//    private Comparator<Student> comparator = Comparator.comparing(Student::getLastName)
//            .thenComparing(Student::getFirstName)
//            .thenComparing(Student::getId);
//
//    private List<String> abFunc(List<Student> students, Function<Student, String> f) {
//        return students.stream()
//                .map(f)
//                .collect(Collectors.toList());
//    }
//
//    private List<Student> abSortedFunc(Collection<Student> students, Predicate<Student> f) {
//        return students.stream()
//                .sorted(comparator)
//                .filter(f)
//                .collect(Collectors.toList());
//    }
//
//    public List<String> getFirstNames(List<Student> students) {
//        return abFunc(students, Student::getFirstName);
//    }
//
//    public List<String> getLastNames(List<Student> students) {
//        return abFunc(students, Student::getLastName);
//    }
//
//    public List<String> getGroups(List<Student> students) {
//        return abFunc(students, Student::getGroup);
//    }
//
//    public List<String> getFullNames(List<Student> students) {
//        return abFunc(students, student -> student.getFirstName() + " " + student.getLastName());
//    }
//
//    public Set<String> getDistinctFirstNames(List<Student> students) {
//        return students.stream()
//                .map(Student::getFirstName)
//                .collect(Collectors.toCollection(TreeSet::new));
//    }
//
//    public String getMinStudentFirstName(List<Student> students) {
//        return students.stream()
//                .min(Comparator.comparing(Student::getId))
//                .map(Student::getFirstName).orElse("");
//    }
//
//    public List<Student> sortStudentsById(Collection<Student> students) {
//        return students.stream()
//                .sorted(Comparator.comparing(Student::getId))
//                .collect(Collectors.toList());
//    }
//
//    public List<Student> sortStudentsByName(Collection<Student> students) {
//        return students.stream()
//                .sorted(comparator)
//                .collect(Collectors.toList());
//    }
//
//    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
//        return abSortedFunc(students, student -> student.getFirstName().equals(name));
//    }
//
//    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
//        return abSortedFunc(students, student -> student.getLastName().equals(name));
//    }
//
//    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
//        return abSortedFunc(students, student -> student.getGroup().equals(group));
//    }
//
//    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
//        return students.stream()
//                .filter(student -> student.getGroup().equals(group))
//                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, (a, b) -> a.compareTo(b) < 0 ? a : b));
//    }
//}