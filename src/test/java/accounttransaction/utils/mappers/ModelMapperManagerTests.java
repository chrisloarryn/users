package accounttransaction.utils.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.test.context.ActiveProfiles;

import accounttransaction.utils.mappers.ModelMapperManager;
import accounttransaction.utils.mappers.ModelMapperService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class ModelMapperManagerTests {

	private ModelMapperManager modelMapperManager;

	@BeforeEach
	public void setUp() {
		ModelMapper mapper = new ModelMapper();
		modelMapperManager = new ModelMapperManager(mapper);
	}

	@Test
	public void testForResponse() {
		ModelMapper mapper = mock(ModelMapper.class);
		Configuration configuration = mock(Configuration.class);

		given(configuration.setAmbiguityIgnored(true)).willReturn(configuration);
		given(configuration.setMatchingStrategy(MatchingStrategies.LOOSE)).willReturn(configuration);
		when(mapper.getConfiguration()).thenReturn(configuration);

		ModelMapperService modelMapperService = new ModelMapperManager(mapper);
		ModelMapper resultMapper = modelMapperService.forResponse();

		// Verify that setAmbiguityIgnored(true) and setMatchingStrategy(LOOSE) were
		// called
		verify(configuration).setAmbiguityIgnored(true);
		verify(configuration).setMatchingStrategy(MatchingStrategies.LOOSE);

		// Verify that the same mapper instance is returned
		assert (resultMapper == mapper);
	}

	@Test
	public void testForRequest() {
		ModelMapper mapper = mock(ModelMapper.class);
		Configuration configuration = mock(Configuration.class);

		given(configuration.setAmbiguityIgnored(true)).willReturn(configuration);
		given(configuration.setMatchingStrategy(MatchingStrategies.STANDARD)).willReturn(configuration);
		when(mapper.getConfiguration()).thenReturn(configuration);

		ModelMapperService modelMapperService = new ModelMapperManager(mapper);
		ModelMapper resultMapper = modelMapperService.forRequest();

		// Verify that setAmbiguityIgnored(true) and setMatchingStrategy(STANDARD) were
		// called
		verify(configuration).setAmbiguityIgnored(true);
		verify(configuration).setMatchingStrategy(MatchingStrategies.STANDARD);

		// Verify that the same mapper instance is returned
		assert (resultMapper == mapper);
	}
}
