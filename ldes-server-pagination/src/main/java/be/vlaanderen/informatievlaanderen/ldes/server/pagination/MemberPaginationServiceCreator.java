package be.vlaanderen.informatievlaanderen.ldes.server.pagination;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.ConfigProperties;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.ViewName;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.ViewSpecification;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.repository.BucketisedMemberRepository;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.repository.FragmentRepository;
import be.vlaanderen.informatievlaanderen.ldes.server.pagination.config.PaginationProperties;
import be.vlaanderen.informatievlaanderen.ldes.server.pagination.services.OpenPageProvider;
import be.vlaanderen.informatievlaanderen.ldes.server.pagination.config.PaginationConfig;
import be.vlaanderen.informatievlaanderen.ldes.server.pagination.repositories.PaginationSequenceRepository;
import be.vlaanderen.informatievlaanderen.ldes.server.pagination.services.PageCreator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MemberPaginationServiceCreator {
    private final PaginationSequenceRepository sequenceRepository;
    private final BucketisedMemberRepository bucketisedMemberRepository;
    private final FragmentRepository fragmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MemberPaginationServiceCreator(PaginationSequenceRepository sequenceRepository,
                                          BucketisedMemberRepository bucketisedMemberRepository,
                                          FragmentRepository fragmentRepository,
                                          ApplicationEventPublisher eventPublisher) {
        this.sequenceRepository = sequenceRepository;
        this.bucketisedMemberRepository = bucketisedMemberRepository;
        this.fragmentRepository = fragmentRepository;
        this.eventPublisher = eventPublisher;
    }

    public MemberPaginationService createPaginationService(ViewName viewName, ViewSpecification view) {
        OpenPageProvider openPageProvider = getOpenPageProvider(view.getPaginationProperties());

        return new MemberPaginationService(sequenceRepository, bucketisedMemberRepository,
                openPageProvider, fragmentRepository, eventPublisher, viewName);
    }

    private OpenPageProvider getOpenPageProvider(ConfigProperties properties) {
        PaginationConfig paginationConfig = createPaginationConfig(properties);
        PageCreator pageFragmentCreator = getPageCreator(paginationConfig.bidirectionalRelations());
        return new OpenPageProvider(pageFragmentCreator, fragmentRepository,
                paginationConfig.memberLimit());
    }

    private PageCreator getPageCreator(boolean bidirectionalRelations) {
        return new PageCreator(
                fragmentRepository, bidirectionalRelations);
    }

    private PaginationConfig createPaginationConfig(ConfigProperties properties) {
        return new PaginationConfig(Long.valueOf(properties.get(PaginationProperties.MEMBER_LIMIT)),
                Boolean.parseBoolean(properties.getOrDefault(PaginationProperties.BIDIRECTIONAL_RELATIONS, "true")));
    }
}
