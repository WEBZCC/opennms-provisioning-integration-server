package org.opennms.opennms.pris.plugins.xls.source;

import java.nio.file.Paths;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.opennms.pris.api.MockInstanceConfiguration;
import org.opennms.pris.model.AssetField;
import org.opennms.pris.model.MetaData;
import org.opennms.pris.model.PrimaryType;
import org.opennms.pris.model.Requisition;
import org.opennms.pris.model.RequisitionCategory;
import org.opennms.pris.model.RequisitionInterface;
import org.opennms.pris.model.RequisitionMonitoredService;
import org.opennms.pris.model.RequisitionNode;
import org.opennms.pris.util.RequisitionUtils;

public class XlsSourceTest {

    private XlsSource xlsSource;

    @Before
    public void setUp() {
        MockInstanceConfiguration config = new MockInstanceConfiguration("test");
        config.set("encoding", "ISO-8859-1");
        config.set("file", Paths.get("src/test/resources/test.xls"));

        xlsSource = new XlsSource(config);
    }

    @Test
    public void basicTest() throws Exception {
        Requisition resultRequisition = (Requisition) xlsSource.dump();
        
        assertEquals(resultRequisition.getForeignSource(), "test");
        assertEquals(2, resultRequisition.getNodes().size());
        
        RequisitionNode resultNode = resultRequisition.getNodes().get(0);
        assertEquals("TestNode", resultNode.getNodeLabel());
        assertEquals("TestNode", resultNode.getForeignId());
        assertEquals("Test-Parent-Foreign-Source", resultNode.getParentForeignSource());
        assertEquals("Test-Parent-Foreign-Id", resultNode.getParentForeignId());
        assertEquals("Test-Parent-Node-Label", resultNode.getParentNodeLabel());
        assertEquals("Test-Location", resultNode.getLocation());
        
        assertEquals(RequisitionUtils.findAsset(resultNode, AssetField.vendor.name).getValue(), "Vater");
        assertEquals(RequisitionUtils.findAsset(resultNode, AssetField.city.name).getValue(), "Braunschweig");
        assertEquals(RequisitionUtils.findAsset(resultNode, AssetField.vendorPhone.name).getValue(), "123");
        assertEquals(RequisitionUtils.findAsset(resultNode, AssetField.address1.name).getValue(), "Wilhelmstraße 30");
        assertEquals(RequisitionUtils.findAsset(resultNode, AssetField.description.name).getValue(), "POB: Johann Carl Friedrich Gauß");
        assertEquals(RequisitionUtils.findAsset(resultNode, AssetField.comment.name).getValue(), "Died in Göttingen");
        
        assertEquals(RequisitionUtils.findAsset(resultNode, AssetField.latitude.name).getValue(), "54.9633229");
        assertEquals(RequisitionUtils.findAsset(resultNode, AssetField.longitude.name).getValue(), "1");
        
        RequisitionInterface resultInterface = RequisitionUtils.findInterface(resultNode, "1.2.3.4");
        assertEquals(PrimaryType.PRIMARY, resultInterface.getSnmpPrimary());
        assertEquals(1, resultInterface.getStatus());
        
        RequisitionMonitoredService resultService = RequisitionUtils.findMonitoredService(resultInterface, "Test");
        assertEquals("Test", resultService.getServiceName());
        
        RequisitionCategory findCategory = RequisitionUtils.findCategory(resultNode, "Test");
        assertEquals("Test", findCategory.getName());

        assertThat(resultNode.getMetaDatas(), containsInAnyOrder(
                new MetaData("requisition", "KeyWithoutContext", "Foo"),
                new MetaData("Context", "KeyWithContext", "Bar")));
    }

    @Test
    public void getNodeWithMultipleIpInterfaces() throws Exception {
        Requisition resultRequisition = (Requisition) xlsSource.dump();
        assertEquals(resultRequisition.getForeignSource(), "test");
        RequisitionNode resultNode = resultRequisition.getNodes().get(1);
        assertEquals(resultNode.getInterfaces().size(), 2);
        assertEquals(resultNode.getNodeLabel(), "Node2Ips");
        assertEquals(resultNode.getInterfaces().get(0).getIpAddr(),"23.23.23.23");
        assertEquals(resultNode.getInterfaces().get(0).getSnmpPrimary(),"P");
        assertEquals(resultNode.getInterfaces().get(1).getIpAddr(),"42.42.42.42");
        assertEquals(resultNode.getInterfaces().get(1).getSnmpPrimary(),"S");
    }
}
