package ru.abagiev.examples.spring.intercept.rest;

import lombok.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.abagiev.examples.spring.intercept.model.Book;
import ru.abagiev.examples.spring.intercept.model.Student;
import ru.abagiev.examples.spring.intercept.service.LibraryService;

import java.util.List;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {

    private final LibraryService service;

    @PostMapping("add")
    public Mono<Book> add(@RequestBody AddDto dto) {
        return service.addBook(dto.getTitle(), dto.getAuthor());
    }

    @GetMapping("all")
    public Mono<BookListDto> getAll() {
        return service.getAllBooks().map(BookListDto::new);
    }

    @GetMapping("{id}")
    public Mono<Book> getById(@PathVariable Long id) {
        return service.getBook(id);
    }

    @PostMapping("{id}/remove")
    public Mono<Void> remove(@PathVariable Long id) {
        return service.removeStudent(id);
    }

    @Getter
    @Setter
    @ToString
    public static class AddDto {
        private String title;
        private String author;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class BookListDto {
        private List<Book> books;
    }
}
