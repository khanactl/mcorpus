
package com.tll.mcorpus.validate;

import com.tll.mcorpus.gmodel.EmpIdAndLocationKey;
import com.tll.validate.VldtnBuilder;

public class EmpIdAndLocationValidator extends BaseMcorpusValidator<EmpIdAndLocationKey> {

  @Override
  public String getEntityTypeName() { return "EmpIdAndLocation"; }
  
  @Override
  protected void doValidate(final VldtnBuilder<EmpIdAndLocationKey> vldtn) {
    vldtn
      .vtok(MemberValidator::empIdValid, EmpIdAndLocationKey::empId, "member.empId.emsg", "empId")
      .vtok(MemberValidator::locationValid, EmpIdAndLocationKey::location, "member.location.emsg", "location")
    ;
  }

  @Override
  protected boolean hasAnyUpdatableFields(EmpIdAndLocationKey e) { return false; }

  @Override
  protected String getVmkForNoUpdateFieldsPresent() { return null; }
}