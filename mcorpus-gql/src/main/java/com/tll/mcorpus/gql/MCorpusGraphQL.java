package com.tll.mcorpus.gql;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.dflt;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNull;
import static com.tll.mcorpus.transform.BaseMcorpusTransformer.uuidFromToken;
import static com.tll.mcorpus.transform.BaseMcorpusTransformer.uuidToToken;
import static com.tll.mcorpus.transform.MemberXfrm.locationFromString;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.tables.pojos.Maddress;
import com.tll.mcorpus.dmodel.McuserHistoryDomain;
import com.tll.mcorpus.dmodel.MemberAndMauth;
import com.tll.mcorpus.dmodel.MemberSearch;
import com.tll.mcorpus.gmodel.Member;
import com.tll.mcorpus.gmodel.MemberAddress;
import com.tll.mcorpus.gmodel.MemberFilter;
import com.tll.mcorpus.gmodel.Mref;
import com.tll.mcorpus.gmodel.mcuser.Mcstatus;
import com.tll.mcorpus.gmodel.mcuser.Mcuser;
import com.tll.mcorpus.gmodel.mcuser.McuserHistory;
import com.tll.mcorpus.gmodel.mcuser.McuserHistory.LoginEvent;
import com.tll.mcorpus.gmodel.mcuser.McuserHistory.LogoutEvent;
import com.tll.repo.FetchResult;
import com.tll.mcorpus.repo.MCorpusRepo;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.transform.McuserHistoryXfrm;
import com.tll.mcorpus.transform.McuserXfrm;
import com.tll.mcorpus.transform.MemberAddressXfrm;
import com.tll.mcorpus.transform.MemberFilterXfrm;
import com.tll.mcorpus.transform.MemberXfrm;
import com.tll.mcorpus.transform.MrefXfrm;
import com.tll.mcorpus.validate.McuserValidator;
import com.tll.mcorpus.validate.MemberAddressValidator;
import com.tll.mcorpus.validate.MemberValidator;
import com.tll.validate.VldtnResult;
import com.tll.mcorpus.web.GraphQLWebContext;
import com.tll.web.RequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;

public class MCorpusGraphQL {

  private final Logger log = LoggerFactory.getLogger(MCorpusGraphQL.class);

  // validators
  private final McuserValidator vldtnMcuser;

  private final MemberValidator vldtnMember;
  private final MemberAddressValidator vldtnMemberAddress;

  // transformers
  private final McuserXfrm xfrmMcuser;
  private final McuserHistoryXfrm xfrmMcuserHistory;

  private final MrefXfrm xfrmMref;
  private final MemberXfrm xfrmMember;
  private final MemberAddressXfrm xfrmMemberAddress;
  private final MemberFilterXfrm xfrmMemberFilter;
  
  // backend repos
  private final MCorpusUserRepo mcuserRepo;
  private final MCorpusRepo mcorpusRepo;

  // GraphQL schema
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

    this.vldtnMcuser = new McuserValidator();
    this.xfrmMcuserHistory = new McuserHistoryXfrm();
    
    this.vldtnMember = new MemberValidator();
    this.vldtnMemberAddress = new MemberAddressValidator();
    
    this.xfrmMcuser = new McuserXfrm();
    
