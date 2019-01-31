package com.tll.mcorpus.repo.model;

import static com.tll.mcorpus.Util.clean;
import static com.tll.mcorpus.Util.isBlank;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.enums.McuserStatus;

/**
 * McuserToAdd GraphQL type pojo.
 * 
 * @author jpk
 */
public class McuserToAdd {

  public static McuserToAdd fromMap(final Map<String, Object> map) {
    McuserToAdd mta = null;
    if(map != null) {
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
      mta = new McuserToAdd(name, email, username, pswd, initialStatus, roles);
    }
    return mta;
  }

  private final String name;
  private final String email;
  private final String username;
  private final String pswd;
  private final McuserStatus initialStatus;
  private final Set<McuserRole> roles;

  /**
   * Constructor.
   * 
   * @param name
   * @param email
   * @param username
   * @param pswd
   * @param initialStatus
   * @param roles
   */
  public McuserToAdd(String name, String email, String username, String pswd, McuserStatus initialStatus, Set<McuserRole> roles) {
    this.name = name;
    this.email = email;
    this.username = username;
    this.pswd = pswd;
    this.initialStatus = initialStatus;
    this.roles = roles;
  }

  /**
   * Constructor.
   * 
   * @param name
   * @param email
   * @param username
   * @param pswd
   * @param initialStatus
   * @param roles
   */
  public McuserToAdd(String name, String email, String username, String pswd, McuserStatus initialStatus, McuserRole[] roles) {
    this(name, email, username, pswd, initialStatus, 
      isNullOrEmpty(roles) ? 
        Collections.emptySet() : 
        Arrays.stream(roles).collect(Collectors.toSet())
    );
  }

  public String getName() { return clean(name); }

  public String getUsername() { return clean(username); }

  public String getEmail() { return clean(email); }
  
  public String getPswd() { return clean(pswd); }
  
  public McuserStatus getInitialStatus() { return initialStatus; }
  
  public Set<McuserRole> getRoles() { return roles; }
  
  public String rolesToken() {
    return roles == null ? null : 
      roles.stream()
        .map(role -> { return role.getLiteral(); })
        .collect(Collectors.joining(","));
  }

  @Override
  public String toString() {
    return String.format(
      "McuserToAdd[name: %s, email: %s, username: %s, initialStatus: %s, roles: %s", 
      name, email, username, initialStatus, roles
    );
  }
}