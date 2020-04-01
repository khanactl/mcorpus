package com.tll.mcorpus.transform;

import static com.tll.core.Util.isNull;
import static com.tll.transform.TransformUtil.odtToDate;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tll.mcorpus.dmodel.McuserHistoryDomain;
import com.tll.mcorpus.gmodel.mcuser.McuserHistory;
import com.tll.transform.BaseTransformer;

public class McuserHistoryXfrm extends BaseTransformer<McuserHistory, McuserHistoryDomain> {

  public static List<McuserHistory.LoginEvent> backToFrontLogin(final List<McuserHistoryDomain.LoginEventDomain> blist) {
    return isNull(blist) ? null : blist.stream()
      .map(b -> new McuserHistory.LoginEvent(
        b.jwtId,
        odtToDate(b.timestamp),
        fromInet(b.requestOrigin)
      ))
      .collect(Collectors.toList())
    ;
  }

  public static List<McuserHistory.LogoutEvent> backToFrontLogout(final List<McuserHistoryDomain.LogoutEventDomain> blist) {
    return isNull(blist) ? null : blist.stream()
      .map(b -> new McuserHistory.LogoutEvent(
        b.jwtId,
        odtToDate(b.timestamp),
        fromInet(b.requestOrigin)
      ))
      .collect(Collectors.toList())
    ;
  }

  private static String fromInet(final InetAddress ia) {
    return ia == null ? null : ia.getHostAddress();
  }

  @Override
  protected McuserHistory fromNotEmptyGraphQLMapForAdd(final Map<String, Object> gqlMap) {
    throw new UnsupportedOperationException();
  }

  @Override
  public McuserHistory fromNotEmptyGraphQLMapForUpdate(final Map<String, Object> gqlMap) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected McuserHistory fromNonNullBackend(final McuserHistoryDomain d) {
    return new McuserHistory(d.uid, backToFrontLogin(d.logins), backToFrontLogout(d.logouts));
  }

  @Override
  protected McuserHistoryDomain toBackendFromNonNull(final McuserHistory g) {
    throw new UnsupportedOperationException();
  }

}