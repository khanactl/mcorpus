
package com.tll.mcorpus.validate;

import com.tll.mcorpus.gmodel.EmpIdAndLocationKey;
import com.tll.validate.VldtnBuilder;

public class EmpIdAndLocationValidator extends BaseMcorpusValidator<EmpIdAndLocationKey> {

  @Override
  protected void validate(final VldtnBuilder<EmpIdAndLocationKey> vldtn) {
    vldtn
      .vtok(MemberValidator::empIdValid, EmpIdAndLocationKey::empId, "member.empId.emsg", "empId")
      .vtok(MemberValidator::locationValid, EmpIdAndLocationKey::location, "member.location.emsg", "location")
    ;
  }
}