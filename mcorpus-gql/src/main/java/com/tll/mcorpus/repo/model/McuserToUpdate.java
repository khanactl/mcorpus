package com.tll.mcorpus.repo.model;

import static com.tll.mcorpus.Util.isBlank;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;
import static com.tll.mcorpus.Util.nclean;
import static com.tll.mcorpus.Util.uuidFromToken;
import static com.tll.mcorpus.db.Tables.MCUSER;
import static com.tll.mcorpus.repo.RepoUtil.fputWhenNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.enums.McuserStatus;

/**
 * McuserToUpdate GraphQL type pojo.
 * 
 * @author jpk
 */
public class McuserToUpdate {

  public static McuserToUpdate fromMap(final Map<String, Object> map) {
    McuserToUpdate mta = null;
    if(map != null) {
      UUID uid = null;
      String name = null;
      String email = null;
      String username = null;
      String pswd = null;
      McuserStatus initialStatus = null;
      Set<McuserRole> roles = null;
      for(final Entry<String, Object> entry : map.entrySet()) {
        String key = entry.getKey();
        if(!isBlank(key)) {
          switch(key) {
            case "uid":
              uid = uuidFromToken((String) entry.getValue());
              break;
            case "name":
              name = (String) entry.getValue();
              break;
            case "email":
              email = (String) entry.getValue();
              break;
            case "username":
              username = (String) entry.getValue();
              break;
            case "pswd":
              pswd = (String) entry.getValue();
              break;
            case "initialStatus":
              initialStatus = McuserStatus.valueOf((String) entry.getValue());
              break;
            case "roles":
              @SuppressWarnings("unchecked")
              List<String> rlist = (List<String>) entry.getValue();
              if(not(isNullOrEmpty(rlist))) {
                roles = rlist.stream().map(role -> {
                  return McuserRole.valueOf(role);
                }).collect(Collectors.toSet());
              }
              break;
          }
        }
      }
      mta = new McuserToUpdate(uid, name, email, username, initialStatus, roles);
    }
    return mta;
  }

  public final UUID uid;
  public final String name;
  public final String email;
  public final String username;
  public final McuserStatus status;
  public final Set<McuserRole> roles;

  /**
   * Constructor.
   * 
   * @param uid
   * @param name
   * @param email
   * @param username
   * @param status
   * @param roles
   */
  public McuserToUpdate(UUID uid, String name, String email, String username, McuserStatus status, Set<McuserRole> roles) {
    this.uid = uid;
    this.name = name;
    this.email = email;
    this.username = username;
    this.status = status;
    this.roles = roles;
  }

  public Map<String, Object> asUpdateMap() {
    Map<String, Object> rmap = new HashMap<>(4);
    fputWhenNotNull(MCUSER.NAME, nclean(name), rmap);
    fputWhenNotNull(MCUSER.EMAIL, nclean(email), rmap);
    fputWhenNotNull(MCUSER.USERNAME, nclean(username), rmap);
    fputWhenNotNull(MCUSER.STATUS, status, rmap);
    return rmap;
  }

  public String rolesToken() {
    return roles == null ? null : 
      roles.stream()
        .map(role -> { return role.getLiteral(); })
        .collect(Collectors.joining(","));
  }

  @Override
  public String toString() {
    return String.format(
      "McuserToUpdate[uid: %s, name: %s, email: %s, username: %s, status: %s, roles: %s", 
      uid, name, email, username, status, roles
    );
  }
}