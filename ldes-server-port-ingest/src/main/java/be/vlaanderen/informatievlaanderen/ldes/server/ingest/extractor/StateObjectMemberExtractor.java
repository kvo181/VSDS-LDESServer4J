package be.vlaanderen.informatievlaanderen.ldes.server.ingest.extractor;

import be.vlaanderen.informatievlaanderen.ldes.server.ingest.entities.IngestedMember;
import org.apache.jena.rdf.model.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class StateObjectMemberExtractor implements MemberExtractor {
    private final String collectionName;

    public StateObjectMemberExtractor(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public List<IngestedMember> extractMembers(Model ingestedModel) {
        final String txId = UUID.randomUUID().toString();
        final LocalDateTime ingestedTimestamp = LocalDateTime.now();

        return MemberModelExtractor.initialize(ingestedModel).extractAllMemberModels().stream()
                .map(memberModel -> memberModel.mapToMember(collectionName, ingestedTimestamp, txId))
                .toList();
    }
}
