package com.tll.mcorpus.gql;

import static com.tll.mcorpus.TestUtil.cpr;
import static com.tll.mcorpus.TestUtil.jsonStringToMap;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.UnitTest;
import com.tll.mcorpus.repo.MCorpusRepoAsync;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

@Category(UnitTest.class)
public class MCorpusGraphQLTest {
  private static final Logger log = LoggerFactory.getLogger(MCorpusGraphQLTest.class);

  private static DataSource ds() {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setUrl("jdbc:postgresql://localhost:5432/mcorpus");
    ds.setUser("mcweb");
    ds.setPassword("YAcsR6*-L;djIaX1~%zBa");
    ds.setCurrentSchema("public");
    return ds;
  }

  private static MCorpusGraphQL mcgql() {
    return new MCorpusGraphQL("mcorpus.graphqls", new MCorpusRepoAsync(ds()));
  }

  private static ExecutionResult query(final String query) {
    final GraphQLSchema schema = mcgql().getGraphQLSchema();
    final GraphQL graphQL = GraphQL.newGraphQL(schema).build();
    final ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(query).build();
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

    final ExecutionResult result = query(introspectQuery);
    assertNotNull(result);
    assertTrue(result.getErrors().isEmpty());

    final Map<?, ?> rmap = result.getData();
    log.info("result map:\n{}", rmap);
    assertNotNull(rmap);
  }

  @Test
  public void testSchemaQuery() {
    log.info("Testing mcorpus gql with simple query..");
    final ExecutionResult result = query("query { mrefByMid(mid: \"bLYU_FNrT6O3T917UPSAbw==\") { mid\nempId\ntid\nlocation} }");
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
}
