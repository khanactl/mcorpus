package com.tll.validate;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotBlank;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.upper;
import static com.tll.validate.VldtnCore.lenchk;
import static com.tll.validate.VldtnCore.namePattern;
import static com.tll.validate.VldtnTestHelper.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.tll.UnitTest;
import com.tll.gmodel.BaseEntity;
import com.tll.gmodel.IKey;
import com.tll.gmodel.UUIDKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class ValidatorTest {

  private static final Logger log = LogManager.getLogger();

  static enum TestStatus {
    ACTIVE, INACTIVE;
  }

  static class NestedEntity extends BaseEntity<TestEntity, IKey> {
    private final UUIDKey pk;
    private final String nprop;

    public NestedEntity(String nprop) {
      this.pk = new UUIDKey(UUID.randomUUID(), "NestedEntity");
      this.nprop = nprop;
    }

    @Override
    public IKey getPk() { return pk; }

    public String getNProp() { return nprop; }
  } // NestedEntity
  static class TestEntity extends BaseEntity<TestEntity, IKey> {

    private final UUIDKey pk;
    private final Date created;
    private final String name;
    private final String email;
    private final String username;
    private final String status;
    public NestedEntity nentity;

    public TestEntity(String name, String email, String username, String status, String nprop) {
      this.pk = new UUIDKey(UUID.randomUUID(), "TestEntity");
      this.created = new Date();
      this.name = name;
      this.email = email;
      this.username = username;
      this.status = status;
      this.nentity = new NestedEntity(nprop);
    }

    @Override
    public IKey getPk() { return pk; }
    public Date getCreated() { return created; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getStatus() { return status; }
    public NestedEntity getNEntity() { return nentity; }
  } // TestEntity

  static class NestedValidator extends BaseValidator<NestedEntity> {

    @Override
    public String getEntityTypeName() {
      return "TestEntity";
    }

    @Override
    protected String getValidationMsgsRootName() {
      return "validate-test";
    }

    @Override
    protected void doValidate(final VldtnBuilder<NestedEntity> vldtn) {
      vldtn
        .vtok(nprop -> lenchk(nprop, 2), NestedEntity::getNProp, "nested.nprop.emsg", "nprop")
      ;
    }

    @Override
    protected void doValidateForUpdate(final VldtnBuilder<NestedEntity> vldtn) {
      doValidate(vldtn);
    }

    @Override
    protected boolean hasAnyUpdatableFields(NestedEntity e) {
      return isNotBlank(e.getNProp());
    }

    @Override
    protected String getVmkForNoUpdateFieldsPresent() {
      return "nested.noupdatefields.emsg";
    }

  }

  static class TestValidator extends BaseValidator<TestEntity> {

    @Override
    public String getEntityTypeName() {
      return "TestEntity";
    }

    @Override
    protected String getValidationMsgsRootName() {
      return "validate-test";
    }

    @Override
    protected void doValidate(final VldtnBuilder<TestEntity> vldtn) {
      vldtn
        .vrqd(TestValidator::nameValid, TestEntity::getName, "test.name.emsg", "name")
        .vrqd(VldtnCore::emailValid, TestEntity::getEmail, "test.email.emsg", "email")
        .vrqd(VldtnCore::usernameValid, TestEntity::getUsername, "test.username.emsg", "username")
        .vrqd(TestValidator::statusValid, TestEntity::getStatus, "test.status.emsg", "status")
        .vrqd(TestEntity::getNEntity, "test.nentity.notPresent.emsg", "nested")
        // nested entity validation
        .vnested(
          TestEntity::getNEntity,
          "nentity",
          new NestedValidator()
        )
      ;
    }

    @Override
    protected void doValidateForUpdate(final VldtnBuilder<TestEntity> vldtn) {
      doValidate(vldtn);
    }

    @Override
    protected boolean hasAnyUpdatableFields(TestEntity e) {
      return
        isNotBlank(e.getName()) ||
        isNotBlank(e.getEmail()) ||
        isNotBlank(e.getUsername()) ||
        isNotBlank(e.getStatus()) ||
        isNotNull(e.getNEntity())
        ;
    }

    @Override
    protected String getVmkForNoUpdateFieldsPresent() {
      return "test.noupdatefields.emsg";
    }

    static boolean nameValid(final String name) {
      return isNotBlank(name) && lenchk(name, 64) && namePattern.matcher(name).matches();
    }

    static boolean statusValid(final String status) {
      try {
        return isNotNull(TestStatus.valueOf(upper(clean(status))));
      } catch(Exception e) {
        return false;
      }
    }

  } // TestValidator

  @Test
  public void testAtLeastOneFieldForUpdatePass() {

    TestEntity e = new TestEntity(null, "email@schmail.com", null, null, null);
    e.nentity = null;

    TestValidator validator = new TestValidator();
    VldtnResult vresult = validator.validateForUpdate(e);
    Set<VldtnErr> verrs = vresult.getErrors();
    for(VldtnErr ve : verrs) {
      assertNotEquals("", ve.getFieldPath());
      assertNotEquals("No Test entity update fields provided.", ve.getVldtnErrMsg());
    }
    verify(vresult, false, 4); // i.e. the 4 null test entity fields
  }

  @Test
  public void testAtLeastFieldOneForUpdateFail() {

    TestEntity e = new TestEntity(null, "  ", null, null, null);
    e.nentity = null;

    TestValidator validator = new TestValidator();
    VldtnResult vresult = validator.validateForUpdate(e);
    Set<VldtnErr> verrs = vresult.getErrors();
    verify(vresult, false, 1);

    Iterator<VldtnErr> veitr = verrs.iterator();

    VldtnErr verr = veitr.next();
    log.info(verr);

    assertNotNull(verr);
    assertEquals("TestEntity", verr.getParentType());
    assertEquals("", verr.getFieldName());
    assertEquals("", verr.getFieldPath());
    assertEquals("No Test entity update fields provided.", verr.getVldtnErrMsg());

  }

  @Test
  public void testValidate() {

    String name = "wwwwname-)000$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$432";
    String email = "testentity@myfrigginmail.com";
    String username = "testuser";
    String status = "ACTIVE";
    String nprop = "1234";
    TestEntity e = new TestEntity(name, email, username, status, nprop);

    TestValidator validator = new TestValidator();
    VldtnResult vresult = validator.validate(e);
    Set<VldtnErr> verrs = vresult.getErrors();
    verify(vresult, false, 2);

    Iterator<VldtnErr> veitr = verrs.iterator();

    VldtnErr verr = veitr.next();
    log.info(verr);

    assertNotNull(verr);
    assertEquals("TestEntity", verr.getParentType());
    assertEquals("name", verr.getFieldName());
    assertEquals("name", verr.getFieldPath());
    assertEquals("Invalid Test entity Name.", verr.getVldtnErrMsg());

    verr = veitr.next();
    log.info(verr);

    assertNotNull(verr);
    assertEquals("TestEntity", verr.getParentType());
    assertEquals("nprop", verr.getFieldName());
    assertEquals("nentity.nprop", verr.getFieldPath());
    assertEquals("Invalid nested entity property value.", verr.getVldtnErrMsg());

  }

}