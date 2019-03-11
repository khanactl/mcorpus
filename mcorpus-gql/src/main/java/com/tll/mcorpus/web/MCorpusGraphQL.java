package com.tll.mcorpus.web;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNull;
import static com.tll.mcorpus.transform.BaseMcorpusTransformer.uuidFromToken;
import static com.tll.mcorpus.transform.BaseMcorpusTransformer.uuidToToken;
import static com.tll.mcorpus.transform.MemberXfrm.locationFromString;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tll.gql.GraphQLDate;
import com.tll.gql.GraphQLRequestProcessor;
import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.tables.pojos.Maddress;
import com.tll.mcorpus.dmodel.MemberAndMauth;
import com.tll.mcorpus.gmodel.EmpIdAndLocationKey;
import com.tll.mcorpus.gmodel.Member;
import com.tll.mcorpus.gmodel.MemberAddress;
import com.tll.mcorpus.gmodel.Mref;
import com.tll.mcorpus.gmodel.mcuser.Mcstatus;
import com.tll.mcorpus.gmodel.mcuser.Mcuser;
import com.tll.mcorpus.gmodel.mcuser.McuserHistory;
import com.tll.mcorpus.gmodel.mcuser.McuserHistory.LoginEvent;
import com.tll.mcorpus.gmodel.mcuser.McuserHistory.LogoutEvent;
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
import com.tll.repo.FetchResult;
import com.tll.web.RequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

/**
 * The MCorpus GraphQL schema loader and data fetchers all in one class.
 * 
 * @author jpk
 */
public class MCorpusGraphQL {

  private final Logger log = LoggerFactory.getLogger(MCorpusGraphQL.class);

