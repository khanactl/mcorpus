package com.tll.mcorpus.transform;

import static com.tll.core.Util.clean;
import static com.tll.mcorpus.transform.MemberXfrm.locationFromString;

import com.tll.mcorpus.dmodel.EmpIdAndLocation;
import com.tll.mcorpus.gmodel.EmpIdAndLocationKey;

public class EmpIdAndLocationXfrm extends BaseMcorpusTransformer<EmpIdAndLocationKey, EmpIdAndLocation> {

  protected EmpIdAndLocation toBackendFromNonNull(final EmpIdAndLocationKey e) {
    return new EmpIdAndLocation(
      clean(e.empId()),
      locationFromString(e.location())
    );
  }

}