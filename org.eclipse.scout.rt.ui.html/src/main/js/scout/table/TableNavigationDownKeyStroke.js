scout.TableNavigationDownKeyStroke = function(table) {
  scout.TableNavigationDownKeyStroke.parent.call(this, table);
  this.which = [scout.keys.DOWN];
  this.renderingHints.text = '↓';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var viewport = this._viewportInfo(table);
    return viewport.selection ? viewport.$rowAfterSelection : viewport.$firstRow;
  }.bind(this);
};
scout.inherits(scout.TableNavigationDownKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationDownKeyStroke.prototype._acceptForNavigation = function(event) {
  return scout.TableNavigationDownKeyStroke.parent.prototype._acceptForNavigation.call(this, event);
};

scout.TableNavigationDownKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    $rows = table.$filteredRows(),
    $selection = table.$selectedRows(),
    lastActionRow = table.selectionHandler.lastActionRow,
    deselect = false,
    $newSelection;

  if ($selection.length > 0 || lastActionRow) {
    lastActionRow = lastActionRow || $selection.last().data('row');
    deselect = lastActionRow.$row.isSelected() && lastActionRow.$row.nextAll('.table-row:not(.invisible):first').isSelected();
    $newSelection = deselect ? lastActionRow.$row : lastActionRow.$row.nextAll('.table-row:not(.invisible):first');
    table.selectionHandler.lastActionRow = this._calculateLastActionRowDown(lastActionRow, deselect);
  } else {
    $newSelection = $rows.first();
    table.selectionHandler.lastActionRow = $newSelection.data('row');
  }

  this._applyRowSelection(table, $selection, $newSelection, event.shiftKey, deselect, true);
};
