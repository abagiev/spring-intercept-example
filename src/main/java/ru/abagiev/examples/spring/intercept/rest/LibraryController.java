package ru.abagiev.examples.spring.intercept.rest;

import lombok.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.abagiev.examples.spring.intercept.model.Book;
import ru.abagiev.examples.spring.intercept.service.LibraryService;

import java.util.List;

@RestController
@RequestMapping("/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService service;

    @PostMapping("bind")
    public Mono<Void> bind(@RequestBody KeyDto key) {
        return service.bind(key.getStudentId(), key.getBookId());
    }

    @PostMapping("unbind")
    public Mono<Void> unbind(@RequestBody KeyDto key) {
        return service.unbind(key.getStudentId(), key.getBookId());
    }

    @GetMapping("student/{studentId}/bound/{bookId}")
    public Mono<BoundDto> bound(@PathVariable Long studentId, @PathVariable Long bookId) {
        return service.isBound(studentId, bookId).map(BoundDto::new);
    }

    @GetMapping("student/{studentId}/all")
    public Mono<BookListDto> getBoundBooks(@PathVariable Long studentId) {
        return service.getBoundBooks(studentId).map(BookListDto::new);
    }

    @Getter
    @Setter
    @ToString
    public static class KeyDto {
        private long studentId;
        private long bookId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class BoundDto {
        private boolean isBound;
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
