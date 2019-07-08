package com.tll.mcorpus.validate;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotBlank;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.upper;
import static com.tll.validate.VldtnCore.lenchk;
import static com.tll.validate.VldtnCore.namePattern;

import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.gmodel.mcuser.Mcuser;
import com.tll.validate.VldtnBuilder;
import com.tll.validate.VldtnCore;

public class McuserValidator extends BaseMcorpusValidator<Mcuser> {

  @Override
  public String getEntityTypeName() { return "Member"; }
  
  @Override
  protected void validateForAdd(final VldtnBuilder<Mcuser> vldtn) {
    vldtn
      .vrqd(McuserValidator::mcuserNameValid, Mcuser::getName, "mcuser.name.emsg", "name")
      .vrqd(VldtnCore::emailValid, Mcuser::getEmail, "mcuser.email.emsg", "email")
      .vrqd(VldtnCore::usernameValid, Mcuser::getUsername, "mcuser.username.emsg", "username")
      .vrqd(VldtnCore::pswdValid, Mcuser::getPswd, "mcuser.pswd.emsg", "pswd")
      .vrqd(McuserValidator::mcuserStatusValid, Mcuser::getStatus, "mcuser.status.emsg", "initialStatus")
    ;
  }

  @Override
  protected void validateForUpdate(final VldtnBuilder<Mcuser> vldtn) {
    vldtn
      // require pk
      .vrqd(t -> t.isSet(), Mcuser::getPk, "mcuser.nopk.emsg", "pk")
      
      .vtok(McuserValidator::mcuserNameValid, Mcuser::getName, "mcuser.name.emsg", "name")
      .vtok(VldtnCore::emailValid, Mcuser::getEmail, "mcuser.email.emsg", "email")
      .vtok(VldtnCore::usernameValid, Mcuser::getUsername, "mcuser.username.emsg", "username")
      .vtok(McuserValidator::mcuserStatusValid, Mcuser::getStatus, "mcuser.status.emsg", "status")
    ;
  }

  public static boolean mcuserNameValid(final String name) {
    return isNotBlank(name) && lenchk(name, 64) && namePattern.matcher(name).matches();
  }

  public static boolean mcuserStatusValid(final String status) {
    return isNotNull(mcuserStatusFromString(status));
  }

  private static McuserStatus mcuserStatusFromString(final String s) {
    try {
      return McuserStatus.valueOf(upper(clean(s)));
    } catch(Exception e) {
      return null;
    }
  }  
}