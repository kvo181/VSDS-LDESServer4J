package be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.entities;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.FragmentPair;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.LdesFragmentIdentifier;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.TreeRelation;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.ViewName;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.exceptions.DuplicateFragmentPairException;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Fragment {

	public static final String ROOT = "root";
	private final LdesFragmentIdentifier identifier;
	private Boolean immutable;
	private int nrOfMembersAdded;
	private final List<TreeRelation> relations;
	private LocalDateTime deleteTime;
	private LocalDateTime nextUpdateTs = null;

	public Fragment(LdesFragmentIdentifier identifier) {
		this(identifier, false, 0, new ArrayList<>(), null);
	}

	public Fragment(LdesFragmentIdentifier identifier, Boolean immutable, int nrOfMembersAdded,
	                List<TreeRelation> relations, LocalDateTime deleteTime) {
		this.identifier = identifier;
		this.immutable = immutable;
		this.nrOfMembersAdded = nrOfMembersAdded;
		this.relations = relations;
		this.deleteTime = deleteTime;
	}

	public LdesFragmentIdentifier getFragmentId() {
		return identifier;
	}

	public String getFragmentIdString() {
		return identifier.asDecodedFragmentId();
	}

	public List<FragmentPair> getFragmentPairs() {
		return this.identifier.getFragmentPairs();
	}

	public void makeImmutable() {
		this.immutable = true;
	}

	public boolean isImmutable() {
		return this.immutable;
	}

	public Fragment createChild(FragmentPair fragmentPair) {
		List<FragmentPair> childFragmentPairs = new ArrayList<>(this.identifier.getFragmentPairs().stream().toList());
		if (hasChildWithSameFragmentKey(fragmentPair, childFragmentPairs)) {
			throw new DuplicateFragmentPairException(identifier.asDecodedFragmentId(), fragmentPair.fragmentKey());
		}
		childFragmentPairs.add(fragmentPair);
		return new Fragment(new LdesFragmentIdentifier(getViewName(), childFragmentPairs));
	}

	private static boolean hasChildWithSameFragmentKey(FragmentPair fragmentPair, List<FragmentPair> childFragmentPairs) {
		return childFragmentPairs
				.stream()
				.map(FragmentPair::fragmentKey)
				.anyMatch(key -> key.equals(fragmentPair.fragmentKey()));
	}

	public Optional<String> getValueOfKey(String key) {
		return this.identifier.getValueOfFragmentPairKey(key);
	}

	public ViewName getViewName() {
		return this.identifier.getViewName();
	}

	public int getNrOfMembersAdded() {
		return this.nrOfMembersAdded;
	}

	public int incrementNrOfMembersAdded() {
		return this.nrOfMembersAdded++;
	}

	public Optional<LdesFragmentIdentifier> getParentId() {
		return identifier.getParentId();
	}

	public String getParentIdAsString() {
		return identifier.getParentId().map(LdesFragmentIdentifier::asDecodedFragmentId).orElse(ROOT);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Fragment that = (Fragment) o;
		return Objects.equals(getFragmentId(), that.getFragmentId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getFragmentId());
	}

	public void addRelation(TreeRelation relation) {
		relations.add(relation);
	}

	public boolean containsRelation(TreeRelation parentChildRelation) {
		return relations.contains(parentChildRelation);
	}

	public List<TreeRelation> getRelations() {
		return relations;
	}

	public boolean isRoot() {
		return this.identifier.getFragmentPairs().isEmpty();
	}

	public LocalDateTime getDeleteTime() {
		return deleteTime;
	}

	public void removeRelationToIdentifier(LdesFragmentIdentifier fragmentIdentifier) {
		relations.removeIf(treeRelation -> treeRelation.treeNode().equals(fragmentIdentifier));
	}

	public boolean isConnectedTo(Fragment otherFragment) {
		return getRelations()
				.stream()
				.anyMatch(treeRelation -> treeRelation.treeNode()
						.equals(otherFragment.getFragmentId()));
	}

	@Nullable
	public LocalDateTime getNextUpdateTs() {
		return nextUpdateTs;
	}

	public void setNextUpdateTs(@Nullable LocalDateTime nextUpdateTs) {
		this.nextUpdateTs = nextUpdateTs;
	}

	@Override
	public String toString() {
		return "Fragment{" +
		       "identifier=" + identifier +
		       ", immutable=" + immutable +
		       ", nrOfMembersAdded=" + nrOfMembersAdded +
		       ", relations=" + relations +
		       ", deleteTime=" + deleteTime +
		       '}';
	}
}
