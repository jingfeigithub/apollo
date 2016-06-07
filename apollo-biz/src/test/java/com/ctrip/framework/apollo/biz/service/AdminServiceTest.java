package com.ctrip.framework.apollo.biz.service;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.framework.apollo.biz.BizTestConfiguration;
import com.ctrip.framework.apollo.biz.entity.App;
import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.repository.AppRepository;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.exception.ServiceException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BizTestConfiguration.class)
@Transactional
@Rollback
public class AdminServiceTest {

  @Autowired
  private AdminService adminService;

  @Autowired
  private AuditService auditService;

  @Autowired
  private AppRepository appRepository;

  @Autowired
  private ClusterService clsuterService;

  @Autowired
  private NamespaceService namespaceService;

  @Test
  public void testCreateNewApp() {
    String appId = "someAppId";
    App app = new App();
    app.setAppId(appId);
    app.setName("someAppName");
    String owner = "someOwnerName";
    app.setOwnerName(owner);
    app.setOwnerEmail("someOwnerName@ctrip.com");
    app.setDataChangeCreatedBy(owner);
    app.setDataChangeLastModifiedBy(owner);
    app.setDataChangeCreatedTime(new Date());

    app = adminService.createNewApp(app);
    Assert.assertEquals(appId, app.getAppId());

    List<Cluster> clusters = clsuterService.findClusters(app.getAppId());
    Assert.assertEquals(1, clusters.size());
    Assert.assertEquals(ConfigConsts.CLUSTER_NAME_DEFAULT, clusters.get(0).getName());

    List<Namespace> namespaces = namespaceService.findNamespaces(appId, clusters.get(0).getName());
    Assert.assertEquals(1, namespaces.size());
    Assert.assertEquals(ConfigConsts.NAMESPACE_APPLICATION, namespaces.get(0).getNamespaceName());

    List<Audit> audits = auditService.findByOwner(owner);
    Assert.assertEquals(4, audits.size());
  }

  @Test(expected = ServiceException.class)
  public void testCreateDuplicateApp() {
    String appId = "someAppId";
    App app = new App();
    app.setAppId(appId);
    app.setName("someAppName");
    String owner = "someOwnerName";
    app.setOwnerName(owner);
    app.setOwnerEmail("someOwnerName@ctrip.com");
    app.setDataChangeCreatedBy(owner);
    app.setDataChangeLastModifiedBy(owner);
    app.setDataChangeCreatedTime(new Date());

    appRepository.save(app);

    adminService.createNewApp(app);
  }

}
