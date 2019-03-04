package com.tll.mcorpus.gql;

import static com.tll.TestUtil.cpr;
import static com.tll.mcorpus.McorpusTestUtil.ds_mcweb;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tll.UnitTest;
import com.tll.jwt.JWT;
import com.tll.jwt.JWTStatus;
import com.tll.jwt.JWTStatusInstance;
import com.tll.mcorpus.repo.MCorpusRepo;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.web.GraphQLWebContext;
import com.tll.web.RequestSnapshot;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

@Category(UnitTest.class)
public class MCorpusGraphQLTest {
  private static final Logger log = LoggerFactory.getLogger(MCorpusGraphQLTest.class);

  static MCorpusGraphQL mcgql() {
    return new MCorpusGraphQL(new MCorpusUserRepo(ds_mcweb()), new MCorpusRepo(ds_mcweb()));
  }

  /**
   * Issue a GraphQL query with a context of VALID jwt status under role MCORPUS.
   */
  static ExecutionResult query(final String query) {
    return query(query, JWTStatus.VALID, "MCORPUS");
  }
  
  /**
   * Issue a GraphQL query with with a context of VALID jwt status under a given role.
   */
  static ExecutionResult query(final String query, String role) {
	  return query(query, JWTStatus.VALID, role);
  }
	  
  static final ObjectMapper mapper = new ObjectMapper();

  static final TypeReference<Map<String, Object>> strObjMapTypeRef = new TypeReference<Map<String, Object>>() { };

  /**
   * Converts a JSON string to a map.
   *
   * <p>Requirement: the given json string is assumed to have a
   * root object ref with enclosing name and object key/value pairs.</p>
   *
   * <p>Example:
   * <pre>
   *   "{ \"query\": \"...\", \"variables\":\"...\", ... }"
   * </pre>
   * </p>
   *
   * @param json the JSON string
   * @return newly created {@link Map} with parsed name/values from the JSON string
   * @throws Exception when the json to map conversion fails for some reason.
   */
  static Map<String, Object> jsonStringToMap(final String json) throws Exception {
    try {
      // convert JSON string to Map
      return mapper.readValue(json, strObjMapTypeRef);
    }
    catch(Throwable t) {
      throw new Exception("JSON to map failed: " + t.getMessage());
    }
  }

  static RequestSnapshot testRequestSnapshot() {
    return new RequestSnapshot(
        Instant.now(),
        "127.0.0.1",
        "localhost",
        "origin",
        "https://mcorpus.d2d",
        "forwarded",
        "X-Forwarded-For",
        "X-Forwarded-Proto",
        "X-Forwarded-Port",
        null, // jwt cookie
        null, // rst cookie
        null // rst header
    );
  }

  static JWTStatusInstance testJwtStatus(JWTStatus jwtStatus, String roles) {
    return JWT.jsi(
      jwtStatus,
      UUID.randomUUID(),
      UUID.randomUUID(),
      roles, 
      new Date(Instant.now().toEpochMilli()),
      new Date(Instant.now().toEpochMilli())
    );
  }

  /**
   * Issue a GraphQL query with with a context of the given jwt status and role.
   */
  static ExecutionResult query(final String query, JWTStatus jwtStatus, String roles) {
    final GraphQLSchema schema = mcgql().getGraphQLSchema();
    final GraphQL graphQL = GraphQL.newGraphQL(schema).build();
    final RequestSnapshot requestSnapshot = testRequestSnapshot();
    final JWTStatusInstance jsi = testJwtStatus(jwtStatus, roles);
    final GraphQLWebContext context = new GraphQLWebContext(query, null, requestSnapshot, jsi, null);
    final ExecutionInput executionInput = 
      ExecutionInput.newExecutionInput()
        .query(query)
        .context(context)
        .build();
    final ExecutionResult result = graphQL.execute(executionInput);
    return result;
  }

  /**
   * Issue a GraphQL query with NO context object.
   */
  static ExecutionResult queryNoContext(final String query) {
    final GraphQLSchema schema = mcgql().getGraphQLSchema();
    final GraphQL graphQL = GraphQL.newGraphQL(schema).build();
    final ExecutionInput executionInput = 
      ExecutionInput.newExecutionInput()
        .query(query)
        .build();
    final ExecutionResult result = graphQL.execute(executionInput);
    return result;
  }
  
