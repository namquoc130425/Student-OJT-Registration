package com.r2s.masterclass.support;

import com.r2s.masterclass.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/** Database riêng cho test, độc lập với application.yml local của người chạy. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ojt_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.open-in-view=false",
        "spring.h2.console.enabled=false",
        "logging.level.org.hibernate.engine.jdbc.spi.SqlExceptionHelper=OFF"
})
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    @Autowired
    protected StudentRepository studentRepository;

    @BeforeEach
    void clearTestDatabase() {
        studentRepository.deleteAllInBatch();
    }
}
