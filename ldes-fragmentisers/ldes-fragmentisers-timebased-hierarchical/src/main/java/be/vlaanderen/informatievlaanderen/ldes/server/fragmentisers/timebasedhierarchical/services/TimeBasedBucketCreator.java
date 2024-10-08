package be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.timebasedhierarchical.services;

import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.entities.Bucket;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.repository.BucketRepository;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.valueobjects.BucketDescriptorPair;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.timebasedhierarchical.constants.Granularity;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.timebasedhierarchical.model.FragmentationTimestamp;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static be.vlaanderen.informatievlaanderen.ldes.server.domain.constants.ServerConstants.DEFAULT_BUCKET_STRING;
import static be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.FragmentationService.LDES_SERVER_CREATE_FRAGMENTS_COUNT;
import static be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.metrics.MetricsConstants.FRAGMENTATION_STRATEGY;
import static be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.metrics.MetricsConstants.VIEW;
import static be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.timebasedhierarchical.HierarchicalTimeBasedFragmentationStrategy.TIMEBASED_FRAGMENTATION_HIERARCHICAL;

public class TimeBasedBucketCreator {
	private final BucketRepository bucketRepository;
	private final TimeBasedRelationsAttributer relationsAttributer;
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeBasedBucketCreator.class);

	public TimeBasedBucketCreator(BucketRepository bucketRepository, TimeBasedRelationsAttributer relationsAttributer) {
		this.bucketRepository = bucketRepository;
		this.relationsAttributer = relationsAttributer;
	}

	public Bucket getOrCreateBucket(Bucket parentFragment,
	                                FragmentationTimestamp fragmentationTimestamp,
	                                Granularity granularity) {
		return getOrCreateBucket(parentFragment, fragmentationTimestamp.getTimeValueForGranularity(granularity), granularity);
	}

	public Bucket getOrCreateBucket(Bucket parentBucket, String timeValue, Granularity granularity) {
		final BucketDescriptorPair childDescriptorPair = new BucketDescriptorPair(granularity.getValue(), timeValue);
		return bucketRepository
				.retrieveBucket(parentBucket.getViewName(), parentBucket.createChildDescriptor(childDescriptorPair))
				.orElseGet(() -> {
					final Bucket childBucket = bucketRepository.insertBucket(parentBucket.createChild(childDescriptorPair));
					addRelationToParent(parentBucket, childBucket);
					logBucketisation(parentBucket, childBucket);
					return childBucket;
				});
	}

	private void addRelationToParent(Bucket parentBucket, Bucket childBucket) {
		if(isDefaultBucket(childBucket)) {
			relationsAttributer.addDefaultRelation(parentBucket, childBucket);
		} else {
			relationsAttributer.addInBetweenRelation(parentBucket, childBucket);
		}
	}


	private boolean isDefaultBucket(Bucket bucket) {
		return bucket.getValueForKey(Granularity.YEAR.getValue()).orElse("").equals(DEFAULT_BUCKET_STRING);
	}

	private void logBucketisation(Bucket parentBucket, Bucket child) {
		String viewName = parentBucket.getViewName().asString();
		Metrics
				.counter(LDES_SERVER_CREATE_FRAGMENTS_COUNT, VIEW, viewName, FRAGMENTATION_STRATEGY, TIMEBASED_FRAGMENTATION_HIERARCHICAL)
				.increment();
		LOGGER.debug("Timebased fragment created with id: {}", child.getBucketDescriptorAsString());
	}


}
