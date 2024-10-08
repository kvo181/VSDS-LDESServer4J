package be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.postgres.mapper;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.ViewName;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.entities.Bucket;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.postgres.entity.BucketEntity;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.postgres.projections.BucketProjection;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.valueobjects.BucketDescriptor;

public class BucketMapper {
	private BucketMapper() {
	}

	public static Bucket fromProjection(BucketProjection projection) {
		return new Bucket(
				projection.getBucketId(),
				BucketDescriptor.fromString(projection.getBucketDescriptor()),
				ViewName.fromString(projection.getViewName())
		);
	}

	public static Bucket fromEntity(BucketEntity entity) {
		return new Bucket(
				entity.getBucketId(),
				BucketDescriptor.fromString(entity.getBucketDescriptor()),
				ViewName.fromString(entity.getView().getComposedViewName())
		);
	}

}
