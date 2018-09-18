package com.tll.mcorpus.gql;

import static com.tll.mcorpus.TestUtil.cpr;
import static com.tll.mcorpus.TestUtil.ds_mcweb;
import static com.tll.mcorpus.TestUtil.jsonStringToMap;
import static com.tll.mcorpus.TestUtil.testRequestSnapshot;
import static com.tll.mcorpus.TestUtil.testJwtStatus;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.UnitTest;
import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.repo.MCorpusRepo;
import com.tll.mcorpus.web.GraphQLWebQuery;
import com.tll.mcorpus.web.RequestSnapshot;
import com.tll.mcorpus.web.JWT.JWTStatus;
import com.tll.mcorpus.web.JWT.JWTStatusInstance;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

@Category(UnitTest.class)
public class MCorpusGraphQLTest {
  private static final Logger log = LoggerFactory.getLogger(MCorpusGraphQLTest.class);

  static MCorpusGraphQL mcgql() {
    return new MCorpusGraphQL("mcorpus.graphqls", new MCorpusRepo(ds_mcweb()));
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
	  
  /**
   * Issue a GraphQL query with with a context of the given jwt status and role.
   */
  static ExecutionResult query(final String query, JWTStatus jwtStatus, String roles) {
    final GraphQLSchema schema = mcgql().getGraphQLSchema();
    final GraphQL graphQL = GraphQL.newGraphQL(schema).build();
    final RequestSnapshot requestSnapshot = testRequestSnapshot();
    final JWTStatusInstance jsi = testJwtStatus(jwtStatus, roles);
    final GraphQLWebQuery context = new GraphQLWebQuery(query, null, requestSnapshot, jsi);
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
    final String gql = "{\"query\":\"{\\n  mrefByMid(mid: \\\"bLYU_FNrT6O3T917UPSAbw==\\\") {\\n    empId\\n  }\\n}\",\"variables\":null,\"operationName\":null}";

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
