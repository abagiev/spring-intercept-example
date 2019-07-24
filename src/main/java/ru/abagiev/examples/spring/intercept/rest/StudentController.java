package ru.abagiev.examples.spring.intercept.rest;

import lombok.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.abagiev.examples.spring.intercept.model.Student;
import ru.abagiev.examples.spring.intercept.service.LibraryService;

import java.util.List;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final LibraryService service;

    @PostMapping("add")
    public Mono<Student> add(@RequestBody AddDto dto) {
        return service.addStudent(dto.getFirstName(), dto.getLastName());
    }

    @GetMapping("all")
    public Mono<StudentListDto> getAll() {
        return service.getAllStudents().map(StudentListDto::new);
    }

    @GetMapping("{id}")
    public Mono<Student> getById(@PathVariable Long id) {
        return service.getStudent(id);
    }

    @PostMapping("{id}/remove")
    public Mono<Void> remove(@PathVariable Long id) {
        return service.removeStudent(id);
    }

    @Getter
    @Setter
    @ToString
    public static class AddDto {
        private String firstName;
        private String lastName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class StudentListDto {
        private List<Student> students;
    }
}
