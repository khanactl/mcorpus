package com.tll.validate;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotBlank;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.upper;
import static com.tll.validate.VldtnCore.lenchk;
import static com.tll.validate.VldtnCore.namePattern;
import static com.tll.validate.VldtnTestHelper.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.tll.UnitTest;
import com.tll.gmodel.BaseEntity;
import com.tll.gmodel.IKey;

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

  static class TestEntity extends BaseEntity<TestEntity, IKey> {
    private final IKey pk;
    private final UUID id;
    private final Date created;
    private final String name;
    private final String email;
    private final String username;
    private final String status;

    public TestEntity(UUID id, String name, String email, String username, String status) {
      this.pk = IKey.uuid("TestEntity", id);
      this.id = id;
      this.created = new Date();
      this.name = name;
      this.email = email;
      this.username = username;
      this.status = status;
    }

    @Override
    public IKey getPk() { return pk; }
    public UUID getId() { return id; }
    public Date getCreated() { return created; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getStatus() { return status; }
  } // TestEntity
  
  static class TestValidator extends BaseValidator<TestEntity> {

    @Override
    protected String getEntityTypeName() {
      return "TestEntity";
    }

    @Override
    protected String getValidationMsgsRootName() {
      return "validate-test";
    }

    protected void validate(final VldtnBuilder<TestEntity> vldtn) {
      vldtn
        .vtok(TestValidator::nameValid, TestEntity::getName, "test.name.emsg", "name")
        .vrqd(VldtnCore::emailValid, TestEntity::getEmail, "test.email.emsg", "email")
        .vrqd(VldtnCore::usernameValid, TestEntity::getUsername, "test.username.emsg", "username")
        .vrqd(TestValidator::statusValid, TestEntity::getStatus, "test.status.emsg", "status")
      ;
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
  public void test() {
    
    String name = "wwwwname-)000$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$432";
    String email = "testentity@myfrigginmail.com";
    String username = "testuser";
    String status = "ACTIVE";
    TestEntity e = new TestEntity(UUID.randomUUID(), name, email, username, status);
    
    TestValidator validator = new TestValidator();
    VldtnResult vresult = validator.validate(e);
    Set<VldtnErr> verrs = vresult.getErrors();
    verify(vresult, false, 1);
    
    VldtnErr verr = verrs.iterator().next();
    assertNotNull(verr);
    assertEquals("TestEntity", verr.etype());
    assertEquals("name", verr.getFieldName());
    assertEquals(name, verr.getFieldValue());
    assertEquals("Invalid test Name.", verr.getVldtnErrMsg());

    log.info(verr);
  }

}