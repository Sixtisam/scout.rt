/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SmartFieldParseValueTest {

  private static List<IBean<?>> m_beans;

  private AbstractSmartField m_smartField;

  @BeforeClass
  public static void beforeClass() throws Exception {
    m_beans = TestingUtility.registerBeans(new BeanMetaData(P_LookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(m_beans);
  }

  @Before
  public void setUp() throws ProcessingException {
    m_smartField = new SmartField();
    m_smartField.registerProposalChooserInternal();
  }

  /**
   * Tests the case where only a single proposal matches the seachText and the proposal is accepted.
   */
  @Test
  public void testSingleMatch() throws ProcessingException, InterruptedException {
    testMatch("a", 1L, "aName", 1, false, false);
  }

  @Test
  public void testMultiMatch() throws ProcessingException, InterruptedException {
    testMatch("b", 0L, null, 2, true, true);
  }

  /**
   * Other than in earlier versions of the SmartField the proposal chooser
   */
  @Test
  public void testNoMatch() throws ProcessingException, InterruptedException {
    testMatch("c", 0L, null, 0, false, true);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSetValue() throws Exception {
    m_smartField.setValue(1L);
    assertFalse(m_smartField.isEmptyCurrentLookupRow());
    assertEquals(1L, m_smartField.getValue());
    assertEquals("aName", m_smartField.getDisplayText());
  }

  @Test
  public void testParseAndSetValue() throws Exception {
    m_smartField.parseAndSetValue("aName");
    assertFalse(m_smartField.isEmptyCurrentLookupRow());
    assertEquals(1L, m_smartField.getValue());
    assertEquals("aName", m_smartField.getDisplayText());
  }

  @Test
  public void testParseAndSetValue_InvalidValue() throws Exception {
    m_smartField.parseAndSetValue("FooBar");
    assertTrue(m_smartField.isEmptyCurrentLookupRow());
    assertNotNull(m_smartField.getErrorStatus());

    // When value becomes valid again, error status must be removed
    m_smartField.parseAndSetValue("aName");
    assertNull(m_smartField.getErrorStatus());
  }

  /**
   * This method deals with the async nature of the proposal chooser
   */
  void testMatch(String searchText, Long expectedValue, String expectedDisplayText, int expectedNumProposals,
      boolean expectedProposalChooserOpen, boolean expectValidationError) throws ProcessingException, InterruptedException {
    final IBlockingCondition bc = Jobs.getJobManager().createBlockingCondition("loadProposals", true);

    m_smartField.getLookupRowFetcher().addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IContentAssistFieldLookupRowFetcher.PROP_SEARCH_RESULT.equals(evt.getPropertyName())) {
          bc.setBlocking(false);
        }
      }
    });
    m_smartField.getUIFacade().openProposalChooserFromUI(searchText, false);
    bc.waitFor(); // must wait until results from client-job are available...
    if (expectedNumProposals > 0) {
      assertTrue(m_smartField.isProposalChooserRegistered());
      assertEquals(expectedNumProposals, getProposalTableRowCount());
    }
    assertEquals(searchText, m_smartField.getDisplayText());
    assertEquals(null, m_smartField.getValue());

    m_smartField.getUIFacade().acceptProposalFromUI(searchText, true);
    assertEquals(expectedProposalChooserOpen, m_smartField.isProposalChooserRegistered());

    if (expectValidationError) {
      assertFalse(m_smartField.getErrorStatus().isOK());
      assertEquals(searchText, m_smartField.getDisplayText());
      assertEquals(null, m_smartField.getValue());
      assertTrue(m_smartField.isEmptyCurrentLookupRow());
    }
    else {
      assertNull(m_smartField.getErrorStatus());
      assertEquals(expectedDisplayText, m_smartField.getDisplayText());
      assertEquals(expectedValue, m_smartField.getValue());
      assertFalse(m_smartField.isEmptyCurrentLookupRow());
    }
  }

  int getProposalTableRowCount() {
    return ((ITable) m_smartField.getProposalChooser().getModel()).getRowCount();
  }

  private static class SmartField extends AbstractSmartField<Long> {
    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_LookupCall.class;
    }
  }

  public static class P_LookupCall extends LookupCall<Long> {

    private static final long serialVersionUID = 1;

    @Override
    protected ILookupService<Long> createLookupService() {
      return new P_LookupService();
    }
  }

  public static class P_LookupService implements ILookupService<Long> {

    @Override
    public List<? extends ILookupRow<Long>> getDataByKey(ILookupCall<Long> call) throws ProcessingException {
      return LookupRows.getRowsByKey(call.getKey());
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByText(ILookupCall<Long> call) throws ProcessingException {
      return LookupRows.getRowsByText(call.getText());
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByAll(ILookupCall<Long> call) throws ProcessingException {
      return null;
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByRec(ILookupCall<Long> call) throws ProcessingException {
      return null;
    }

  }
}
