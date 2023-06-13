package be.vlaanderen.informatievlaanderen.ldes.server.infra.mongo.mongock.changeset2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "ldes-server")
@Configuration
public class AppConfigChangeset2 {
	private String hostName;
	private List<LdesConfig> collections = new ArrayList<>();

	public String getHostName() {
		return hostName;
	}

	public AppConfigChangeset2 setHostName(String hostName) {
		this.hostName = hostName;
		return this;
	}

	public List<LdesConfig> getCollections() {
		return collections;
	}

	public void setCollections(List<LdesConfig> collections) {
		this.collections = collections;
	}

	public LdesConfig getLdesConfig(String collectionName) {
		return getCollections()
				.stream()
				.filter(ldes -> ldes.getCollectionName().equals(collectionName))
				.findFirst()
				.orElse(new LdesConfig());
	}

}
