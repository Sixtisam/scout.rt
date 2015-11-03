/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ColumnUserFilter = function() {
  scout.ColumnUserFilter.parent.call(this);
  this.filterType = scout.ColumnUserFilter.Type;

  /**
   * array of (normalized) key, text composite
   */
  this.availableValues = [];

  /**
   * array of (normalized) keys
   */
  this.selectedValues = [];
};
scout.inherits(scout.ColumnUserFilter, scout.TableUserFilter);

scout.ColumnUserFilter.Type = 'column';

scout.ColumnUserFilter.prototype.calculate = function() {
  var containsSelectedValue, reorderAxis,
    group = -1;

  if (this.column.type === 'date') {
    if (this.column.hasDate) {
      // Default grouping for date columns is year
      group = scout.TableCube.DateGroup.YEAR;
    } else {
      // No grouping for time columns
      group = scout.TableCube.DateGroup.NONE;
    }
  }
  this.matrix = new scout.TableCube(this.table, this.session),
    this.xAxis = this.matrix.addAxis(this.column, group);
  this.matrix.calculate();

  this.selectedValues.forEach(function(selectedValue) {
    containsSelectedValue = false;
    if (this._useTextInsteadOfNormValue(selectedValue)) {
      // selected value was not normalized -> normalize
      selectedValue = this.xAxis.norm(selectedValue);
    }
    this.xAxis.some(function(key) {
      if (key === selectedValue) {
        containsSelectedValue = true;
        return true;
      }
    }, this);

    if (!containsSelectedValue) {
      this.xAxis.push(selectedValue);
      reorderAxis = true;
    }
  }, this);

  if (reorderAxis) {
    this.xAxis.reorder();
  }

  this.availableValues = [];
  this.xAxis.forEach(function(key) {
    var text = this.xAxis.format(key);
    if (this._useTextInsteadOfNormValue(key)) {
      key = text;
    }
    this.availableValues.push({
      key: key,
      text: text
    });
  }, this);
};

/**
 * In case of text columns, the normalized key generated by the matrix is not deterministic, it depends on the table data
 * -> use the text.
 * In the other cases it is possible to use the normalized key which has the advantage that it is locale independent
 */
scout.ColumnUserFilter.prototype._useTextInsteadOfNormValue = function(value) {
  if (value === null) {
    // null is valid, if for text columns. We do not want to store -empty-
    return false;
  }
  return this.column.type === 'text';
};

scout.ColumnUserFilter.prototype.createAddFilterEventData = function() {
  var data = scout.ColumnUserFilter.parent.prototype.createAddFilterEventData.call(this);
  return $.extend(data, {
    columnId: this.column.id,
    selectedValues: this.selectedValues
  });
};

scout.ColumnUserFilter.prototype.createRemoveFilterEventData = function() {
  var data = scout.ColumnUserFilter.parent.prototype.createRemoveFilterEventData.call(this);
  return $.extend(data, {
    columnId: this.column.id
  });
};

scout.ColumnUserFilter.prototype.createLabel = function() {
  return this.column.text || '';
};

scout.ColumnUserFilter.prototype.createKey = function() {
  return this.column.id;
};

scout.ColumnUserFilter.prototype.accept = function($row) {
  if (!this.xAxis) {
    // Lazy calculation. It is not possible on init, because the table is not rendered yet.
    this.calculate();
  }
  var row = $row.data('row'),
    key = this.column.cellValueForGrouping(row),
    normKey = this.xAxis.norm(key);

  if (this._useTextInsteadOfNormValue(normKey)) {
    normKey = this.xAxis.format(normKey);
  }
  return (this.selectedValues.indexOf(normKey) > -1);
};