  private final GraphQLRequestProcessor processor;

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
  public MCorpusGraphQL(final MCorpusUserRepo mcuserRepo, final MCorpusRepo mcorpusRepo) {
    this.mcuserRepo = mcuserRepo;
    this.mcorpusRepo = mcorpusRepo;

    this.processor = new GraphQLRequestProcessor();

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
          final MCorpusGraphQLWebContext webContext = env.getContext();
          return webContext.mcstatus();
        })

        // mcuser history
        .dataFetcher("mchistory", env -> processor.fetch(
            env, 
            env2 -> uuidFromToken(env2.getArgument("uid")), 
            uid -> mcuserRepo.mcuserHistory(uid), 
            b -> xfrmMcuserHistory.fromBackend(b))
        )

        // fetch mcuser
        .dataFetcher("fetchMcuser", env -> processor.fetch(
            env, 
            env2 -> uuidFromToken(env2.getArgument("uid")), 
            uid -> mcuserRepo.fetchMcuser(uid), 
            b -> xfrmMcuser.fromBackend(b))
        )

        // mcorpus

        .dataFetcher("mrefByMid", env -> processor.fetch(
            env, 
            env2 -> uuidFromToken(env2.getArgument("mid")), 
            mid -> mcorpusRepo.fetchMRefByMid(mid), 
            b -> xfrmMref.fromBackend(b))
        )
        .dataFetcher("mrefByEmpIdAndLoc", env -> {
          final String empId = clean(env.getArgument("empId"));
          final Location location = locationFromString(env.getArgument("location"));
          final FetchResult<com.tll.mcorpus.db.udt.pojos.Mref> fr = mcorpusRepo.fetchMRefByEmpIdAndLoc(empId, location);
          return processor.processFetchResult(fr, env, b -> xfrmMref.fromBackend(b));
        })
        .dataFetcher("mrefsByEmpId", env -> {
          final String empId = clean(env.getArgument("empId"));
          final FetchResult<List<com.tll.mcorpus.db.udt.pojos.Mref>> fr = mcorpusRepo.fetchMRefsByEmpId(empId);
          return processor.processFetchResult(fr, env, blist -> 
            blist.stream()
              .map(b -> xfrmMref.fromBackend(b))
              .collect(Collectors.toList())
          );
        })
        .dataFetcher("memberByMid", env -> {
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final FetchResult<MemberAndMauth> fr = mcorpusRepo.fetchMember(mid);
          return processor.processFetchResult(fr, env, b -> xfrmMember.fromBackend(b));
        })
        .dataFetcher("members", env -> processor.fetch(
            env, 
            env2 -> xfrmMemberFilter.fromGraphQLMap(env2.getArgument("filter")), 
            mfilter -> xfrmMemberFilter.toBackend(mfilter), 
            msearch -> mcorpusRepo.memberSearch(msearch), 
            blist -> blist.stream().map(b -> xfrmMember.fromBackend(b)).collect(Collectors.toList()))
        )
      )

      // Mutation
      .type("Mutation", typeWiring -> typeWiring

        // mcuser

        // mcuser login
        .dataFetcher("mclogin", env -> {
          final MCorpusGraphQLWebContext webContext = env.getContext();
          final String username = clean(env.getArgument("username"));
          final String pswd = clean(env.getArgument("pswd"));
          return webContext.mcuserLogin(username, pswd);
        })

        // mcuser logout
        .dataFetcher("mclogout", env -> {
          final MCorpusGraphQLWebContext webContext = env.getContext();
          return webContext.mcuserLogout();
        })

        // add mcuser
        .dataFetcher("addMcuser", env -> processor.handleMutation(
            "mcuser", env,
            map -> xfrmMcuser.fromGraphQLMapForAdd(map),
            g -> vldtnMcuser.validateForAdd(g), 
            (Mcuser gvldtd) -> xfrmMcuser.toBackend(gvldtd),
            b -> mcuserRepo.addMcuser(b),
            bpost -> xfrmMcuser.fromBackend(bpost))
        )

        // update mcuser
        .dataFetcher("updateMcuser", env -> processor.handleMutation(
            "mcuser", env,
            map -> xfrmMcuser.fromGraphQLMapForUpdate(map),
            g -> vldtnMcuser.validateForUpdate(g), 
            (Mcuser gvldtd) -> xfrmMcuser.toBackend(gvldtd),
            b -> mcuserRepo.updateMcuser(b),
            bpost -> xfrmMcuser.fromBackend(bpost))
        )

        // delete mcuser
        .dataFetcher("deleteMcuser", env -> processor.handleDeletion(
            env, 
            uuidFromToken(env.getArgument("uid")), 
            b -> mcuserRepo.deleteMcuser(b))
        )

        // mcpswd
        .dataFetcher("mcpswd", env -> {
          final UUID uid = uuidFromToken(env.getArgument("uid"));
          final String pswd = clean(env.getArgument("pswd"));
          final FetchResult<Boolean> fr = mcuserRepo.setPswd(uid, pswd);
          return processor.processFetchResult(fr, env);
        })
        
        // invalidateJwtsFor
        .dataFetcher("invalidateJwtsFor", env -> {
          final UUID uid = uuidFromToken(env.getArgument("uid"));
          final MCorpusGraphQLWebContext webContext = env.getContext();
          final RequestSnapshot rs = webContext.getRequestSnapshot();
          final Instant requestInstant = rs.getRequestInstant();
          final String requestOrigin = rs.getClientOrigin();
          final FetchResult<Boolean> fr = mcuserRepo.invalidateJwtsFor(uid, requestInstant, requestOrigin);
          return processor.processFetchResult(fr, env);
        })
        
        // mcorpus

        // member login
        .dataFetcher("mlogin", env -> {
          final MCorpusGraphQLWebContext webContext = env.getContext();
          final RequestSnapshot rs = webContext.getRequestSnapshot();
          final String username = clean(env.getArgument("username"));
          final String pswd = clean(env.getArgument("pswd"));
          final Instant requestInstant = rs.getRequestInstant();
          final String requestOrigin = rs.getClientOrigin();
          final FetchResult<com.tll.mcorpus.db.udt.pojos.Mref> fr = 
                  mcorpusRepo.memberLogin(username, pswd, requestInstant, requestOrigin);
          return processor.processFetchResult(fr, env, b -> xfrmMref.fromBackend(b));
        })
        
        // member logout
        .dataFetcher("mlogout", env -> {
          final MCorpusGraphQLWebContext webContext = env.getContext();
          final RequestSnapshot rs = webContext.getRequestSnapshot();
          final UUID mid = uuidFromToken(env.getArgument("mid"));
          final Instant requestInstant = rs.getRequestInstant();
          final String requestOrigin = rs.getClientOrigin();
          final FetchResult<UUID> fr = mcorpusRepo.memberLogout(mid, requestInstant, requestOrigin);
          return isNotNull(processor.processFetchResult(fr, env));
        })
        
        // add member
        .dataFetcher("addMember", env -> processor.handleMutation(
            "member", env, 
            map -> xfrmMember.fromGraphQLMapForAdd(map), 
            (Member g) -> vldtnMember.validateForAdd(g), 
            gvldtd -> xfrmMember.toBackend(gvldtd), 
            (MemberAndMauth b) -> mcorpusRepo.addMember(b), 
            bpost -> xfrmMember.fromBackend(bpost))
        )

        // update member
        .dataFetcher("updateMember", env -> processor.handleMutation(
            "member", env, 
            map -> xfrmMember.fromGraphQLMapForUpdate(map), 
            (Member g) -> vldtnMember.validateForUpdate(g), 
            gvldtd -> xfrmMember.toBackend(gvldtd), 
            (MemberAndMauth b) -> mcorpusRepo.updateMember(b), 
            bpost -> xfrmMember.fromBackend(bpost))
        )

        // delete member
        .dataFetcher("deleteMember", env -> processor.handleDeletion(
            env, 
            uuidFromToken(env.getArgument("mid")), 
            b -> mcorpusRepo.deleteMember(b))
        )

        // add member address
        .dataFetcher("addMemberAddress", env -> processor.handleMutation(
            "memberAddress", env, 
            map -> xfrmMemberAddress.fromGraphQLMapForAdd(map), 
            (MemberAddress g) -> vldtnMemberAddress.validateForAdd(g), 
            gvldtd -> xfrmMemberAddress.toBackend(gvldtd), 
            (Maddress b) -> mcorpusRepo.addMemberAddress(b), 
            bpost -> xfrmMemberAddress.fromBackend(bpost))
        )

        // update member address
        .dataFetcher("updateMemberAddress", env -> processor.handleMutation(
            "memberAddress", env, 
            map -> xfrmMemberAddress.fromGraphQLMapForUpdate(map), 
            (MemberAddress g) -> vldtnMemberAddress.validateForUpdate(g), 
            gvldtd -> xfrmMemberAddress.toBackend(gvldtd), 
            (Maddress b) -> mcorpusRepo.updateMemberAddress(b), 
            bpost -> xfrmMemberAddress.fromBackend(bpost))
        )

        // delete member address
        .dataFetcher("deleteMemberAddress", env -> processor.handleDeletion(env, () -> {
            final UUID mid = uuidFromToken(env.getArgument("mid"));
            final Addressname addressname = MemberAddressXfrm.addressnameFromString(env.getArgument("addressName"));
            final FetchResult<Boolean> fr = mcorpusRepo.deleteMemberAddress(mid, addressname);
            return fr;
          })
        )
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
          final Mref mref = env.getSource();
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
          return processor.processFetchResult(fr, env, 
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

}
