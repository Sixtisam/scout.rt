/**
 * Common base class for ValueFields having an HTML input field.
 */
scout.BasicField = function() {
  scout.BasicField.parent.call(this);
  this._keyUpListener;
};
scout.inherits(scout.BasicField, scout.ValueField);

scout.BasicField.prototype._renderProperties = function() {
  scout.BasicField.parent.prototype._renderProperties.call(this);
  this._renderUpdateDisplayTextOnModify();
};

/**
 * @override FormField.js
 */
scout.BasicField.prototype._renderEnabled = function() {
  scout.BasicField.parent.prototype._renderEnabled.call(this);
  this._renderDisabledOverlay();
};

/**
 * "Update display-text on modify" does not really belong to ValueField, but is available here
 * as a convenience for all subclasses that want to support it.
 */
scout.BasicField.prototype._renderUpdateDisplayTextOnModify = function() {
  if (this.updateDisplayTextOnModify) {
    this._keyUpListener = this._onFieldKeyUp.bind(this);
    this.$field.on('keyup', this._keyUpListener);
  } else {
    this.$field.off('keyup', this._keyUpListener);
  }
};

scout.BasicField.prototype._onFieldKeyUp = function() {
  this.acceptInput(true);
};

/**
 * @override ValueField.js
 */
scout.BasicField.prototype._checkDisplayTextChanged = function(displayText, whileTyping) {
  var displayTextChanged = scout.BasicField.parent.prototype._checkDisplayTextChanged.call(this, displayText, whileTyping);

  // OR if updateDisplayTextOnModify is true
  // 2. check is necessary to make sure the value and not only the display text gets written to the model (IBasicFieldUIFacade.parseAndSetValueFromUI vs setDisplayTextFromUI)
  if (displayTextChanged || (this.updateDisplayTextOnModify || this._displayTextChangedWhileTyping) && !whileTyping) {
    // In 'updateDisplayTextOnModify' mode, each change of text is sent to the server with whileTyping=true.
    // On field blur, the text is sent again with whileTyping=false. The following logic prevents sending
    // to many events to the server. When whileTyping is false, the text has only to be send to the server
    // when there have been any whileTyping=true events. When the field looses the focus without any
    // changes, no request should be sent.
    if (this.updateDisplayTextOnModify) {
      if (whileTyping) {
        // Remember that we sent some events to the server with "whileTyping=true".
        this._displayTextChangedWhileTyping = true;
      }
      else {
        if (!this._displayTextChangedWhileTyping) {
          // If there were no "whileTyping=true" events, don't send anything to the server.
          return false;
        }
        this._displayTextChangedWhileTyping = false; // Reset
      }
    }
    return true;
  }
  return false;
};

/**
 * Add or remove an overlay DIV for browsers that don't support copy from disabled text-fields.
 * The overlay provides a custom 'copy' menu which opens the ClipboardForm.
 */
scout.BasicField.prototype._renderDisabledOverlay = function() {
  if (scout.device.supportsCopyFromDisabledInputFields()) {
    return;
  }

  if (this.enabled) {
    if (this._$disabledOverlay) {
      this._$disabledOverlay.remove();
      this._$disabledOverlay = null;
    }
  } else if (!this._$disabledOverlay) {
    this._$disabledOverlay = $.makeDiv('disabled-overlay')
      .on('contextmenu', this._createCopyContextMenu.bind(this))
      .appendTo(this.$container);
  }
};

scout.BasicField.prototype._createCopyContextMenu = function(event) {
  if (!this.visible) {
    return;
  }

  var fieldId = this.id;
  var menu = scout.localObjects.createObject(this.session, {
    objectType: 'Menu',
    text: this.session.text('ui.copy')
  });
  menu.remoteHandler = function(target, type) {
    if ('doAction' === type) {
      this.session.send(fieldId, 'exportToClipboard');
    }
  };

  var popup = new scout.ContextMenuPopup(this.session, {
    menuItems: [menu],
    cloneMenuItems: false,
    location: {
      x: event.pageX,
      y: event.pageY
    },
    $anchor: this._$disabledOverlay
  });
  popup.render();
};
