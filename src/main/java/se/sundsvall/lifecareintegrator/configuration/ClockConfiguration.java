package se.sundsvall.lifecareintegrator.configuration;

import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The one {@link Clock} and one {@link Ticker} for the whole application, so every component that needs the current
 * time or a cache tick gets it injected rather than reaching for a static factory it cannot fake in a test.
 */
@Configuration
public class ClockConfiguration {

	/** Sundsvall's time zone, for the clock and for the zone-less date-times Lifecare sends. */
	public static final ZoneId STOCKHOLM = ZoneId.of("Europe/Stockholm");

	@Bean
	Clock clock() {
		return Clock.system(STOCKHOLM);
	}

	@Bean
	Ticker ticker() {
		return Ticker.systemTicker();
	}
}
