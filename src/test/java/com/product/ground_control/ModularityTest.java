package com.product.ground_control;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

	ApplicationModules modules = ApplicationModules.of(GroundControlApplication.class);

	@Test
	void verifiesModularStructure() {
		modules.verify();
	}

	@Test
	void createModuleDocumentation() {
		new Documenter(modules).writeDocumentation();
	}
}