package com.r2s.masterclass.student.service.impl;

import com.r2s.masterclass.shared.exception.StudentAlreadyExistsException;
import com.r2s.masterclass.student.dto.StudentRequestDTO;
import com.r2s.masterclass.student.dto.StudentResponseDTO;
import com.r2s.masterclass.student.entity.StudentEntity;
import com.r2s.masterclass.student.repository.StudentRepository;
import com.r2s.masterclass.support.IntegrationTestSupport;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class StudentRegistrationConcurrencyTest extends IntegrationTestSupport {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @ParameterizedTest
    @ValueSource(strings = {"email", "nationalId"})
    @Timeout(30)
    void concurrentRegistrationsCommitExactlyOneRowAndTranslateRealDatabaseConflict(String duplicateField)
            throws Exception {
        var beforeWrite = new CyclicBarrier(2);
        var databaseConflicts = new AtomicInteger();
        var gated = mock(StudentRepository.class, delegatesTo(studentRepository));

        // Các truy vấn vẫn chạy trên H2 thật. Barrier buộc cả hai precheck hoàn tất trước khi ghi.
        doAnswer(invocation -> {
            beforeWrite.await(10, TimeUnit.SECONDS);
            try {
                return studentRepository.saveAndFlush(invocation.getArgument(0));
            } catch (DataIntegrityViolationException exception) {
                databaseConflicts.incrementAndGet();
                throw exception;
            }
        }).when(gated).saveAndFlush(any(StudentEntity.class));

        var service = new StudentServiceImpl(gated);
        var first = new StudentRequestDTO("Sinh vien A", "race-a@example.test", "0901234567",
                "079123456781", "Truong mau", "CNPM");
        var second = new StudentRequestDTO("Sinh vien B",
                duplicateField.equals("email") ? first.email() : "race-b@example.test", "0901234567",
                duplicateField.equals("nationalId") ? first.nationalId() : "079123456782", "Truong mau", "CNPM");

        List<RegistrationResult> results;
        try (var executor = Executors.newFixedThreadPool(2)) {
            var taskA = executor.submit(() -> registerInTransaction(service, first));
            var taskB = executor.submit(() -> registerInTransaction(service, second));
            results = List.of(taskA.get(20, TimeUnit.SECONDS), taskB.get(20, TimeUnit.SECONDS));
        }

        assertThat(results).filteredOn(result -> result.student() != null).hasSize(1);
        assertThat(results).filteredOn(result -> result.duplicate() != null).singleElement()
                .satisfies(result -> assertThat(result.duplicate()).hasMessage(
                        "Số CCCD hoặc Email đã tồn tại trên hệ thống OJT"));
        assertThat(databaseConflicts.get()).as("a real H2 unique violation must occur").isEqualTo(1);
        assertThat(studentRepository.count()).isEqualTo(1);
        var winner = results.stream().filter(result -> result.student() != null)
                .findFirst().orElseThrow().student();
        assertThat(studentRepository.findById(winner.id())).isPresent();
        assertThat(winner.maskedPhone()).isEqualTo("090*****67");
    }

    private RegistrationResult registerInTransaction(StudentServiceImpl service, StudentRequestDTO request) {
        try {
            // Service có wrapper đồng bộ riêng cho test; transaction vẫn dùng JpaTransactionManager thật.
            StudentResponseDTO student = new TransactionTemplate(transactionManager)
                    .execute(status -> service.register(request));
            return new RegistrationResult(student, null);
        } catch (StudentAlreadyExistsException duplicate) {
            return new RegistrationResult(null, duplicate);
        }
    }

    private record RegistrationResult(StudentResponseDTO student, StudentAlreadyExistsException duplicate) {
    }
}