  @Test
  public void testJsonStringToMap() throws Exception {
    final String json = cpr("introspect.gql");
    Map<String, Object> rmap = jsonStringToMap(json);
    assertNotNull(rmap);
    assertTrue(rmap.size() == 1);
    assertTrue(rmap.containsKey("query"));
  }
  
  @Test
  public void testLoadSchema() {
    final MCorpusGraphQL g = mcgql();
    g.loadSchema();
    log.info("MCorpus GraphQL schema loaded.");
    assertNotNull(g.getGraphQLSchema());
  }

  @Test
  public void testIntrospectQuery() throws Exception {
    final String rawQuery = cpr("introspect.gql");
    log.info("rawQuery:\n{}\n***", rawQuery);

    final Map<String, Object> qmap = jsonStringToMap(rawQuery);
    log.info("qmap: {}", qmap);
    assertNotNull(qmap);
    assertTrue(qmap.size() == 1);
    assertTrue(qmap.containsKey("query"));

    final String introspectQuery = (String) qmap.get("query");
    assertNotNull(introspectQuery);

    final ExecutionResult result = queryNoContext(introspectQuery);
    assertNotNull(result);
    assertTrue(result.getErrors().isEmpty());

    final Map<?, ?> rmap = result.getData();
    log.info("result map:\n{}", rmap);
    assertNotNull(rmap);
  }

  @Test
  public void testSchemaQuery() {
    log.info("Testing mcorpus gql with simple query..");
    final ExecutionResult result = queryNoContext("query { mrefByMid(mid: \"bLYU_FNrT6O3T917UPSAbw==\") { mid\nempId\nlocation} }");
    assertNotNull(result);
    assertTrue(result.getErrors().isEmpty());
    final Map<?, ?> rmap = result.getData();
    log.info("result map:\n{}", rmap);
    assertNotNull(rmap);
  }

  @Test
  public void testSimpleQueryParse() throws Exception {
    final String initialQuery = "{\"query\":\"{\\n  mrefByMid(mid: \\\"bLYU_FNrT6O3T917UPSAbw==\\\") {\\n    empId\\n  }\\n}\",\"variables\":null,\"operationName\":null}";

    final Map<String, Object> qmap = jsonStringToMap(initialQuery);
    log.info("qmap: {}", qmap);

    final String query = (String) qmap.get("query");
    assertNotNull(query);

    final ExecutionResult result = query(query);
    assertNotNull(result);
    assertTrue(result.getErrors().isEmpty());

    final Map<?, ?> rmap = result.getData();
    log.info("result map:\n{}", rmap);
    assertNotNull(rmap);
  }

  @Test
  public void testAuthorization_success() throws Exception {
    final String gql = "{\"query\":\"{\\n  mrefByMid(mid: \\\"001ea236-12be-410a-9586-1bc6c2b2c89c\\\") {\\n    empId\\n  }\\n}\",\"variables\":null,\"operationName\":null}";

    final Map<String, Object> qmap = jsonStringToMap(gql);
    log.info("qmap: {}", qmap);

    final String query = (String) qmap.get("query");
    assertNotNull(query);

    final ExecutionResult result = query(query, "PUBLIC");
    assertNotNull(result);
    assertTrue(result.getErrors().isEmpty());

    final Map<?, ?> rmap = result.getData();
    log.info("result map:\n{}", rmap);
    assertNotNull(rmap);
    
    // verify we have data returned (authorized)
    final Object output = rmap.get("mrefByMid");
    assertNotNull(output);
  }

  @Test
  public void testAuthorization_fail() throws Exception {
    final String initialQuery = "{\"query\":\"{\\n  memberByMid(mid: \\\"bLYU_FNrT6O3T917UPSAbw==\\\") {\\n    empId\\n  }\\n}\",\"variables\":null,\"operationName\":null}";

    final Map<String, Object> qmap = jsonStringToMap(initialQuery);
    log.info("qmap: {}", qmap);

    final String query = (String) qmap.get("query");
    assertNotNull(query);

    final ExecutionResult result = query(query, "PUBLIC");
    assertNotNull(result);
    assertTrue(result.getErrors().isEmpty());

    final Map<?, ?> rmap = result.getData();
    log.info("result map:\n{}", rmap);
    assertNotNull(rmap);

    // verify we have no data returned (un-authorized)
    final Object output = rmap.get("memberByMid");
    assertNull(output);
  }
}
