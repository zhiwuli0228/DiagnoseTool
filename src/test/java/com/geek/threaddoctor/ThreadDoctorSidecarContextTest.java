package com.geek.threaddoctor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("sidecar")
class ThreadDoctorSidecarContextTest {
    @Test
    void loadsSidecarContext() {
    }
}
