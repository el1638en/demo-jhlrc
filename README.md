## Elastic Search - Java High Level Rest Client

Elastic Search est un moteur de recherche distribué destiné au traitement de données volumineuses.
Il utilise Apache Lucene pour indexer et rechercher les données.
Pour communiquer avec Elastic Search, on utilise souvent un client TCP sur le port 9200. Spring Data ElasticSearch utilise cette solution.

Il existe une 2ème solution pour communiquer avec Elastic Search. Il s'agit de [Java
High Level Rest Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high.html), une bibliothèque développée par Elastic Search et qui utilise
le protocole HTTP sur le port 9300. Nous allons utiliser cette 2ème solution dans ce tutoriel.
1. Installation

    1. Environnement

    ```
    root@pl-debian:~# uname -a
    Linux pl-debian 4.9.0-8-amd64 #1 SMP Debian 4.9.110-3+deb9u6 (2018-10-08) x86_64 GNU/Linux
    root@pl-debian:~# lsb_release -a
    No LSB modules are available.
    Distributor ID: Debian
    Description:    Debian GNU/Linux 9.5 (stretch)
    Release:        9.5
    Codename:       stretch
    ```

    2. Elastic Search

    Le fichier `docker-compose.yml` à la racine du projet contient un service Elastic
    Search prêt à l'utilisation. Il permet de créer un container Docker à partir de l'image d'Elastic Search.

    ```
	version: '3.6'

	services:  
	  elasticsearch:
        image: elasticsearch:6.8.2
        container_name: elasticsearch
        ports:
            - "9200:9200"
            - "9300:9300"
        environment:
	      - node.name=node-1
	      - cluster.name=docker-cluster
	      - bootstrap.memory_lock=true
	      - http.cors.enabled=true
	      - http.cors.allow-origin=*
	      - node.master=true
	      - node.data=true
	      - http.port=9200
	      - transport.tcp.port=9300
    ```

    Pour lancer le  service, utiliser la commande `docker-compose up -d`.

    ```
    root@pl-debian:~# docker-compose up -d
    Starting elasticsearch ... done
    root@pl-debian:~# docker-compose ps
    elasticsearch   /usr/local/bin/docker-entr ...   Up      0.0.0.0:9200->9200/tcp, 0.0.0.0:9300->9300/tcp

    ```


2. [Java High Level Rest Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high.html)

    1. Configuration du client

        - fichier `src/main/resources/application.properties`

            ```
            # Elasticsearch REST clients (RestClientProperties)
            spring.elasticsearch.rest.host=127.0.0.1
            spring.elasticsearch.rest.port=9200
            spring.elasticsearch.rest.username= # Credentials username.
            spring.elasticsearch.rest.password= # Credentials password.
            ```

        - Configuration du client `RestHighLevelClient` pour se connecter à ElasticSearch

		  ```java
          package com.syscom.demojhlrc.config;

	      import org.apache.http.HttpHost;
	      import org.elasticsearch.client.RestClient;
	      import org.elasticsearch.client.RestHighLevelClient;
	      import org.springframework.beans.factory.annotation.Value;
	      import org.springframework.context.annotation.Bean;
	      import org.springframework.context.annotation.Configuration;

		  @Configuration
	      public class ElasticSearchConfig {

            @Value("${spring.elasticsearch.rest.host}")
            private String elasticSearchHost;

		    @Value("${spring.elasticsearch.rest.port}")
            private int elasticSearchPort;

            @Value("${spring.elasticsearch.rest.username}")
            private String elasticSearchClientUsername;

		    @Value("${spring.elasticsearch.rest.password}")
            private String elasticSearchClientPassword;

            @Bean(destroyMethod = "close")
            public RestHighLevelClient client() {
                RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticSearchHost, elasticSearchPort)));
                return client;
            }

		  }
          ```

    2. Model `User`

        ```java
         package com.syscom.demojhlrc.beans;

         import java.util.List;
         import lombok.AllArgsConstructor;
         import lombok.Builder;
         import lombok.Data;
         import lombok.NoArgsConstructor;
         import lombok.ToString;

         @Data
         @Builder
         @ToString
         @AllArgsConstructor
         @NoArgsConstructor
         public class User {
            private String id;
            private String firstName;
            private String lastName;
            private List<String> emails;
        }
        ```

    3. Le DAO des  utilisateurs pour écrire/rechercher/modifier/supprimer des données du serveur Elastic Search.

        ```java
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
                Map<String, Object> documentMapper = objectMapper.convertValue(user, new TypeReference<Map<String, Object>>() {});
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
                UpdateRequest updateRequest = new UpdateRequest(USER_DOCUMENT_INDEX, USER_DOCUMENT_TYPE, resultDocument.getId());
                Map<String, Object> documentMapper = objectMapper.convertValue(user, new TypeReference<Map<String, Object>>() { });
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
        ```

    4. Le service métier de traitement des utilisateurs est un passe-plat qui appelle la DAO ci-dessus (voir la classe `UserServiceImpl`)

    5. Appel du service depuis un controller REST API.

      ```java
        @RestController
        @RequestMapping(UserController.PATH)
        public class UserController {

            private static final Logger logger = LoggerFactory.getLogger(UserController.class);

            public static final String PATH = "/api/users";

            @Autowired
            private UserServiceES userServiceES;

            @PostMapping
            public String createUser(@RequestBody User user) {
                logger.info("Create user {}.", user);
                return userServiceES.create(user);
            }

            @GetMapping("/{id}")
            public User findUserById(@PathVariable String id) {
                logger.info("Find user by id {}.", id);
                return userServiceES.findById(id);
            }

            @DeleteMapping("/{id}")
            public String deleteUser(@PathVariable String id) {
                logger.info("Delete user by id {}.", id);
                return userServiceES.deleteProfileDocument(id);

            }

            @GetMapping(value = "/search")
            public List<User> search(@RequestParam(value = "lastName") String lastName) {
                logger.info("Find user by last name {}.", lastName);
                return userServiceES.searchByLastName(lastName);
            }

            @GetMapping
            public List<User> findAll() {
                logger.info("Find all users.");
                return userServiceES.findAll();
            }
          }
      ```
