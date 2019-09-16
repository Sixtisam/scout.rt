/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.TableTextUserFilter = function() {
  scout.TableTextUserFilter.parent.call(this);
  this.filterType = scout.TableTextUserFilter.TYPE;
};
scout.inherits(scout.TableTextUserFilter, scout.TableUserFilter);

scout.TableTextUserFilter.TYPE = 'text';

/**
 * @override TableUserFilter.js
 */
scout.TableTextUserFilter.prototype.createFilterAddedEventData = function() {
  var data = scout.TableTextUserFilter.parent.prototype.createFilterAddedEventData.call(this);
  data.text = this.text;
  return data;
};

scout.TableTextUserFilter.prototype.createLabel = function() {
  return this.text;
};

scout.TableTextUserFilter.prototype.accept = function(row) {
  var rowText = this.table.visibleColumns().reduce(function(acc, column) {
    return acc + column.cellTextForTextFilter(row) + ' ';
  }, '');
  if (this.text !== this._cachedText) {
    this._cachedText = this.text;
    this._cachedTextLowerCase = this.text.toLowerCase();
  }
  rowText = rowText.trim().toLowerCase();
  return rowText.indexOf(this._cachedTextLowerCase) > -1;
};
