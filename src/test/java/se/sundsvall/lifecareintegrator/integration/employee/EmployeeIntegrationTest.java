package se.sundsvall.lifecareintegrator.integration.employee;

import generated.se.sundsvall.employee.PortalPersonData;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.lifecareintegrator.integration.employee.configuration.EmployeeProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeIntegrationTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String LOGIN_NAME = "LOHE";

	@Mock
	private EmployeeClient employeeClientMock;

	@Mock
	private EmployeeProperties propertiesMock;

	@InjectMocks
	private EmployeeIntegration employeeIntegration;

	@Test
	void resolvesTheFullNameForALoginName() {
		when(propertiesMock.domain()).thenReturn("personal");
		when(employeeClientMock.getPortalPersonData(MUNICIPALITY_ID, "personal", LOGIN_NAME))
			.thenReturn(Optional.of(new PortalPersonData().fullname("Lotta Helsinger")));

		assertThat(employeeIntegration.getFullName(MUNICIPALITY_ID, LOGIN_NAME)).contains("Lotta Helsinger");
	}

	@Test
	void anUnknownLoginNameYieldsNothing() {
		// dismiss404 turns "no such employee" into an empty Optional — the common case for ids like FÖRSKASSAN.
		when(propertiesMock.domain()).thenReturn("personal");
		when(employeeClientMock.getPortalPersonData(any(), any(), any())).thenReturn(Optional.empty());

		assertThat(employeeIntegration.getFullName(MUNICIPALITY_ID, "FÖRSKASSAN")).isEmpty();
	}

	@Test
	void aBlankFullNameYieldsNothingRatherThanAnEmptyString() {
		when(propertiesMock.domain()).thenReturn("personal");
		when(employeeClientMock.getPortalPersonData(any(), any(), any())).thenReturn(Optional.of(new PortalPersonData().fullname("  ")));

		assertThat(employeeIntegration.getFullName(MUNICIPALITY_ID, LOGIN_NAME)).isEmpty();
	}

	@Test
	void anEmployeeFailureIsAbsorbed() {
		// A caseworker's name is decoration; a decision must never be withheld because the directory is down.
		when(propertiesMock.domain()).thenReturn("personal");
		when(employeeClientMock.getPortalPersonData(any(), any(), any())).thenThrow(new IllegalStateException("circuit open"));

		assertThat(employeeIntegration.getFullName(MUNICIPALITY_ID, LOGIN_NAME)).isEmpty();
	}
}
