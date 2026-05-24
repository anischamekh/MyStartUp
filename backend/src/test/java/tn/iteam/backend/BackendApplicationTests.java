package tn.iteam.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Disabled to avoid requiring a running PostgreSQL during tests")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
