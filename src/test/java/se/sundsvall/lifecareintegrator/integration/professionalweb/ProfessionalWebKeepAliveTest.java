package se.sundsvall.lifecareintegrator.integration.professionalweb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProfessionalWebKeepAliveTest {

	@Mock
	private ProfessionalWebSession session;

	@InjectMocks
	private ProfessionalWebKeepAlive keepAlive;

	@Test
	void keepsTheSessionAlive() {
		keepAlive.keepAlive();

		verify(session).keepAlive();
	}
}
