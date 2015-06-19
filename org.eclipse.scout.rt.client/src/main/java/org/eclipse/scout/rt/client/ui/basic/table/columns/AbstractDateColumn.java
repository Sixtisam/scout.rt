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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IDateColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.DateFormatProvider;

/**
 * Column holding Date
 */
@ClassId("9185f9ed-3dc2-459b-b06b-f39c6c6fed2e")
public abstract class AbstractDateColumn extends AbstractColumn<Date> implements IDateColumn {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private String m_format;
  private boolean m_hasTime;
  private boolean m_hasDate;
  private long m_autoTimeMillis;

  public AbstractDateColumn() {
    super();
  }

  /*
   * Configuration
   */

  /**
   * Configures the format used to render the value. See the {@link DateFormat} class for more information about the
   * expected format.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Format of this column.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(140)
  protected String getConfiguredFormat() {
    return null;
  }

  /**
   * Configures whether the value represented by this column has a date. If {@link #getConfiguredFormat()} is set, this
   * configuration has no effect.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if the value represented by this column has a date, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(150)
  protected boolean getConfiguredHasDate() {
    return true;
  }

  /**
   * Configures whether the value represented by this column has a time. If {@link #getConfiguredFormat()} is set, this
   * configuration has no effect.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if the value represented by this column has a time, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(151)
  protected boolean getConfiguredHasTime() {
    return false;
  }

  /**
   * When a date without time is picked, this time value is used as hh/mm/ss.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(152)
  protected long getConfiguredAutoTimeMillis() {
    return 0;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setFormat(getConfiguredFormat());
    setHasDate(getConfiguredHasDate());
    setHasTime(getConfiguredHasTime());
    setAutoTimeMillis(getConfiguredAutoTimeMillis());
  }

  /*
   * Runtime
   */
  @Override
  public void setFormat(String s) {
    m_format = s;
    decorateCells();
  }

  @Override
  public String getFormat() {
    return m_format;
  }

  @Override
  public void setHasDate(boolean b) {
    m_hasDate = b;
    refreshValues();
  }

  @Override
  public void setHasTime(boolean b) {
    m_hasTime = b;
    refreshValues();
  }

  @Override
  public boolean isHasDate() {
    return m_hasDate;
  }

  @Override
  public boolean isHasTime() {
    return m_hasTime;
  }

  @Override
  public void setAutoTimeMillis(long l) {
    m_autoTimeMillis = l;
  }

  @Override
  public void setAutoTimeMillis(int hour, int minute, int second) {
    setAutoTimeMillis(((hour * 60L + minute) * 60L + second) * 1000L);
  }

  @Override
  public long getAutoTimeMillis() {
    return m_autoTimeMillis;
  }

  @Override
  protected Date parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    //legacy support
    if (rawValue instanceof Number) {
      rawValue = convertDoubleTimeToDate((Number) rawValue);
    }
    Date validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof Date) {
      validValue = (Date) rawValue;
    }
    else {
      throw new ProcessingException("invalid Date value in column '" + getClass().getSimpleName() + "': " + rawValue + " class=" + rawValue.getClass());
    }
    return validValue;
  }

  private Date convertDoubleTimeToDate(Number d) {
    if (d == null) {
      return null;
    }
    int m = (int) (((long) (d.doubleValue() * MILLIS_PER_DAY + 0.5)) % MILLIS_PER_DAY);
    Calendar c = Calendar.getInstance();
    c.clear();
    c.set(Calendar.MILLISECOND, m % 1000);
    m = m / 1000;
    c.set(Calendar.SECOND, m % 60);
    m = m / 60;
    c.set(Calendar.MINUTE, m % 60);
    m = m / 60;
    c.set(Calendar.HOUR_OF_DAY, m % 24);
    return c.getTime();
  }

  @Override
  protected IFormField prepareEditInternal(ITableRow row) throws ProcessingException {
    IDateField f = (IDateField) getDefaultEditor();
    mapEditorFieldProperties(f);
    return f;
  }

  @Override
  protected IValueField<Date> createDefaultEditor() {
    return new AbstractDateField() {
    };
  }

  protected void mapEditorFieldProperties(IDateField f) {
    super.mapEditorFieldProperties(f);
    f.setFormat(getFormat());
    f.setHasDate(isHasDate());
    f.setHasTime(isHasTime());
    f.setAutoTimeMillis(getAutoTimeMillis());
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    updateDisplayText(row, cell);
  }

  @Override
  protected String formatValueInternal(ITableRow row, Date value) {
    if (value != null) {
      return getDateFormat().format(value);
    }
    return "";
  }

  private DateFormat getDateFormat() {
    DateFormat df = null;
    if (getFormat() != null) {
      df = new SimpleDateFormat(getFormat(), NlsLocale.get());
    }
    else {
      if (isHasDate() && !isHasTime()) {
        df = BEANS.get(DateFormatProvider.class).getDateInstance(DateFormat.MEDIUM, NlsLocale.get());
      }
      else if (!isHasDate() && isHasTime()) {
        df = BEANS.get(DateFormatProvider.class).getTimeInstance(DateFormat.SHORT, NlsLocale.get());
      }
      else {
        df = BEANS.get(DateFormatProvider.class).getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, NlsLocale.get());
      }
      df.setLenient(true);
    }
    return df;
  }

  protected static class LocalDateColumnExtension<OWNER extends AbstractDateColumn> extends LocalColumnExtension<Date, OWNER> implements IDateColumnExtension<OWNER> {

    public LocalDateColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IDateColumnExtension<? extends AbstractDateColumn> createLocalExtension() {
    return new LocalDateColumnExtension<AbstractDateColumn>(this);
  }

}
