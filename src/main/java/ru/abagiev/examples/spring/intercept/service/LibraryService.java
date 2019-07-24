package ru.abagiev.examples.spring.intercept.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abagiev.examples.spring.intercept.model.Book;
import ru.abagiev.examples.spring.intercept.model.Student;
import ru.abagiev.examples.spring.intercept.repo.LibraryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryRepository repo;

    public Mono<Book> addBook(String title, String author) {
        return Mono.fromCallable(() -> {
            return repo.addBook(title, author);
        });
    }

    public Mono<List<Book>> getAllBooks() {
        return Mono.fromCallable(() -> {
            return repo.getAllBooks();
        });
    }

    public Mono<Book> getBook(long id) {
        return Mono.fromCallable(() -> {
            Book book = repo.getBook(id);
            if (book == null) {
                throw new RuntimeException("Book is not found by ID " + id);
            }
            return book;
        });
    }

    public Mono<Void> removeBook(long id) {
        return Mono.fromRunnable(() -> {
            if (repo.getBook(id) == null) {
                throw new RuntimeException("Book is not found by ID " + id);
            }
            repo.removeBook(id);
        });
    }

    public Mono<Student> addStudent(String firstName, String lastName) {
        return Mono.fromCallable(() -> {
            return repo.addStudent(firstName, lastName);
        });
    }

    public Mono<List<Student>> getAllStudents() {
        return Mono.fromCallable(() -> {
            return repo.getAllStudents();
        });
    }

    public Mono<Student> getStudent(long id) {
        return Mono.fromCallable(() -> {
            Student student = repo.getStudent(id);
            if (student == null) {
                throw new RuntimeException("Student is not found by ID " + id);
            }
            return student;
        });
    }

    public Mono<Void> removeStudent(long id) {
        return Mono.fromRunnable(() -> {
            if (repo.getStudent(id) == null) {
                throw new RuntimeException("Student is not found by ID " + id);
            }
            repo.removeStudent(id);
        });
    }

    public Mono<Void> bind(long studentId, long bookId) {
        return Mono.fromRunnable(() -> {
            if (repo.getStudent(studentId) == null) {
                throw new RuntimeException("Student is not found by ID " + studentId);
            }
            if (repo.getBook(bookId) == null) {
                throw new RuntimeException("Book is not found by ID " + bookId);
            }
            repo.bind(studentId, bookId);
        });
    }

    public Mono<Void> unbind(long studentId, long bookId) {
        return Mono.fromRunnable(() -> {
            if (repo.getStudent(studentId) == null) {
                throw new RuntimeException("Student is not found by ID " + studentId);
            }
            if (repo.getBook(bookId) == null) {
                throw new RuntimeException("Book is not found by ID " + bookId);
            }
            repo.unbind(studentId, bookId);
        });
    }

    public Mono<Boolean> isBound(long studentId, long bookId) {
        return Mono.fromCallable(() -> {
            if (repo.getStudent(studentId) == null) {
                throw new RuntimeException("Student is not found by ID " + studentId);
            }
            if (repo.getBook(bookId) == null) {
                throw new RuntimeException("Book is not found by ID " + bookId);
            }
            return repo.isBound(studentId, bookId);
        });
    }

    public Mono<List<Book>> getBoundBooks(long studentId) {
        return Mono.fromCallable(() -> {
            if (repo.getStudent(studentId) == null) {
                throw new RuntimeException("Student is not found by ID " + studentId);
            }
            return repo.getBoundBooks(studentId);
        });
    }
}
