package com.example.board;

import com.example.board.entity.Post;
import com.example.board.repository.PostRepository;
import com.example.board.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:replacetestdb;DB_CLOSE_DELAY=-1")
class ReplaceWordTest {

    @Autowired
    PostService service;

    @Autowired
    PostRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private Long save(String title, String content) {
        Post p = new Post();
        p.setTitle(title);
        p.setName("kim");
        p.setContent(content);
        return repository.save(p).getId();
    }

    @Test
    void 단어가_있는_글만_변경_건수에_포함() {
        Long has = save("kfc 후기", "kfc 맛있다");
        Long none = save("주말 계획", "영화 보기");

        int changed = service.replaceWord(List.of(has, none), "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
        assertThat(repository.findById(has).get().getTitle()).isEqualTo("bhc 후기");
        assertThat(repository.findById(none).get().getTitle()).isEqualTo("주말 계획");
    }

    @Test
    void 대소문자만_다른_글은_변경되지_않으므로_집계_제외() {
        Long upper = save("KFC 후기", "KFC 최고");

        int changed = service.replaceWord(List.of(upper), "kfc", "bhc");

        assertThat(changed).isZero();
        assertThat(repository.findById(upper).get().getTitle()).isEqualTo("KFC 후기");
    }

    @Test
    void 중복_id는_한_번만_집계() {
        Long id = save("kfc", "내용");

        int changed = service.replaceWord(List.of(id, id), "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
    }

    @Test
    void 없는_id는_건너뜀() {
        Long id = save("kfc", "내용");

        int changed = service.replaceWord(List.of(id, 99999L), "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
    }

    @Test
    void content에만_단어가_있어도_변경() {
        Long id = save("제목", "오늘은 kfc");

        int changed = service.replaceWord(List.of(id), "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
        assertThat(repository.findById(id).get().getContent()).isEqualTo("오늘은 bhc");
    }
}
