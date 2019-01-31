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
import static com.tll.mcorpus.repo.RepoUtil.fval;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.db.udt.pojos.Mref;
import com.tll.mcorpus.repo.MCorpusRepo;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.repo.model.FetchResult;
import com.tll.mcorpus.repo.model.McuserHistory;
import com.tll.mcorpus.repo.model.McuserToAdd;
import com.tll.mcorpus.repo.model.McuserToUpdate;
import com.tll.mcorpus.repo.model.McuserHistory.LoginEvent;
import com.tll.mcorpus.repo.model.McuserHistory.LogoutEvent;
import com.tll.mcorpus.repo.model.MemberFilter;
import com.tll.mcorpus.web.GraphQLWebContext;
import com.tll.mcorpus.web.RequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.language.SourceLocation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;

public class MCorpusGraphQL {

  private final Logger log = LoggerFactory.getLogger(MCorpusGraphQL.class);

  private final MCorpusUserRepo mcuserRepo;

  private final MCorpusRepo mcorpusRepo;

  private GraphQLSchema graphQLSchema = null;

  /**
   * Constructor.
   *
   * @param graphqlSchemaFilename the GraphQL schema file name (no path)
   * @param mcuserRepo required mcuser repo
   * @param mcorpusRepo required mcorpus repo
   */
  MCorpusGraphQL(final MCorpusUserRepo mcuserRepo, final MCorpusRepo mcorpusRepo) {
    this.mcuserRepo = mcuserRepo;
    this.mcorpusRepo = mcorpusRepo;
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
      if(graphQLSchema == null) throw new RuntimeException("No GraphQL schema loaded.");
    }
    return graphQLSchema;
  }

  /**
   * Load the GraphQL schema.
   *
   * @throws RuntimeException upon any error during load.
   */
  public void loadSchema() throws RuntimeException {
    final SchemaParser schemaParser = new SchemaParser();
    final SchemaGenerator schemaGenerator = new SchemaGenerator();
    try (
      final InputStreamReader rdrMcuser = new InputStreamReader(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("mcuser.graphqls"), StandardCharsets.UTF_8);
      final InputStreamReader rdrMcorpus = new InputStreamReader(
          Thread.currentThread().getContextClassLoader().getResourceAsStream("mcorpus.graphqls"), StandardCharsets.UTF_8);
      ) {
      log.debug("Loading mcorpus GraphQL schema(s)..");

      final TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
      typeRegistry.merge(schemaParser.parse(rdrMcuser));
      typeRegistry.merge(schemaParser.parse(rdrMcorpus));
      
      final RuntimeWiring wiring = buildRuntimeWiring();
      graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring);

      log.info("GraphQL mcorpus schema(s) loaded.");
    }
    catch (Exception e) {
      log.error("GraphQL load schema error: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private RuntimeWiring buildRuntimeWiring() {
    return RuntimeWiring.newRuntimeWiring()

      .scalar(new GraphQLDate())
      .directive("auth", new AuthorizationDirective())

      // Query
      .type("Query", typeWiring -> typeWiring

        // mcuser

        // mcuser status
        .dataFetcher("mcstatus", env -> {
          final GraphQLWebContext webContext = env.getContext();
          return webContext.mcstatus();
        })

        // mcuser history
        .dataFetcher("mchistory", env -> {
          final UUID uid = uuidFromToken(env.getArgument("uid"));
          final FetchResult<McuserHistory> fr = mcuserRepo.mcuserHistory(uid);
          return fr.isSuccess() ? fr.get() : null;
        })

        // fetch mcuser
        .dataFetcher("fetchMcuser", env -> {
          final UUID uid = uuidFromToken(env.getArgument("uid"));
          final FetchResult<Mcuser> fr = mcuserRepo.fetchMcuser(uid);
          return fr.isSuccess() ? fr.get() : null;
        })

        // mcorpus

        .dataFetcher("mrefByMid", env -> {
          final UUID uuid = uuidFromToken(env.getArgument("mid"));
          final FetchResult<Mref> memberRefFetchResult = mcorpusRepo.fetchMRefByMid(uuid);
          return memberRefFetchResult.isSuccess() ? memberRefFetchResult.get() : null;
        })
        .dataFetcher("mrefByEmpIdAndLoc", env -> {
          final String empId = env.getArgument("empId");
          final Location location = locationFromString(env.getArgument("location"));
          final FetchResult<Mref> memberRefFetchResult = mcorpusRepo.fetchMRefByEmpIdAndLoc(empId, location);
          return memberRefFetchResult.isSuccess() ? memberRefFetchResult.get() : null;
        })
        .dataFetcher("mrefsByEmpId", env -> {
          final String empId = env.getArgument("empId");
          final FetchResult<List<Mref>> memberRefsFetchResult = mcorpusRepo.fetchMRefsByEmpId(empId);
          return memberRefsFetchResult.isSuccess() ? memberRefsFetchResult.get() : null;
        })
        .dataFetcher("memberByMid", env -> {
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final FetchResult<Map<String, Object>> memberFetchResult = mcorpusRepo.fetchMember(mid);
          return memberFetchResult.isSuccess() ? memberFetchResult.get() : null;
        })
        .dataFetcher("members", env -> {
          final MemberFilter filter = MemberFilter.fromMap(env.getArgument("filter"));
          final int offset = dflt(env.getArgument("offset"), 0);
          final int limit = dflt(env.getArgument("limit"), 10);
          final FetchResult<List<Map<String, Object>>> membersFetchResult = mcorpusRepo.memberSearch(filter, offset, limit);
          return membersFetchResult.isSuccess() ? membersFetchResult.get() : null;
        })
      )

      // Mutation
      .type("Mutation", typeWiring -> typeWiring

        // mcuser

        // mcuser login
        .dataFetcher("mclogin", env -> {
          final GraphQLWebContext webContext = env.getContext();
          final String username = asStringAndClean(env.getArgument("username"));
          final String pswd = asStringAndClean(env.getArgument("pswd"));
          return webContext.mcuserLogin(username, pswd);
        })

        // mcuser logout
        .dataFetcher("mclogout", env -> {
          final GraphQLWebContext webContext = env.getContext();
          return webContext.mcuserLogout();
        })

        // add mcuser
        .dataFetcher("addMcuser", env -> {
          final Map<String, Object> mcuserMap = env.getArgument("mcuser");
          final McuserToAdd mta = McuserToAdd.fromMap(mcuserMap);
          final FetchResult<Mcuser> fr = mcuserRepo.addMcuser(mta);
          return fr.isSuccess() ? fr.get() : null;
        })

        // update mcuser
        .dataFetcher("updateMcuser", env -> {
          final Map<String, Object> mcuserMap = env.getArgument("mcuser");
          final McuserToUpdate mtu = McuserToUpdate.fromMap(mcuserMap);
          final FetchResult<Mcuser> fr = mcuserRepo.updateMcuser(mtu);
          return fr.isSuccess() ? fr.get() : null;
        })

        // delete mcuser
        .dataFetcher("deleteMcuser", env -> {
          final UUID uid = uuidFromToken(env.getArgument("uid"));
          final FetchResult<Boolean> fr = mcuserRepo.deleteMcuser(uid);
          return fr.isSuccess() ? fr.get() : Boolean.FALSE;
        })

        // mcpswd
        .dataFetcher("mcpswd", env -> {
          final UUID uid = uuidFromToken(env.getArgument("uid"));
          final String pswd = env.getArgument("pswd");
          final FetchResult<Boolean> fr = mcuserRepo.setPswd(uid, pswd);
          return fr.isSuccess() ? fr.get() : Boolean.FALSE;
        })
        
        // invalidateJwtsFor
        .dataFetcher("invalidateJwtsFor", env -> {
          final UUID uid = uuidFromToken(env.getArgument("uid"));
          final GraphQLWebContext webContext = env.getContext();
          final RequestSnapshot rs = webContext.getRequestSnapshot();
          final Instant requestInstant = rs.getRequestInstant();
          final String requestOrigin = rs.getClientOrigin();
          final FetchResult<Boolean> fr = mcuserRepo.invalidateJwtsFor(uid, requestInstant, requestOrigin);
          return fr.isSuccess() ? fr.get() : Boolean.FALSE;
        })
        
        // mcorpus

        // member login
        .dataFetcher("mlogin", env -> {
          final GraphQLWebContext webContext = env.getContext();
          final RequestSnapshot rs = webContext.getRequestSnapshot();
          final String username = asStringAndClean(env.getArgument("username"));
          final String pswd = asStringAndClean(env.getArgument("pswd"));
          final Instant requestInstant = rs.getRequestInstant();
          final String requestOrigin = rs.getClientOrigin();
          final FetchResult<Mref> mloginResult = mcorpusRepo.memberLogin(username, pswd, requestInstant, requestOrigin);
          return mloginResult.isSuccess() ? mloginResult.get() : null;
        })
        
        // member logout
        .dataFetcher("mlogout", env -> {
          final GraphQLWebContext webContext = env.getContext();
          final RequestSnapshot rs = webContext.getRequestSnapshot();
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final Instant requestInstant = rs.getRequestInstant();
          final String requestOrigin = rs.getClientOrigin();
          final FetchResult<UUID> mlogoutResult = mcorpusRepo.memberLogout(mid, requestInstant, requestOrigin);
          return mlogoutResult.isSuccess();
        })
        
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

          final FetchResult<Map<String, Object>> fr = mcorpusRepo.addMember(memberMapDomain);
          if(fr.isSuccess()) {
            return fr.get();
          } else {
            final String emsg = fr.getErrorMsg();
            env.getExecutionContext().addError(new ValidationError(ValidationErrorType.InvalidSyntax, (SourceLocation) null, emsg));
            return null;
          }
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

          final FetchResult<Map<String, Object>> fr = mcorpusRepo.updateMember(memberMapDomain);
          if(fr.isSuccess()) {
            return fr.get();
          } else {
            final String emsg = fr.getErrorMsg();
            env.getExecutionContext().addError(new ValidationError(ValidationErrorType.InvalidSyntax, (SourceLocation) null, emsg));
            return null;
          }
        })

        // delete member
        .dataFetcher("deleteMember", env -> {
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final FetchResult<UUID> fr = mcorpusRepo.deleteMember(mid);
          return fr.isSuccess() ? uuidToToken(fr.get()) : null;
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

          final FetchResult<Map<String, Object>> fr = mcorpusRepo.addMemberAddress(maddressMap);
          if(fr.isSuccess()) {
            return fr.get();
          } else {
            final String emsg = fr.getErrorMsg();
            env.getExecutionContext().addError(new ValidationError(ValidationErrorType.InvalidSyntax, (SourceLocation) null, emsg));
            return null;
          }
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

          final FetchResult<Map<String, Object>> fr = mcorpusRepo.updateMemberAddress(maddressMap);
          if(fr.isSuccess()) {
            return fr.get();
          } else {
            final String emsg = fr.getErrorMsg();
            env.getExecutionContext().addError(new ValidationError(ValidationErrorType.InvalidSyntax, (SourceLocation) null, emsg));
            return null;
          }
        })

        // delete member address
        .dataFetcher("deleteMemberAddress", env -> {
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final Addressname addressname = addressNameFromString(env.getArgument("addressName"));
          final FetchResult<UUID> fr = mcorpusRepo.deleteMemberAddress(mid, addressname);
          return fr.isSuccess() ? uuidToToken(fr.get()) : null;
        })
      )

      // mcuser types

      // Mcstatus
      .type("Mcstatus", typeWiring -> typeWiring
        .dataFetcher("uid", env -> {
          final Mcstatus mcstatus = env.getSource();
          return uuidToToken(mcstatus.mcuserId);
        })
        .dataFetcher("since", env -> {
          final Mcstatus mcstatus = env.getSource();
          return mcstatus.since;
        })
        .dataFetcher("expires", env -> {
          final Mcstatus mcstatus = env.getSource();
          return mcstatus.expires;
        })
        .dataFetcher("numActiveJWTs", env -> {
          final Mcstatus mcstatus = env.getSource();
          return mcstatus.numActiveJWTs;
        })
      )

      // Mcuser
      .type("Mcuser", typeWiring -> typeWiring
        .dataFetcher("uid", env -> {
          final Mcuser mc = env.getSource();
          return uuidToToken(mc.getUid());
        })
        .dataFetcher("created", env -> {
          final Mcuser mc = env.getSource();
          return mc.getCreated();
        })
        .dataFetcher("modified", env -> {
          final Mcuser mc = env.getSource();
          return mc.getModified();
        })
        .dataFetcher("name", env -> {
          final Mcuser mc = env.getSource();
          return mc.getName();
        })
        .dataFetcher("email", env -> {
          final Mcuser mc = env.getSource();
          return mc.getEmail();
        })
        .dataFetcher("username", env -> {
          final Mcuser mc = env.getSource();
          return mc.getUsername();
        })
        .dataFetcher("status", env -> {
          final Mcuser mc = env.getSource();
          return mc.getStatus();
        })
        .dataFetcher("roles", env -> {
          final Mcuser mc = env.getSource();
          return isNullOrEmpty(mc.getRoles()) ? 
            Collections.emptyList() :
            Arrays.stream(mc.getRoles()).map(role -> {
              return role.getLiteral();
            }).collect(Collectors.toList());
        })
      )

      // McuserHistory
      .type("McuserHistory", typeWiring -> typeWiring
        .dataFetcher("uid", env -> {
          final McuserHistory mh = env.getSource();
          return mh.uid;
        })
        .dataFetcher("logins", env -> {
          final McuserHistory mh = env.getSource();
          return mh.logins;
        })
        .dataFetcher("logouts", env -> {
          final McuserHistory mh = env.getSource();
          return mh.logouts;
        })
      )

      // McuserLoginEvent
      .type("McuserLoginEvent", typeWiring -> typeWiring
        .dataFetcher("jwtId", env -> {
          final LoginEvent le = env.getSource();
          return le.jwtId;
        })
        .dataFetcher("timestamp", env -> {
          final LoginEvent le = env.getSource();
          return le.timestamp;
        })
      )

      // McuserLogoutEvent
      .type("McuserLogoutEvent", typeWiring -> typeWiring
        .dataFetcher("jwtId", env -> {
          final LogoutEvent le = env.getSource();
          return le.jwtId;
        })
        .dataFetcher("timestamp", env -> {
          final LogoutEvent le = env.getSource();
          return le.timestamp;
        })
      )

      // mcorpus types

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
          final UUID mid = m == null ? null : fval(MEMBER.MID, m);
          return mid == null ? null : uuidToToken(mid);
        })
        .dataFetcher("created", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MEMBER.CREATED, m);
        })
        .dataFetcher("modified", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MEMBER.MODIFIED, m);
        })
        .dataFetcher("empId", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MEMBER.EMP_ID, m);
        })
        .dataFetcher("location", env -> {
          final Map<String, Object> m = env.getSource();
          final Location loc = m == null ? null : fval(MEMBER.LOCATION, m);
          return locationToString(loc);
        })
        .dataFetcher("nameFirst", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MEMBER.NAME_FIRST, m);
        })
        .dataFetcher("nameMiddle", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MEMBER.NAME_MIDDLE, m);
        })
        .dataFetcher("nameLast", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MEMBER.NAME_LAST, m);
        })
        .dataFetcher("displayName", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MEMBER.DISPLAY_NAME, m);
        })
        .dataFetcher("status", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : memberStatusToString(fval(MEMBER.STATUS, m));
        })
        .dataFetcher("dob", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MAUTH.DOB, m);
        })
        .dataFetcher("ssn", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MAUTH.SSN, m);
        })
        .dataFetcher("personalEmail", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MAUTH.EMAIL_PERSONAL, m);
        })
        .dataFetcher("workEmail", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MAUTH.EMAIL_WORK, m);
        })
        .dataFetcher("mobilePhone", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MAUTH.MOBILE_PHONE, m);
        })
        .dataFetcher("homePhone", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MAUTH.HOME_PHONE, m);
        })
        .dataFetcher("workPhone", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MAUTH.WORK_PHONE, m);
        })
        .dataFetcher("username", env -> {
          final Map<String, Object> m = env.getSource();
          return m == null ? null : fval(MAUTH.USERNAME, m);
        })
        .dataFetcher("addresses", env -> {
          final Map<String, Object> m = env.getSource();
          final FetchResult<List<Map<String, Object>>> memberAddressesFetchResult = mcorpusRepo.fetchMemberAddresses(fval(MADDRESS.MID, m));
          return memberAddressesFetchResult.isSuccess() ? memberAddressesFetchResult.get() : null;
        })
      )

      // MemberAddress
      .type("MemberAddress", typeWiring -> typeWiring
        .dataFetcher("mid", env -> {
          final Map<String, Object> ma = env.getSource();
          final UUID mid = ma == null ? null : fval(MADDRESS.MID, ma);
          return mid == null ? null : uuidToToken(mid);
        })
        .dataFetcher("addressName", env -> {
          final Map<String, Object> ma = env.getSource();
          final Addressname aname = ma == null ? null : fval(MADDRESS.ADDRESS_NAME, ma);
          return addressNameToString(aname);
        })
        .dataFetcher("modified", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : fval(MADDRESS.MODIFIED, ma);
        })
        .dataFetcher("attn", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : fval(MADDRESS.ATTN, ma);
        })
        .dataFetcher("street1", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : fval(MADDRESS.STREET1, ma);
        })
        .dataFetcher("street2", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : fval(MADDRESS.STREET2, ma);
        })
        .dataFetcher("city", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : fval(MADDRESS.CITY, ma);
        })
        .dataFetcher("state", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : fval(MADDRESS.STATE, ma);
        })
        .dataFetcher("postalCode", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : fval(MADDRESS.POSTAL_CODE, ma);
        })
        .dataFetcher("country", env -> {
          final Map<String, Object> ma = env.getSource();
          return ma == null ? null : fval(MADDRESS.COUNTRY, ma);
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
