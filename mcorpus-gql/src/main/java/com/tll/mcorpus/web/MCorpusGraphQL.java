package com.tll.mcorpus.web;

import static com.tll.core.Util.clean;
import static com.tll.transform.TransformUtil.uuidFromToken;
import static com.tll.transform.TransformUtil.uuidToToken;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.tll.gql.GraphQLDate;
import com.tll.gql.GraphQLRequestProcessor;
import com.tll.jwt.IJwtUserStatus;
import com.tll.mcorpus.db.tables.pojos.Maddress;
import com.tll.mcorpus.dmodel.MemberAndMauth;
import com.tll.mcorpus.gmodel.EmpIdAndLocationKey;
import com.tll.mcorpus.gmodel.Member;
import com.tll.mcorpus.gmodel.MemberAddress;
import com.tll.mcorpus.gmodel.MemberAndAddresses;
import com.tll.mcorpus.gmodel.MemberAddress.MidAndAddressNameKey;
import com.tll.mcorpus.gmodel.MemberIdAndPswdKey;
import com.tll.mcorpus.gmodel.Mlogin;
import com.tll.mcorpus.gmodel.Mlogout;
import com.tll.mcorpus.gmodel.Mref;
import com.tll.mcorpus.gmodel.mcuser.Mcuser;
import com.tll.mcorpus.gmodel.mcuser.McuserHistory;
import com.tll.mcorpus.gmodel.mcuser.McuserHistory.LoginEvent;
import com.tll.mcorpus.gmodel.mcuser.McuserHistory.LogoutEvent;
import com.tll.mcorpus.gmodel.mcuser.McuserIdAndPswdKey;
import com.tll.mcorpus.gmodel.mcuser.McusernameAndPswdKey;
import com.tll.mcorpus.repo.MCorpusRepo;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.transform.EmpIdAndLocationXfrm;
import com.tll.mcorpus.transform.McuserHistoryXfrm;
import com.tll.mcorpus.transform.McuserXfrm;
import com.tll.mcorpus.transform.MemberAddressXfrm;
import com.tll.mcorpus.transform.MemberAndAddressesXfrm;
import com.tll.mcorpus.transform.MemberFilterXfrm;
import com.tll.mcorpus.transform.MemberXfrm;
import com.tll.mcorpus.transform.MidAndAddressNameXfrm;
import com.tll.mcorpus.transform.MrefXfrm;
import com.tll.mcorpus.validate.EmpIdAndLocationValidator;
import com.tll.mcorpus.validate.McuserValidator;
import com.tll.mcorpus.validate.MemberAddressValidator;
import com.tll.mcorpus.validate.MemberValidator;
import com.tll.web.JWTUserGraphQLWebContext;

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

  private final EmpIdAndLocationValidator vldtnEmpIdAndLocation;
  private final MemberValidator vldtnMember;
  private final MemberAddressValidator vldtnMemberAddress;

  // transformers
  private final McuserXfrm xfrmMcuser;
  private final McuserHistoryXfrm xfrmMcuserHistory;

  private final EmpIdAndLocationXfrm xfrmEmpIdAndLocation;
  private final MrefXfrm xfrmMref;
  private final MemberXfrm xfrmMember;
  private final MemberAndAddressesXfrm xfrmMemberAndAddress;
  private final MidAndAddressNameXfrm xfrmMidAndAddressName;
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

    this.vldtnEmpIdAndLocation = new EmpIdAndLocationValidator();
    this.vldtnMember = new MemberValidator();
    this.vldtnMemberAddress = new MemberAddressValidator();

    this.xfrmMcuser = new McuserXfrm();

    this.xfrmEmpIdAndLocation = new EmpIdAndLocationXfrm();
    this.xfrmMref = new MrefXfrm();
    this.xfrmMember = new MemberXfrm();
    this.xfrmMemberAndAddress = new MemberAndAddressesXfrm();
    this.xfrmMidAndAddressName = new MidAndAddressNameXfrm();
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
        .dataFetcher("mcstatus", env -> processor.process(
          env,
          () -> ((JWTUserGraphQLWebContext) env.getContext()).jwtUserStatus())
        )

        // mcuser history
        .dataFetcher("mchistory", env -> processor.fetch(
          env,
          () -> uuidFromToken(env.getArgument("uid")),
          uid -> mcuserRepo.mcuserHistory(uid),
          b -> xfrmMcuserHistory.fromBackend(b))
        )

        // fetch mcuser
        .dataFetcher("fetchMcuser", env -> processor.fetch(
          env,
          () -> uuidFromToken(env.getArgument("uid")),
          uid -> mcuserRepo.fetchMcuser(uid),
          b -> xfrmMcuser.fromBackend(b))
        )

        // mcorpus

        .dataFetcher("mrefByMid", env -> processor.fetch(
          env,
          () -> uuidFromToken(env.getArgument("mid")),
          mid -> mcorpusRepo.fetchMRefByMid(mid),
          b -> xfrmMref.fromBackend(b))
        )
        .dataFetcher("mrefByEmpIdAndLoc", env -> processor.fetch(
          env,
          () -> new EmpIdAndLocationKey(
            clean(env.getArgument("empId")),
            clean(env.getArgument("location"))
          ),
          key -> vldtnEmpIdAndLocation.validate(key),
          key2 -> xfrmEmpIdAndLocation.toBackend(key2),
          key3 -> mcorpusRepo.fetchMRefByEmpIdAndLoc(key3.empId(), key3.location()),
          b -> xfrmMref.fromBackend(b))
        )
        .dataFetcher("mrefsByEmpId", env -> processor.fetch(
          env,
          () -> clean(env.getArgument("empId")),
          empId -> mcorpusRepo.fetchMRefsByEmpId(empId),
          blist -> blist.stream()
                      .map(b -> xfrmMref.fromBackend(b))
                      .collect(Collectors.toList()))
        )
        .dataFetcher("memberByMid", env -> {
          // deal with N+1 problem by determining if we are fetching related addresses or not
          if(env.getSelectionSet().contains("addresses")) {
            // member and address fields case
            return processor.fetch(
              env,
              () -> uuidFromToken(env.getArgument("mid")),
              mid -> mcorpusRepo.fetchMemberAndAddresses(mid),
              b -> xfrmMemberAndAddress.fromBackend(b));
          } else {
            // member only fields case
            return processor.fetch(
              env,
              () -> uuidFromToken(env.getArgument("mid")),
              mid -> mcorpusRepo.fetchMember(mid),
              b -> xfrmMember.fromBackend(b));
          }
        })
        .dataFetcher("members", env -> processor.fetch(
          env,
          () -> xfrmMemberFilter.fromGraphQLMap(env.getArgument("filter")),
          null,
          mfilter -> xfrmMemberFilter.toBackend(mfilter),
          msearch -> mcorpusRepo.memberSearch(msearch),
          blist -> blist.stream().map(b -> xfrmMember.fromBackend(b)).collect(Collectors.toList()))
        )
      )

      // Mutation
      .type("Mutation", typeWiring -> typeWiring

        // mcuser

        // mcuser login
        .dataFetcher("mclogin", env -> processor.handleSimpleMutation(
          env,
          () -> new McusernameAndPswdKey(
            clean(env.getArgument("username")),
            clean(env.getArgument("pswd"))
          ),
          key -> ((JWTUserGraphQLWebContext) env.getContext()).jwtUserLogin(key.getUsername(), key.getPswd()),
          b -> b)
        )

        // mcuser logout
        .dataFetcher("mclogout", env -> processor.process(
          env,
          () -> ((JWTUserGraphQLWebContext) env.getContext()).jwtUserLogout())
        )

        // add mcuser
        .dataFetcher("addMcuser", env -> processor.handleMutation(
          env,
          () -> xfrmMcuser.fromGraphQLMapForAdd(env.getArgument("mcuser")),
          g -> vldtnMcuser.validateForAdd(g),
          (Mcuser gvldtd) -> xfrmMcuser.toBackend(gvldtd),
          b -> mcuserRepo.addMcuser(b),
          bpost -> xfrmMcuser.fromBackend(bpost))
        )

        // update mcuser
        .dataFetcher("updateMcuser", env -> processor.handleMutation(
          env,
          () -> xfrmMcuser.fromGraphQLMapForUpdate(env.getArgument("mcuser")),
          g -> vldtnMcuser.validateForUpdate(g),
          (Mcuser gvldtd) -> xfrmMcuser.toBackend(gvldtd),
          b -> mcuserRepo.updateMcuser(b),
          bpost -> xfrmMcuser.fromBackend(bpost))
        )

        // delete mcuser
        .dataFetcher("deleteMcuser", env -> processor.handleDeletion(
          env,
          () -> uuidFromToken(env.getArgument("uid")),
          key -> key,
          b -> mcuserRepo.deleteMcuser(b))
        )

        // mcpswd
        .dataFetcher("mcpswd", env -> processor.handleSimpleMutation(
          env,
          () -> new McuserIdAndPswdKey(
            uuidFromToken(env.getArgument("uid")),
            clean(env.getArgument("pswd"))
          ),
          key -> mcuserRepo.setPswd(key.getUid(), key.getPswd()),
          fr -> fr.get())
        )

        // invalidateJwtsFor
        .dataFetcher("invalidateJwtsFor", env -> processor.handleSimpleMutation(
          env,
          () -> uuidFromToken(env.getArgument("uid")),
          key -> ((JWTUserGraphQLWebContext) env.getContext()).jwtInvalidateAllForUser(key),
          b -> b)
        )

        // mcorpus

        // member login
        .dataFetcher("mlogin", env -> processor.handleMutation(
          env,
          () -> new Mlogin(
            clean(env.getArgument("username")),
            clean(env.getArgument("pswd")),
            ((JWTUserGraphQLWebContext) env.getContext()).getJwtRequestProvider().getRequestInstant(),
            ((JWTUserGraphQLWebContext) env.getContext()).getJwtRequestProvider().getClientOrigin()
          ),
          mclogin -> mcorpusRepo.memberLogin(
            mclogin.getUsername(),
            mclogin.getPswd(),
            mclogin.getRequestInstant(),
            mclogin.getRequestOrigin()
          ),
          b -> xfrmMref.fromBackend(b))
        )

        // member logout
        .dataFetcher("mlogout", env -> processor.handleMutation(
          env,
          () -> new Mlogout(
            uuidFromToken(env.getArgument("mid")),
            ((JWTUserGraphQLWebContext) env.getContext()).getJwtRequestProvider().getRequestInstant(),
            ((JWTUserGraphQLWebContext) env.getContext()).getJwtRequestProvider().getClientOrigin()
          ),
          mclogout -> mcorpusRepo.memberLogout(
            mclogout.getMid(),
            mclogout.getRequestInstant(),
            mclogout.getRequestOrigin()
          ),
          mid -> mid != null)
        )

        // add member
        .dataFetcher("addMember", env -> processor.handleMutation(
          env,
          () -> xfrmMember.fromGraphQLMapForAdd(env.getArgument("member")),
          (Member g) -> vldtnMember.validateForAdd(g),
          gvldtd -> xfrmMember.toBackend(gvldtd),
          (MemberAndMauth b) -> mcorpusRepo.addMember(b),
          bpost -> xfrmMember.fromBackend(bpost))
        )

        // update member
        .dataFetcher("updateMember", env -> processor.handleMutation(
          env,
          () -> xfrmMember.fromGraphQLMapForUpdate(env.getArgument("member")),
          (Member g) -> vldtnMember.validateForUpdate(g),
          gvldtd -> xfrmMember.toBackend(gvldtd),
          (MemberAndMauth b) -> mcorpusRepo.updateMember(b),
          bpost -> xfrmMember.fromBackend(bpost))
        )

        // delete member
        .dataFetcher("deleteMember", env -> processor.handleDeletion(
          env,
          () -> uuidFromToken(env.getArgument("mid")),
          key -> key,
          b -> mcorpusRepo.deleteMember(b))
        )

        // member pswd
        .dataFetcher("mpswd", env -> processor.handleSimpleMutation(
          env,
          () -> new MemberIdAndPswdKey(
            uuidFromToken(env.getArgument("mid")),
            clean(env.getArgument("pswd"))
          ),
          key -> mcorpusRepo.setMemberPswd(key.getMid(), key.getPswd()),
          fr -> fr.get())
        )

        // add member address
        .dataFetcher("addMemberAddress", env -> processor.handleMutation(
          env,
          () -> xfrmMemberAddress.fromGraphQLMapForAdd(env.getArgument("memberAddress")),
          (MemberAddress g) -> vldtnMemberAddress.validateForAdd(g),
          gvldtd -> xfrmMemberAddress.toBackend(gvldtd),
          (Maddress b) -> mcorpusRepo.addMemberAddress(b),
          bpost -> xfrmMemberAddress.fromBackend(bpost))
        )

        // update member address
        .dataFetcher("updateMemberAddress", env -> processor.handleMutation(
          env,
          () -> xfrmMemberAddress.fromGraphQLMapForUpdate(env.getArgument("memberAddress")),
          (MemberAddress g) -> vldtnMemberAddress.validateForUpdate(g),
          gvldtd -> xfrmMemberAddress.toBackend(gvldtd),
          (Maddress b) -> mcorpusRepo.updateMemberAddress(b),
          bpost -> xfrmMemberAddress.fromBackend(bpost))
        )

        // delete member address
        .dataFetcher("deleteMemberAddress", env -> processor.handleDeletion(
          env,
          () -> new MidAndAddressNameKey(
            uuidFromToken(env.getArgument("mid")),
            clean(env.getArgument("addressName"))
          ),
          keyg -> xfrmMidAndAddressName.toBackend(keyg),
          key -> mcorpusRepo.deleteMemberAddress(key.getMid(), key.getAddressname())
        ))
      )

      // mcuser types

      // IJwtUserStatus
      .type("Mcstatus", typeWiring -> typeWiring
        .dataFetcher("uid", env -> {
          final IJwtUserStatus jwtus = env.getSource();
          return uuidToToken(jwtus.getJwtUserId());
        })
        .dataFetcher("since", env -> {
          final IJwtUserStatus jwtus = env.getSource();
          return jwtus.getSince();
        })
        .dataFetcher("expires", env -> {
          final IJwtUserStatus jwtus = env.getSource();
          return jwtus.getExpires();
        })
        .dataFetcher("numActiveJWTs", env -> {
          final IJwtUserStatus jwtus = env.getSource();
          return jwtus.getNumActiveJWTs();
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
          return uuidToToken(mh.getPk().getUUID());
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
          return uuidToToken(le.jwtId);
        })
        .dataFetcher("timestamp", env -> {
          final LoginEvent le = env.getSource();
          return le.timestamp;
        })
        .dataFetcher("clientOrigin", env -> {
          final LoginEvent le = env.getSource();
          return le.clientOrigin;
        })
      )

      // McuserLogoutEvent
      .type("McuserLogoutEvent", typeWiring -> typeWiring
        .dataFetcher("jwtId", env -> {
          final LogoutEvent le = env.getSource();
          return uuidToToken(le.jwtId);
        })
        .dataFetcher("timestamp", env -> {
          final LogoutEvent le = env.getSource();
          return le.timestamp;
        })
        .dataFetcher("clientOrigin", env -> {
          final LogoutEvent le = env.getSource();
          return le.clientOrigin;
        })
      )

      // mcorpus types

      // MRef
      .type("MRef", typeWiring -> typeWiring
        .dataFetcher("mid", env -> {
          final Mref mref = env.getSource();
          return uuidToToken(mref.getPk().getUUID());
        })
        .dataFetcher("empId", env -> {
          final Mref mref = env.getSource();
          return mref.empId;
        })
        .dataFetcher("location", env -> {
          final Mref mref = env.getSource();
          return mref.location;
        })
      )

      // Member
      .type("Member", typeWiring -> typeWiring
        .dataFetcher("mid", env -> {
          final Member m = env.getSource();
          return uuidToToken(m.getMid());
        })
        .dataFetcher("created", env -> {
          final Member m = env.getSource();
          return m.getCreated();
        })
        .dataFetcher("modified", env -> {
          final Member m = env.getSource();
          return m.getModified();
        })
        .dataFetcher("empId", env -> {
          final Member m = env.getSource();
          return m.getEmpId();
        })
        .dataFetcher("location", env -> {
          final Member m = env.getSource();
          return m.getLocation();
        })
        .dataFetcher("nameFirst", env -> {
          final Member m = env.getSource();
          return m.getNameFirst();
        })
        .dataFetcher("nameMiddle", env -> {
          final Member m = env.getSource();
          return m.getNameMiddle();
        })
        .dataFetcher("nameLast", env -> {
          final Member m = env.getSource();
          return m.getNameLast();
        })
        .dataFetcher("displayName", env -> {
          final Member m = env.getSource();
          return m.getDisplayName();
        })
        .dataFetcher("status", env -> {
          final Member m = env.getSource();
          return m.getStatus();
        })
        .dataFetcher("dob", env -> {
          final Member m = env.getSource();
          return m.getDob();
        })
        .dataFetcher("ssn", env -> {
          final Member m = env.getSource();
          return m.getSsn();
        })
        .dataFetcher("personalEmail", env -> {
          final Member m = env.getSource();
          return m.getPersonalEmail();
        })
        .dataFetcher("workEmail", env -> {
          final Member m = env.getSource();
          return m.getWorkEmail();
        })
        .dataFetcher("mobilePhone", env -> {
          final Member m = env.getSource();
          return m.getMobilePhone();
        })
        .dataFetcher("homePhone", env -> {
          final Member m = env.getSource();
          return m.getHomePhone();
        })
        .dataFetcher("workPhone", env -> {
          final Member m = env.getSource();
          return m.getWorkPhone();
        })
        .dataFetcher("username", env -> {
          final Member m = env.getSource();
          return m.getUsername();
        })
        .dataFetcher("addresses", env -> {
          final Object mo = env.getSource();
          if(mo instanceof MemberAndAddresses) {
            final MemberAndAddresses maa = (MemberAndAddresses) mo;
            return maa.getAddresses();
          } else {
            return processor.fetch(
              env,
              () -> ((Member) mo).getMid(),
              mid -> mcorpusRepo.fetchMemberAddresses(mid),
              blist -> blist.stream().map(b -> xfrmMemberAddress.fromBackend(b)).collect(Collectors.toList())
            );
          }
        })
      )

      // MemberAddress
      .type("MemberAddress", typeWiring -> typeWiring
        .dataFetcher("mid", env -> {
          final MemberAddress ma = env.getSource();
          return uuidToToken(ma.getMid());
        })
        .dataFetcher("addressName", env -> {
          final MemberAddress ma = env.getSource();
          return ma.getAddressName();
        })
        .dataFetcher("modified", env -> {
          final MemberAddress ma = env.getSource();
          return ma.getModified();
        })
        .dataFetcher("attn", env -> {
          final MemberAddress ma = env.getSource();
          return ma.getAttn();
        })
        .dataFetcher("street1", env -> {
          final MemberAddress ma = env.getSource();
          return ma.getStreet1();
        })
        .dataFetcher("street2", env -> {
          final MemberAddress ma = env.getSource();
          return ma.getStreet2();
        })
        .dataFetcher("city", env -> {
          final MemberAddress ma = env.getSource();
          return ma.getCity();
        })
        .dataFetcher("state", env -> {
          final MemberAddress ma = env.getSource();
          return ma.getState();
        })
        .dataFetcher("postalCode", env -> {
          final MemberAddress ma = env.getSource();
          return ma.getPostalCode();
        })
        .dataFetcher("country", env -> {
          final MemberAddress ma = env.getSource();
          return ma.getCountry();
        })
      )

      .build();
  }

}
