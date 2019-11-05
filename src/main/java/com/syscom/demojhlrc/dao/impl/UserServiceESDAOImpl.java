package com.syscom.demojhlrc.dao.impl;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.demojhlrc.beans.User;
import com.syscom.demojhlrc.dao.UserServiceESDAO;
import com.syscom.demojhlrc.exception.ElasticSearchException;

@Repository
public class UserServiceESDAOImpl implements UserServiceESDAO {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceESDAOImpl.class);

	private static final String USER_DOCUMENT_INDEX = "user_index";
	private static final String USER_DOCUMENT_TYPE = "user";

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public void createIndex() {
		LocalDateTime start = LocalDateTime.now();
		CreateIndexRequest request = new CreateIndexRequest(USER_DOCUMENT_INDEX);
		restHighLevelClient.indices().createAsync(request, RequestOptions.DEFAULT,
				new ActionListener<CreateIndexResponse>() {
					@Override
					public void onResponse(CreateIndexResponse createIndexResponse) {
						LocalDateTime end = LocalDateTime.now();
						int duration = Duration.between(start, end).getNano() / 1000000;
						logger.info("Create Successffuly elastic search index {} (duration {} ms).",
								USER_DOCUMENT_INDEX, duration);
					}

					@Override
					public void onFailure(Exception exception) {
						logger.error("unable to create elastic search index {}, exception {}.", USER_DOCUMENT_INDEX,
								exception);
					}
				});
	}

	@Override
	public String create(User user) {
		user.setId(UUID.randomUUID().toString());
		Map<String, Object> documentMapper = objectMapper.convertValue(user, new TypeReference<Map<String, Object>>() {
		});
		IndexRequest indexRequest = new IndexRequest(USER_DOCUMENT_INDEX, USER_DOCUMENT_TYPE, user.getId())
				.source(documentMapper);
		IndexResponse indexResponse;
		try {
			indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
		} catch (IOException ioException) {
			logger.error("Erreur lors de la création de l'utilisateur {}.", user, ioException);
			throw new ElasticSearchException(ioException);
		}
		return indexResponse.getResult().name();

	}

	@Override
	public User findById(String id) {
		GetResponse getResponse;
		try {
			getResponse = restHighLevelClient.get(new GetRequest(USER_DOCUMENT_INDEX, USER_DOCUMENT_TYPE, id),
					RequestOptions.DEFAULT);
		} catch (IOException ioException) {
			logger.error("Erreur lors de la recherche de l'utilisateur ayant pour ID : {}.", id, ioException);
			throw new ElasticSearchException(ioException);
		}

		return objectMapper.convertValue(getResponse.getSource(), User.class);
	}

	@Override
	public String update(User user) {
		User resultDocument = findById(user.getId());
		if (resultDocument == null) {
			return null;
		}
		UpdateRequest updateRequest = new UpdateRequest(USER_DOCUMENT_INDEX, USER_DOCUMENT_TYPE,
				resultDocument.getId());
		Map<String, Object> documentMapper = objectMapper.convertValue(user, new TypeReference<Map<String, Object>>() {
		});
		updateRequest.doc(documentMapper);
		UpdateResponse updateResponse;
		try {
			updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
		} catch (IOException ioException) {
			logger.error("Erreur lors de la mise à jour de l'utilisateur {}.", user, ioException);
			throw new ElasticSearchException(ioException);
		}

		return updateResponse.getResult().name();
	}

	@Override
	public String deleteProfileDocument(String id) {
		DeleteResponse response;
		try {
			response = restHighLevelClient.delete(new DeleteRequest(USER_DOCUMENT_INDEX, USER_DOCUMENT_TYPE, id),
					RequestOptions.DEFAULT);
		} catch (IOException ioException) {
			logger.error("Erreur lors de suppression de l'utilisateur ayant pour Id : {}.", id, ioException);
			throw new ElasticSearchException(ioException);
		}
		return response.getResult().name();
	}

	@Override
	public List<User> findAll() {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		return getSearchResult(searchSourceBuilder);
	}

	@Override
	public List<User> searchByLastName(String lastName) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.wildcardQuery("lastName", "*" + lastName + "*"));
		return getSearchResult(searchSourceBuilder);
	}

	private List<User> getSearchResult(SearchSourceBuilder searchSourceBuilder) {
		List<User> users = new ArrayList<>();
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.source(searchSourceBuilder);
		SearchResponse response;

		try {
			response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException ioException) {
			logger.error("Erreur lors la recherche.", ioException);
			throw new ElasticSearchException(ioException);
		}

		SearchHit[] searchHit = response.getHits().getHits();
		if (searchHit.length > 0) {
			Arrays.stream(searchHit)
					.forEach(hit -> users.add(objectMapper.convertValue(hit.getSourceAsMap(), User.class)));
		}
		return users;
	}
}
