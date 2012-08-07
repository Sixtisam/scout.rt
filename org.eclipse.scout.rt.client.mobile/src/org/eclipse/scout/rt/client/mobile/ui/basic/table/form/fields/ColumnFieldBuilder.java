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
package org.eclipse.scout.rt.client.mobile.ui.basic.table.form.fields;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDoubleColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ILongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 * @since 3.9.0
 */
public class ColumnFieldBuilder {

  public List<IValueField> build(IColumn<?>[] columns) {
    List<IValueField> fieldList = new LinkedList<IValueField>();
    if (columns == null) {
      return fieldList;
    }

    for (IColumn column : columns) {
      if (column instanceof IStringColumn) {
        IValueField field = new StringColumnField((IStringColumn) column);
        fieldList.add(field);
      }
      else if (column instanceof ISmartColumn) {
        IValueField field = new SmartColumnField((ISmartColumn) column);
        fieldList.add(field);
      }
      else if (column instanceof IDoubleColumn) {
        IValueField field = new DoubleColumnField((IDoubleColumn) column);
        fieldList.add(field);
      }
      else if (column instanceof IDateColumn) {
        IValueField field = new DateColumnField((IDateColumn) column);
        fieldList.add(field);
      }
      else if (column instanceof IBooleanColumn) {
        IValueField field = new BooleanColumnField((IBooleanColumn) column);
        fieldList.add(field);
      }
      else if (column instanceof ILongColumn) {
        IValueField field = new LongColumnField((ILongColumn) column);
        fieldList.add(field);
      }
      else if (column instanceof IIntegerColumn) {
        IValueField field = new IntegerColumnField((IIntegerColumn) column);
        fieldList.add(field);
      }
      else if (column instanceof IBigDecimalColumn) {
        IValueField field = new BigDecimalColumnField((IBigDecimalColumn) column);
        fieldList.add(field);
      }
    }

    return fieldList;
  }
}
