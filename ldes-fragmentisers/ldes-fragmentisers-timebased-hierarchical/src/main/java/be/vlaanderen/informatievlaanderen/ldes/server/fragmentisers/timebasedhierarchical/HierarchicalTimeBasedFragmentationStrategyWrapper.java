package be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.timebasedhierarchical;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.ConfigProperties;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.FragmentationStrategy;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.FragmentationStrategyWrapper;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.repository.BucketRepository;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.timebasedhierarchical.config.TimeBasedConfig;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.timebasedhierarchical.constants.Granularity;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.timebasedhierarchical.services.TimeBasedBucketCreator;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.timebasedhierarchical.services.TimeBasedBucketFinder;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.timebasedhierarchical.services.TimeBasedRelationsAttributer;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.ApplicationContext;

import static be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.timebasedhierarchical.config.TimeBasedProperties.*;

public class HierarchicalTimeBasedFragmentationStrategyWrapper implements FragmentationStrategyWrapper {

	public FragmentationStrategy wrapFragmentationStrategy(ApplicationContext applicationContext,
	                                                       FragmentationStrategy fragmentationStrategy, ConfigProperties fragmentationProperties) {
		BucketRepository bucketRepository = applicationContext.getBean(BucketRepository.class);
		ObservationRegistry observationRegistry = applicationContext.getBean(ObservationRegistry.class);

		TimeBasedConfig config = createConfig(fragmentationProperties);
		TimeBasedRelationsAttributer relationsAttributer = new TimeBasedRelationsAttributer(
				applicationContext, config);
		TimeBasedBucketCreator bucketCreator = new TimeBasedBucketCreator(bucketRepository, relationsAttributer);
		TimeBasedBucketFinder bucketFinder = new TimeBasedBucketFinder(bucketCreator, config);
		return new HierarchicalTimeBasedFragmentationStrategy(fragmentationStrategy,
				observationRegistry, bucketFinder, applicationContext, config);
	}

	private TimeBasedConfig createConfig(ConfigProperties properties) {
		return new TimeBasedConfig(properties.getOrDefault(FRAGMENTATION_SUBJECT_FILTER, ".*"),
				properties.get(FRAGMENTATION_PATH), Granularity.from(properties.get(MAX_GRANULARITY)),
				Boolean.parseBoolean(properties.getOrDefault(LINEAR_TIME_CACHING_ENABLED, "false")));
	}

}
