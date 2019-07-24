package ru.abagiev.examples.spring.intercept.repo;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.abagiev.examples.spring.intercept.model.Binding;
import ru.abagiev.examples.spring.intercept.model.Book;
import ru.abagiev.examples.spring.intercept.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class LibraryRepository {
    private Map<Long, Book> bookMap = new ConcurrentHashMap<>();
    private AtomicLong bookSequence = new AtomicLong(0);

    private Map<Long, Student> studentMap = new ConcurrentHashMap<>();
    private AtomicLong studentSequence = new AtomicLong(0);

    private Set<Binding> bindingSet = ConcurrentHashMap.newKeySet();

    public Book addBook(String title, String author) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setId(bookSequence.incrementAndGet());

        bookMap.put(book.getId(), book);
        return book;
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(bookMap.values());
    }

    public Book getBook(long bookId) {
        return bookMap.get(bookId);
    }

    public void removeBook(long bookId) {
        bookMap.remove(bookId);
    }

    public Student addStudent(String firstName, String lastName) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setId(studentSequence.incrementAndGet());

        studentMap.put(student.getId(), student);
        return student;
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(studentMap.values());
    }

    public Student getStudent(long studentId) {
        return studentMap.get(studentId);
    }

    public void removeStudent(long studentId) {
        studentMap.remove(studentId);
    }

    public void bind(long studentId, long bookId) {
        Binding binding = new Binding();
        binding.setStudentId(studentId);
        binding.setBookId(bookId);

        bindingSet.add(binding);
    }

    public void unbind(long studentId, long bookId) {
        Binding binding = new Binding();
        binding.setStudentId(studentId);
        binding.setBookId(bookId);

        bindingSet.remove(binding);
    }

    public boolean isBound(long studentId, long bookId) {
        Binding binding = new Binding();
        binding.setStudentId(studentId);
        binding.setBookId(bookId);

        return bindingSet.contains(binding);
    }

    public List<Book> getBoundBooks(long studentId) {
        return bindingSet.stream()
                .filter(b -> b.getStudentId() == studentId)
                .map(b -> b.getBookId())
                .map(id -> bookMap.get(id))
                .collect(Collectors.toList());
    }
}