    this.xfrmMref = new MrefXfrm();
    this.xfrmMember = new MemberXfrm();
    this.xfrmMemberAddress = new MemberAddressXfrm();
    this.xfrmMemberFilter = new MemberFilterXfrm();
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
          final FetchResult<McuserHistoryDomain> fr = mcuserRepo.mcuserHistory(uid);
          return processFetchResult(fr, env, b -> xfrmMcuserHistory.fromBackend(b));
        })

        // fetch mcuser
        .dataFetcher("fetchMcuser", env -> {
          final UUID uid = uuidFromToken(env.getArgument("uid"));
          final FetchResult<com.tll.mcorpus.db.tables.pojos.Mcuser> fr = mcuserRepo.fetchMcuser(uid);
          return processFetchResult(fr, env, b -> xfrmMcuser.fromBackend(b));
        })

        // mcorpus

        .dataFetcher("mrefByMid", env -> {
          final UUID uuid = uuidFromToken(env.getArgument("mid"));
          final FetchResult<com.tll.mcorpus.db.udt.pojos.Mref> fr = mcorpusRepo.fetchMRefByMid(uuid);
          return processFetchResult(fr, env, b -> xfrmMref.fromBackend(b));
        })
        .dataFetcher("mrefByEmpIdAndLoc", env -> {
          final String empId = clean(env.getArgument("empId"));
          final Location location = locationFromString(env.getArgument("location"));
          final FetchResult<com.tll.mcorpus.db.udt.pojos.Mref> fr = mcorpusRepo.fetchMRefByEmpIdAndLoc(empId, location);
          return processFetchResult(fr, env, b -> xfrmMref.fromBackend(b));
        })
        .dataFetcher("mrefsByEmpId", env -> {
          final String empId = clean(env.getArgument("empId"));
          final FetchResult<List<com.tll.mcorpus.db.udt.pojos.Mref>> fr = mcorpusRepo.fetchMRefsByEmpId(empId);
          return processFetchResult(fr, env, blist -> 
            blist.stream()
              .map(b -> xfrmMref.fromBackend(b))
              .collect(Collectors.toList())
          );
        })
        .dataFetcher("memberByMid", env -> {
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final FetchResult<MemberAndMauth> fr = mcorpusRepo.fetchMember(mid);
          return processFetchResult(fr, env, b -> xfrmMember.fromBackend(b));
        })
        .dataFetcher("members", env -> {
          final MemberFilter filter = xfrmMemberFilter.fromGraphQLMap(env.getArgument("filter"));
          final int offset = dflt(env.getArgument("offset"), 0);
          final int limit = dflt(env.getArgument("limit"), 10);
          final MemberSearch msearch = xfrmMemberFilter.toBackend(filter);
          final FetchResult<List<MemberAndMauth>> fr = mcorpusRepo.memberSearch(msearch, offset, limit);
          return processFetchResult(fr, env, blist -> 
            blist.stream()
              .map(b -> xfrmMember.fromBackend(b))
              .collect(Collectors.toList())
          );
        })
      )

      // Mutation
      .type("Mutation", typeWiring -> typeWiring

        // mcuser

        // mcuser login
        .dataFetcher("mclogin", env -> {
          final GraphQLWebContext webContext = env.getContext();
          final String username = clean(env.getArgument("username"));
          final String pswd = clean(env.getArgument("pswd"));
          return webContext.mcuserLogin(username, pswd);
        })

        // mcuser logout
        .dataFetcher("mclogout", env -> {
          final GraphQLWebContext webContext = env.getContext();
          return webContext.mcuserLogout();
        })

        // add mcuser
        .dataFetcher("addMcuser", env -> {
          return handleMutation(
            "mcuser", env,
            map -> xfrmMcuser.fromGraphQLMapForAdd(map),
            g -> vldtnMcuser.validateForAdd(g), 
            (Mcuser gvldtd) -> xfrmMcuser.toBackend(gvldtd),
            b -> mcuserRepo.addMcuser(b),
            bpost -> xfrmMcuser.fromBackend(bpost)
          );
        })

        // update mcuser
        .dataFetcher("updateMcuser", env -> {
          return handleMutation(
            "mcuser", env,
            map -> xfrmMcuser.fromGraphQLMapForUpdate(map),
            g -> vldtnMcuser.validateForUpdate(g), 
            (Mcuser gvldtd) -> xfrmMcuser.toBackend(gvldtd),
            b -> mcuserRepo.updateMcuser(b),
            bpost -> xfrmMcuser.fromBackend(bpost)
          );
        })

        // delete mcuser
        .dataFetcher("deleteMcuser", env -> {
          return handleDeletion(
            env, 
            uuidFromToken(env.getArgument("uid")), 
            b -> mcuserRepo.deleteMcuser(b) 
          );
        })

        // mcpswd
        .dataFetcher("mcpswd", env -> {
          final UUID uid = uuidFromToken(env.getArgument("uid"));
          final String pswd = clean(env.getArgument("pswd"));
          final FetchResult<Boolean> fr = mcuserRepo.setPswd(uid, pswd);
          return processFetchResult(fr, env);
        })
        
        // invalidateJwtsFor
        .dataFetcher("invalidateJwtsFor", env -> {
          final UUID uid = uuidFromToken(env.getArgument("uid"));
          final GraphQLWebContext webContext = env.getContext();
          final RequestSnapshot rs = webContext.getRequestSnapshot();
          final Instant requestInstant = rs.getRequestInstant();
          final String requestOrigin = rs.getClientOrigin();
          final FetchResult<Boolean> fr = mcuserRepo.invalidateJwtsFor(uid, requestInstant, requestOrigin);
          return processFetchResult(fr, env);
        })
        
        // mcorpus

        // member login
        .dataFetcher("mlogin", env -> {
          final GraphQLWebContext webContext = env.getContext();
          final RequestSnapshot rs = webContext.getRequestSnapshot();
          final String username = clean(env.getArgument("username"));
          final String pswd = clean(env.getArgument("pswd"));
          final Instant requestInstant = rs.getRequestInstant();
          final String requestOrigin = rs.getClientOrigin();
          final FetchResult<com.tll.mcorpus.db.udt.pojos.Mref> fr = 
                  mcorpusRepo.memberLogin(username, pswd, requestInstant, requestOrigin);
          return processFetchResult(fr, env, b -> xfrmMref.fromBackend(b));
        })
        
        // member logout
        .dataFetcher("mlogout", env -> {
          final GraphQLWebContext webContext = env.getContext();
          final RequestSnapshot rs = webContext.getRequestSnapshot();
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final Instant requestInstant = rs.getRequestInstant();
          final String requestOrigin = rs.getClientOrigin();
          final FetchResult<UUID> fr = mcorpusRepo.memberLogout(mid, requestInstant, requestOrigin);
          return isNotNull(processFetchResult(fr, env));
        })
        
        // add member
        .dataFetcher("addMember", env -> {
          return handleMutation(
            "member", env, 
            map -> xfrmMember.fromGraphQLMapForAdd(map), 
            (Member g) -> vldtnMember.validateForAdd(g), 
            gvldtd -> xfrmMember.toBackend(gvldtd), 
            (MemberAndMauth b) -> mcorpusRepo.addMember(b), 
            bpost -> xfrmMember.fromBackend(bpost)
          );
        })

        // update member
        .dataFetcher("updateMember", env -> {
          return handleMutation(
            "member", env, 
            map -> xfrmMember.fromGraphQLMapForUpdate(map), 
            (Member g) -> vldtnMember.validateForUpdate(g), 
            gvldtd -> xfrmMember.toBackend(gvldtd), 
            (MemberAndMauth b) -> mcorpusRepo.updateMember(b), 
            bpost -> xfrmMember.fromBackend(bpost)
          );
        })

        // delete member
        .dataFetcher("deleteMember", env -> {
          return handleDeletion(
            env, 
            uuidFromToken(env.getArgument("mid")), 
            b -> mcorpusRepo.deleteMember(b) 
          );
        })

        // add member address
        .dataFetcher("addMemberAddress", env -> {
          return handleMutation(
            "memberAddress", env, 
            map -> xfrmMemberAddress.fromGraphQLMapForAdd(map), 
            (MemberAddress g) -> vldtnMemberAddress.validateForAdd(g), 
            gvldtd -> xfrmMemberAddress.toBackend(gvldtd), 
            (Maddress b) -> mcorpusRepo.addMemberAddress(b), 
            bpost -> xfrmMemberAddress.fromBackend(bpost)
          );
        })

        // update member address
        .dataFetcher("updateMemberAddress", env -> {
          return handleMutation(
            "memberAddress", env, 
            map -> xfrmMemberAddress.fromGraphQLMapForUpdate(map), 
            (MemberAddress g) -> vldtnMemberAddress.validateForUpdate(g), 
            gvldtd -> xfrmMemberAddress.toBackend(gvldtd), 
            (Maddress b) -> mcorpusRepo.updateMemberAddress(b), 
            bpost -> xfrmMemberAddress.fromBackend(bpost)
          );
        })

        // delete member address
        .dataFetcher("deleteMemberAddress", env -> {
          return handleDeletion(env, () -> {
            final UUID mid = uuidFromToken(env.getArgument("mid"));
            final Addressname addressname = MemberAddressXfrm.addressnameFromString(env.getArgument("addressName"));
            final FetchResult<Boolean> fr = mcorpusRepo.deleteMemberAddress(mid, addressname);
            return fr;
          });
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
          return mc.getRoles().stream().collect(Collectors.toList());
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
          final com.tll.mcorpus.gmodel.Mref mref = env.getSource();
          return mref == null ? null : uuidToToken(mref.mid);
        })
        .dataFetcher("empId", env -> {
          final Mref mref = env.getSource();
          return mref == null ? null : mref.empId;
        })
        .dataFetcher("location", env -> {
          final Mref mref = env.getSource();
          return mref == null ? null : mref.location;
        })
      )

      // Member
      .type("Member", typeWiring -> typeWiring
        .dataFetcher("mid", env -> {
          final Member m = env.getSource();
          final UUID mid = m == null ? null : m.getMid();
          return mid == null ? null : uuidToToken(mid);
        })
        .dataFetcher("created", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getCreated();
        })
        .dataFetcher("modified", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getModified();
        })
        .dataFetcher("empId", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getEmpId();
        })
        .dataFetcher("location", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getLocation();
        })
        .dataFetcher("nameFirst", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getNameFirst();
        })
        .dataFetcher("nameMiddle", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getNameMiddle();
        })
        .dataFetcher("nameLast", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getNameLast();
        })
        .dataFetcher("displayName", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getDisplayName();
        })
        .dataFetcher("status", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getStatus();
        })
        .dataFetcher("dob", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getDob();
        })
        .dataFetcher("ssn", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getSsn();
        })
        .dataFetcher("personalEmail", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getPersonalEmail();
        })
        .dataFetcher("workEmail", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getWorkEmail();
        })
        .dataFetcher("mobilePhone", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getMobilePhone();
        })
        .dataFetcher("homePhone", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getHomePhone();
        })
        .dataFetcher("workPhone", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getWorkPhone();
        })
        .dataFetcher("username", env -> {
          final Member m = env.getSource();
          return m == null ? null : m.getUsername();
        })
        .dataFetcher("addresses", env -> {
          final Member m = env.getSource();
          final UUID mid = m.getMid();
          final FetchResult<List<Maddress>> fr = mcorpusRepo.fetchMemberAddresses(mid);
          return processFetchResult(fr, env, 
            blist -> blist.stream()
              .map(b -> xfrmMemberAddress.fromBackend(b)).collect(Collectors.toList())
          );
        })
      )

      // MemberAddress
      .type("MemberAddress", typeWiring -> typeWiring
        .dataFetcher("mid", env -> {
          final MemberAddress ma = env.getSource();
          return ma == null ? null : uuidToToken(ma.getMid());
        })
        .dataFetcher("addressName", env -> {
          final MemberAddress ma = env.getSource();
          return ma == null ? null : ma.getAddressName();
        })
        .dataFetcher("modified", env -> {
          final MemberAddress ma = env.getSource();
          return ma == null ? null : ma.getModified();
        })
        .dataFetcher("attn", env -> {
          final MemberAddress ma = env.getSource();
          return ma == null ? null : ma.getAttn();
        })
        .dataFetcher("street1", env -> {
          final MemberAddress ma = env.getSource();
          return ma == null ? null : ma.getStreet1();
        })
        .dataFetcher("street2", env -> {
          final MemberAddress ma = env.getSource();
          return ma == null ? null : ma.getStreet2();
        })
        .dataFetcher("city", env -> {
          final MemberAddress ma = env.getSource();
          return ma == null ? null : ma.getCity();
        })
        .dataFetcher("state", env -> {
          final MemberAddress ma = env.getSource();
          return ma == null ? null : ma.getState();
        })
        .dataFetcher("postalCode", env -> {
          final MemberAddress ma = env.getSource();
          return ma == null ? null : ma.getPostalCode();
        })
        .dataFetcher("country", env -> {
          final MemberAddress ma = env.getSource();
          return ma == null ? null : ma.getCountry();
        })
      )

      .build();
  }

  /**
   * Process a GraphQL mutation request.
   * <p>
   * Steps:
   * <ul>
   * <li>Transform GraphQL field map to fronend entity type
   * <li>Validate
   * <li>Transform to backend domain type
   * <li>Do the persist operation
   * <li>Transform to returned persisted entity to a frontend entity type
   * </ul>
   * 
   * @param <G> the GraphQl schema type
   * @param <B> the backend domain type
   * 
   * @param arg0 the [first] GraphQL mutation method input argument name 
   *             as defined in the loaded GraphQL schema
   * @param env the GraphQL data fetching env object
   * @param tfrmFromGqlMap constitutes a frontend GraphQL type from the 
   *                       GraphQL data fetching environment (param <code>env</code>).
   * @param vldtn the optional validation function to use to validate the frontend object <code>e</code>
   * @param tfrmToBack the frontend to backend transform function
   * @param persistOp the backend repository persist operation function
   * @param tfrmToFront the backend to frontend transform function
   * @return the returned entity from the backend persist operation
   *         which should be considered to be the post-mutation 
   *         current state of the entity.
   */
  private <G, B> G handleMutation(
    final String arg0, 
    final DataFetchingEnvironment env, 
    final Function<Map<String, Object>, G> tfrmFromGqlMap, 
    final Function<G, VldtnResult> vldtn, 
    final Function<G, B> tfrmToBack, 
    final Function<B, FetchResult<B>> persistOp, 
    final Function<B, G> trfmToFront
  ) {
    try {
      final G g = tfrmFromGqlMap.apply(env.getArgument(arg0));
      final VldtnResult vresult = isNull(vldtn) ? VldtnResult.VALID : vldtn.apply(g);
      if(vresult.isValid()) {
        final B b = tfrmToBack.apply(g);
        final FetchResult<B> fr = persistOp.apply(b);
        if(fr.isSuccess()) {
          final G gpost = trfmToFront.apply(fr.get());
          return gpost;
        } 
        if(fr.hasErrorMsg()) {
          final String emsg = fr.getErrorMsg();
          env.getExecutionContext().addError(
            new ValidationError(
                ValidationErrorType.InvalidSyntax, 
                (SourceLocation) null, 
                emsg));
        }
      } else {
        vresult.getErrors().stream().forEach(cv -> {
          env.getExecutionContext().addError(
            new ValidationError(
              ValidationErrorType.InvalidSyntax, 
              (SourceLocation) null, 
              cv.getVldtnErrMsg()));
        });
      }
    } catch(Exception e) {
      // mutation processing error
      log.error("Mutation processing error: {}", e.getMessage());
    }
    // default
    return null;
  }

  /**
   * Generalized backend entity deletion routine.
   * <p>
   * Use when the backend primary key is not a single UUID.
   * 
   * @param env the GraphQL data fetching environment ref
   * @param deleteOp the {@link FetchResult} provider that performs the backend deletion
   * @return true when the delete op was run without error, false otherwise.
   */
  private <G> boolean handleDeletion(
    final DataFetchingEnvironment env, 
    final Supplier<FetchResult<Boolean>> deleteOp 
  ) {
    try {
      final FetchResult<Boolean> fr = deleteOp.get();
      if(fr.isSuccess()) {
        return true;
      } 
      if(fr.hasErrorMsg()) {
        final String emsg = fr.getErrorMsg();
        env.getExecutionContext().addError(
          new ValidationError(
              ValidationErrorType.InvalidSyntax, 
              (SourceLocation) null, 
              emsg));
      }
    } catch(Exception e) {
      log.error("Deletion by op processing error: {}", e.getMessage());
    }
    // default
    return false;
  }

  /**
   * Do a backend entity deletion for the case of a single UUID (primary key) input argument.
   * 
   * @param env the GraphQL data fetching environment ref
   * @param pk the entity primary key value
   * @param deleteOp the {@link FetchResult} provider that performs the backend deletion
   * @return true when the delete op was run without error, false otherwise.
   */
  private <G, B> boolean handleDeletion(
    final DataFetchingEnvironment env, 
    final UUID pk,
    final Function<UUID, FetchResult<Boolean>> deleteOp 
  ) {
    try {
      final FetchResult<Boolean> fr = deleteOp.apply(pk);
      if(fr.isSuccess()) {
        return true;
      } 
      if(fr.hasErrorMsg()) {
        final String emsg = fr.getErrorMsg();
        env.getExecutionContext().addError(
          new ValidationError(
              ValidationErrorType.InvalidSyntax, 
              (SourceLocation) null, 
              emsg));
      }
    } catch(Exception e) {
      log.error("Deletion by UUID processing error: {}", e.getMessage());
    }
    // delete error
    return false;
  }

  /**
   * Processes a {@link FetchResult} that wraps a simple (non-entity) type.
   * <p>
   * Any backend errors will be translated then added to the GraphQL 
   * fetching environment and <code>null</code> is returned.
   * 
   * @param <T> the simple type in the given fetch result (usually {@link Boolean})
   * @param fr the fetch result
   * @param env the GraphQL data fetching environment ref
   * @return the extracted fetch result value
   */
  private <T> T processFetchResult(
    final FetchResult<T> fr, 
    final DataFetchingEnvironment env
  ) {
    try {
      if(fr.isSuccess()) {
        return fr.get();
      } 
      if(fr.hasErrorMsg()) {
        final String emsg = fr.getErrorMsg();
        env.getExecutionContext().addError(
          new ValidationError(
              ValidationErrorType.InvalidSyntax, 
              (SourceLocation) null, 
              emsg));
      }
    } catch(Exception e) {
      log.error("Fetch result processing error: {}", e.getMessage());
    }
    // default
    return null;
  }

  /**
   * Processes a {@link FetchResult} 
   * transforming the backend result type to the frontend GraphQL type.
   * <p>
   * Any backend errors will be translated then added to the GraphQL 
   * fetching environment and <code>null</code> is returned.
   * 
   * @param <T> the held object type in the given fetch result 
   * @param <G> the frontend GraphQL type
   * @param fr the fetch result
   * @param env the GraphQL data fetching environment ref
   * @return the transformed fetch result value when no errors are present
   *         or null when errors are present
   */
  private <T, G> G processFetchResult(
    final FetchResult<T> fr, 
    final DataFetchingEnvironment env, 
    final Function<T, G> transform
  ) {
    try {
      if(fr.isSuccess()) {
        final G g = transform.apply(fr.get());
        return g;
      } 
      if(fr.hasErrorMsg()) {
        final String emsg = fr.getErrorMsg();
        env.getExecutionContext().addError(
          new ValidationError(
              ValidationErrorType.InvalidSyntax, 
              (SourceLocation) null, 
              emsg));
      }
    } catch(Exception e) {
      log.error("Fetch result (transforming) processing error: {}", e.getMessage());
    }
    // default
    return null;
  }

}
