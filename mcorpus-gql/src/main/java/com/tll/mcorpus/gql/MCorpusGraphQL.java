package com.tll.mcorpus.gql;

import static com.tll.mcorpus.Util.asStringAndClean;
import static com.tll.mcorpus.Util.dflt;
import static com.tll.mcorpus.Util.emptyIfNull;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.lower;
import static com.tll.mcorpus.Util.uuidFromToken;
import static com.tll.mcorpus.Util.uuidToToken;
import static com.tll.mcorpus.db.Tables.MADDRESS;
import static com.tll.mcorpus.db.Tables.MAUTH;
import static com.tll.mcorpus.db.Tables.MEMBER;
import static com.tll.mcorpus.repo.RepoUtil.fput;
import static com.tll.mcorpus.repo.RepoUtil.fputWhenNotBlank;
import static com.tll.mcorpus.repo.RepoUtil.fputWhenNotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.db.udt.pojos.Mref;
import com.tll.mcorpus.repo.MCorpusRepo;
import com.tll.mcorpus.repo.model.FetchResult;
import com.tll.mcorpus.repo.model.MemberFilter;
import com.tll.mcorpus.web.GraphQLWebQuery;
import com.tll.mcorpus.web.RequestSnapshot;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class MCorpusGraphQL {

  private final Logger log = LoggerFactory.getLogger(MCorpusGraphQL.class);

  private final String graphqlSchemaFilename;

  private final MCorpusRepo mCorpusRepo;

  private GraphQLSchema graphQLSchema = null;

  /**
   * Constructor.
   *
   * @param graphqlSchemaFilename the GraphQL schema file name (no path)
   * @param mCorpusRepo required mcorpus repo
   */
  MCorpusGraphQL(final String graphqlSchemaFilename, final MCorpusRepo mCorpusRepo) {
    this.graphqlSchemaFilename = graphqlSchemaFilename;
    this.mCorpusRepo = mCorpusRepo;
  }

  /**
   * Lazy-loading getter method for obtaining the GraphQL schema object.
   *
   * @return the never-null {@link GraphQLSchema} instance.
   * @throws RuntimeException upon any error during load
   */
  public GraphQLSchema getGraphQLSchema() throws RuntimeException {
    if(graphQLSchema == null) {
      loadSchema();
    }
    if(graphQLSchema == null) throw new RuntimeException("No GraphQL schema loaded.");
    return graphQLSchema;
  }

  /**
   * Load the GraphQL schema.
   *
   * @throws RuntimeException upon any error during load.
   */
  public void loadSchema() throws RuntimeException {
    SchemaParser schemaParser = new SchemaParser();
    SchemaGenerator schemaGenerator = new SchemaGenerator();

    InputStreamReader greader = null;
    try {
      log.debug("Loading GraphQL schema file '{}'..", graphqlSchemaFilename);
      greader = new InputStreamReader(
          Thread.currentThread().getContextClassLoader().getResourceAsStream(graphqlSchemaFilename),
          StandardCharsets.UTF_8
      );

      TypeDefinitionRegistry typeRegistry = schemaParser.parse(greader);
      RuntimeWiring wiring = buildRuntimeWiring();

      graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring);
      log.info("GraphQL schema loaded from '{}'.", graphqlSchemaFilename);
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
    finally {
      if(greader != null) { 
        try { 
          greader.close(); 
        } catch (IOException e) {
          log.error("Error closing handle to GraphQL schema file.");
        }
      }
    }
  }

  private RuntimeWiring buildRuntimeWiring() {
    return RuntimeWiring.newRuntimeWiring()

      .scalar(new GraphQLDate())

      // Query
      .type("Query", typeWiring -> typeWiring

        // member login
        .dataFetcher("mlogin", env -> {
          final GraphQLWebQuery webContext = env.getContext();
          final RequestSnapshot rs = webContext.getRequestSnapshot();
          final String username = asStringAndClean(env.getArgument("username"));
          final String pswd = asStringAndClean(env.getArgument("pswd"));
          final Instant requestInstant = rs.getRequestInstant();
          final String requestOrigin = rs.getClientOrigin();
          final FetchResult<Mref> mloginResult = mCorpusRepo.memberLogin(username, pswd, requestInstant, requestOrigin);
          return mloginResult.isSuccess() ? mloginResult.get() : null;
        })
        
        // member logout
        .dataFetcher("mlogout", env -> {
          final GraphQLWebQuery webContext = env.getContext();
          final RequestSnapshot rs = webContext.getRequestSnapshot();
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final Instant requestInstant = rs.getRequestInstant();
          final String requestOrigin = rs.getClientOrigin();
          final FetchResult<UUID> mlogoutResult = mCorpusRepo.memberLogout(mid, requestInstant, requestOrigin);
          return mlogoutResult.isSuccess() ? uuidToToken(mid) : null;
        })
        
        .dataFetcher("mrefByMid", env -> {
          final UUID uuid = uuidFromToken(env.getArgument("mid"));
          final FetchResult<Mref> memberRefFetchResult = mCorpusRepo.fetchMRefByMid(uuid);
          return memberRefFetchResult.isSuccess() ? memberRefFetchResult.get() : null;
        })
        .dataFetcher("mrefByEmpIdAndLoc", env -> {
          final String empId = env.getArgument("empId");
          final Location location = locationFromString(env.getArgument("location"));
          final FetchResult<Mref> memberRefFetchResult = mCorpusRepo.fetchMRefByEmpIdAndLoc(empId, location);
          return memberRefFetchResult.isSuccess() ? memberRefFetchResult.get() : null;
        })
        .dataFetcher("mrefsByEmpId", env -> {
          final String empId = env.getArgument("empId");
          final FetchResult<List<Mref>> memberRefsFetchResult = mCorpusRepo.fetchMRefsByEmpId(empId);
          return memberRefsFetchResult.isSuccess() ? memberRefsFetchResult.get() : null;
        })
        .dataFetcher("memberByMid", env -> {
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final FetchResult<Map<String, Object>> memberFetchResult = mCorpusRepo.fetchMember(mid);
          return memberFetchResult.isSuccess() ? memberFetchResult.get() : null;
        })
        .dataFetcher("members", env -> {
          final MemberFilter filter = MemberFilter.fromMap(env.getArgument("filter"));
          final int offset = dflt(env.getArgument("offset"), 0);
          final int limit = dflt(env.getArgument("limit"), 10);
          final FetchResult<List<Map<String, Object>>> membersFetchResult = mCorpusRepo.memberSearch(filter, offset, limit);
          return membersFetchResult.isSuccess() ? membersFetchResult.get() : null;
        })
      )

      // Mutation
      .type("Mutation", typeWiring -> typeWiring

        // add member
        .dataFetcher("addMember", env -> {
          final Map<String, Object> memberMap = env.getArgument("member");

          final String empId = (String) memberMap.get("empId");
          final Location location = locationFromString((String) memberMap.get("location"));
          final String nameFirst = (String) memberMap.get("nameFirst");
          final String nameMiddle = (String) memberMap.get("nameMiddle");
          final String nameLast = (String) memberMap.get("nameLast");
          final String displayName = (String) memberMap.get("displayName");
          final MemberStatus status = memberStatusFromString((String) memberMap.get("status"));
          final Date dob = (Date) memberMap.get("dob");
          final java.sql.Date dobSql = dob == null ? null : new java.sql.Date(dob.getTime());
          final String ssn = (String) memberMap.get("ssn");
          final String personalEmail = (String) memberMap.get("personalEmail");
          final String workEmail = (String) memberMap.get("workEmail");
          final String mobilePhone = (String) memberMap.get("mobilePhone");
          final String homePhone = (String) memberMap.get("homePhone");
          final String workPhone = (String) memberMap.get("workPhone");
          final String username = (String) memberMap.get("username");
          final String pswd = (String) memberMap.get("pswd");

          final Map<String, Object> memberMapDomain = new HashMap<>(15);
          // member
          fput(MEMBER.EMP_ID, empId, memberMapDomain);
          fput(MEMBER.LOCATION, location, memberMapDomain);
          fput(MEMBER.NAME_FIRST, nameFirst, memberMapDomain);
          fput(MEMBER.NAME_MIDDLE, nameMiddle, memberMapDomain);
          fput(MEMBER.NAME_LAST, nameLast, memberMapDomain);
          fput(MEMBER.DISPLAY_NAME, displayName, memberMapDomain);
          fput(MEMBER.STATUS, status, memberMapDomain);
          // mauth
          fput(MAUTH.DOB, dobSql, memberMapDomain);
          fput(MAUTH.SSN, ssn, memberMapDomain);
          fput(MAUTH.EMAIL_PERSONAL, personalEmail, memberMapDomain);
          fput(MAUTH.EMAIL_WORK, workEmail, memberMapDomain);
          fput(MAUTH.MOBILE_PHONE, mobilePhone, memberMapDomain);
          fput(MAUTH.WORK_PHONE, workPhone, memberMapDomain);
          fput(MAUTH.HOME_PHONE, homePhone, memberMapDomain);
          fput(MAUTH.USERNAME, username, memberMapDomain);
          fput(MAUTH.PSWD, pswd, memberMapDomain);

          final FetchResult<Map<String, Object>> fetchResult = mCorpusRepo.addMember(memberMapDomain);
          return fetchResult.isSuccess() ? fetchResult.get() : null;
        })

        // update member
        .dataFetcher("updateMember", env -> {
          final Map<String, Object> memberMap = env.getArgument("member");

          final UUID mid = uuidFromToken((String) memberMap.get("mid"));
          final String nameFirst = (String) memberMap.get("nameFirst");
          final String nameMiddle = (String) memberMap.get("nameMiddle");
          final String nameLast = (String) memberMap.get("nameLast");
          final String displayName = (String) memberMap.get("displayName");
          final MemberStatus status = memberStatusFromString((String) memberMap.get("status"));
          final Date dob = (Date) memberMap.get("dob");
          final java.sql.Date dobSql = dob == null ? null : new java.sql.Date(dob.getTime());
          final String ssn = (String) memberMap.get("ssn");
          final String personalEmail = (String) memberMap.get("personalEmail");
          final String workEmail = (String) memberMap.get("workEmail");
          final String mobilePhone = (String) memberMap.get("mobilePhone");
          final String homePhone = (String) memberMap.get("homePhone");
          final String workPhone = (String) memberMap.get("workPhone");

          final Map<String, Object> memberMapDomain = new HashMap<>(11);
          // member
          // required
          fput(MEMBER.MID, mid, memberMapDomain);
          // optional
          fputWhenNotBlank(MEMBER.NAME_FIRST, nameFirst, memberMapDomain);
          fputWhenNotBlank(MEMBER.NAME_MIDDLE, nameMiddle, memberMapDomain);
          fputWhenNotBlank(MEMBER.NAME_LAST, nameLast, memberMapDomain);
          fputWhenNotBlank(MEMBER.DISPLAY_NAME, displayName, memberMapDomain);
          fputWhenNotNull(MEMBER.STATUS, status, memberMapDomain);
          // mauth
          fputWhenNotNull(MAUTH.DOB, dobSql, memberMapDomain);
          fputWhenNotBlank(MAUTH.SSN, ssn, memberMapDomain);
          fputWhenNotBlank(MAUTH.EMAIL_PERSONAL, personalEmail, memberMapDomain);
          fputWhenNotBlank(MAUTH.EMAIL_WORK, workEmail, memberMapDomain);
          fputWhenNotBlank(MAUTH.MOBILE_PHONE, mobilePhone, memberMapDomain);
          fputWhenNotBlank(MAUTH.HOME_PHONE, homePhone, memberMapDomain);
          fputWhenNotBlank(MAUTH.WORK_PHONE, workPhone, memberMapDomain);

          final FetchResult<Map<String, Object>> fetchResult = mCorpusRepo.updateMember(memberMapDomain);
          return fetchResult.isSuccess() ? fetchResult.get() : null;
        })

        // delete member
        .dataFetcher("deleteMember", env -> {
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final FetchResult<UUID> fetchResult = mCorpusRepo.deleteMember(mid);
          return fetchResult.isSuccess() ? uuidToToken(fetchResult.get()) : null;
        })

        // add member address
        .dataFetcher("addMemberAddress", env -> {
          final Map<String, Object> memberAddress = env.getArgument("memberAddress");

          final UUID mid = uuidFromToken((String) memberAddress.get("mid"));
          final Addressname addressName = addressNameFromString((String) memberAddress.get("addressName"));

          final String attn = (String) memberAddress.get("attn");
          final String street1 = (String) memberAddress.get("street1");
          final String street2 = (String) memberAddress.get("street2");
          final String city = (String) memberAddress.get("city");
          final String state = (String) memberAddress.get("state");
          final String postalCode = (String) memberAddress.get("postalCode");
          final String country = (String) memberAddress.get("country");

          final Map<String, Object> maddressMap = new HashMap<>(8);
          fput(MADDRESS.MID, mid, maddressMap);
          fput(MADDRESS.ADDRESS_NAME, addressName, maddressMap);

          fput(MADDRESS.ATTN, attn, maddressMap);
          fput(MADDRESS.STREET1, street1, maddressMap);
          fput(MADDRESS.STREET2, street2, maddressMap);
          fput(MADDRESS.CITY, city, maddressMap);
          fput(MADDRESS.STATE, state, maddressMap);
          fput(MADDRESS.POSTAL_CODE, postalCode, maddressMap);
          fput(MADDRESS.COUNTRY, country, maddressMap);

          final FetchResult<Map<String, Object>> fetchResult = mCorpusRepo.addMemberAddress(maddressMap);
          return fetchResult.isSuccess() ? fetchResult.get() : null;
        })
        // update member address
        .dataFetcher("updateMemberAddress", env -> {
          final Map<String, Object> memberAddress = env.getArgument("memberAddress");

          final UUID mid = uuidFromToken((String) memberAddress.get("mid"));
          final Addressname addressName = addressNameFromString((String) memberAddress.get("addressName"));
          final String attn = (String) memberAddress.get("attn");
          final String street1 = (String) memberAddress.get("street1");
          final String street2 = (String) memberAddress.get("street2");
          final String city = (String) memberAddress.get("city");
          final String state = (String) memberAddress.get("state");
          final String postalCode = (String) memberAddress.get("postalCode");
          final String country = (String) memberAddress.get("country");

          final Map<String, Object> maddressMap = new HashMap<>(8);
          // required
          fput(MADDRESS.MID, mid, maddressMap);
          fput(MADDRESS.ADDRESS_NAME, addressName, maddressMap);
          // optional
          fputWhenNotBlank(MADDRESS.ATTN, attn, maddressMap);
          fputWhenNotBlank(MADDRESS.STREET1, street1, maddressMap);
          fputWhenNotBlank(MADDRESS.STREET2, street2, maddressMap);
          fputWhenNotBlank(MADDRESS.CITY, city, maddressMap);
          fputWhenNotBlank(MADDRESS.STATE, state, maddressMap);
          fputWhenNotBlank(MADDRESS.POSTAL_CODE, postalCode, maddressMap);
          fputWhenNotBlank(MADDRESS.COUNTRY, country, maddressMap);

          final FetchResult<Map<String, Object>> fetchResult = mCorpusRepo.updateMemberAddress(maddressMap);
          return fetchResult.isSuccess() ? fetchResult.get() : null;
        })
        // delete member address
        .dataFetcher("deleteMemberAddress", env -> {
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final Addressname addressname = addressNameFromString(env.getArgument("addressName"));
          final FetchResult<UUID> fetchResult = mCorpusRepo.deleteMemberAddress(mid, addressname);
          return fetchResult.isSuccess() ? uuidToToken(fetchResult.get()) : null;
        })
      )

      // MRef
      .type("MRef", typeWiring -> typeWiring
        .dataFetcher("mid", env -> {
          final Mref mref = env.getSource();
          return mref == null ? null : uuidToToken(mref.getMid());
        })
        .dataFetcher("empId", env -> {
          final Mref mref = env.getSource();
          return mref == null ? null : mref.getEmpId();
        })
        .dataFetcher("location", env -> {
          final Mref mref = env.getSource();
          return mref == null ? null : locationToString(mref.getLocation());
        })
      )

      // Member
      .type("Member", typeWiring -> typeWiring
        .dataFetcher("mid", env -> {
          final Map<String, Object> m = env.getSource();
          final UUID mid = m == null ? null : (UUID) m.get(MEMBER.MID.getName());
          return mid == null ? null : uuidToToken(mid);
        })
        .dataFetcher("created", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MEMBER.CREATED.getName());
        })
        .dataFetcher("modified", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MEMBER.MODIFIED.getName());
        })
        .dataFetcher("empId", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MEMBER.EMP_ID.getName());
        })
        .dataFetcher("location", env -> {
          final Map<String, Object> m = env.getSource();
          final Location loc = m == null ? null : (Location) m.get(MEMBER.LOCATION.getName());
          return locationToString(loc);
        })
        .dataFetcher("nameFirst", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MEMBER.NAME_FIRST.getName());
        })
        .dataFetcher("nameMiddle", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MEMBER.NAME_MIDDLE.getName());
        })
        .dataFetcher("nameLast", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MEMBER.NAME_LAST.getName());
        })
        .dataFetcher("displayName", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MEMBER.DISPLAY_NAME.getName());
        })
        .dataFetcher("status", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : memberStatusToString((MemberStatus) m.get(MEMBER.STATUS.getName()));
        })
        .dataFetcher("dob", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MAUTH.DOB.getName());
        })
        .dataFetcher("ssn", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MAUTH.SSN.getName());
        })
        .dataFetcher("personalEmail", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MAUTH.EMAIL_PERSONAL.getName());
        })
        .dataFetcher("workEmail", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MAUTH.EMAIL_WORK.getName());
        })
        .dataFetcher("mobilePhone", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MAUTH.MOBILE_PHONE.getName());
        })
        .dataFetcher("homePhone", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MAUTH.HOME_PHONE.getName());
        })
        .dataFetcher("workPhone", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MAUTH.WORK_PHONE.getName());
        })
        .dataFetcher("username", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : m.get(MAUTH.USERNAME.getName());
        })
        .dataFetcher("addresses", env -> {
          final Map<String, Object> m = env.getSource();
          final FetchResult<List<Map<String, Object>>> memberAddressesFetchResult = mCorpusRepo.fetchMemberAddresses((UUID) m.get(MADDRESS.MID.getName()));
          return memberAddressesFetchResult.isSuccess() ? memberAddressesFetchResult.get() : null;
        })
      )

      // MemberAddress
      .type("MemberAddress", typeWiring -> typeWiring
        .dataFetcher("mid", env -> {
          final Map<String, Object> ma = env.getSource();
          final UUID mid = ma == null ? null : (UUID) ma.get(MADDRESS.MID.getName());
          return mid == null ? null : uuidToToken(mid);
        })
        .dataFetcher("addressName", env -> {
          final Map<String, Object> ma = env.getSource();
          final Addressname aname = ma == null ? null : (Addressname) ma.get(MADDRESS.ADDRESS_NAME.getName());
          return addressNameToString(aname);
        })
        .dataFetcher("modified", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : ma.get(MADDRESS.MODIFIED.getName());
        })
        .dataFetcher("attn", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : ma.get(MADDRESS.ATTN.getName());
        })
        .dataFetcher("street1", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : ma.get(MADDRESS.STREET1.getName());
        })
        .dataFetcher("street2", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : ma.get(MADDRESS.STREET2.getName());
        })
        .dataFetcher("city", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : ma.get(MADDRESS.CITY.getName());
        })
        .dataFetcher("state", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : ma.get(MADDRESS.STATE.getName());
        })
        .dataFetcher("postalCode", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : ma.get(MADDRESS.POSTAL_CODE.getName());
        })
        .dataFetcher("country", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : ma.get(MADDRESS.COUNTRY.getName());
        })
      )

      .build();
  }

  private static Location locationFromString(final String location) {
    final String sloc = emptyIfNull(location).startsWith("L") ? location.substring(1) : location;
    for(final Location enmLoc : Location.values()) {
      if(enmLoc.getLiteral().equals(sloc)) return enmLoc;
    }
    // default
    return null;
  }

  private static String locationToString(Location location) {
    return location == null ? null : "L" + location.getLiteral();
  }

  private static MemberStatus memberStatusFromString(final String memberStatus) {
    return isNullOrEmpty(memberStatus) ? null : MemberStatus.valueOf(memberStatus);
  }

  private static String memberStatusToString(MemberStatus memberStatus) {
    return memberStatus == null ? null : memberStatus.name();
  }

  private static Addressname addressNameFromString(final String addressName) {
    return isNullOrEmpty(addressName) ? null : Addressname.valueOf(lower(addressName));
  }

  private static String addressNameToString(Addressname addressname) {
    return addressname == null ? null : addressname.getLiteral().toUpperCase(Locale.US);
  }
}
