/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Tooltip = function() {
  scout.Tooltip.parent.call(this);

  /**
   * Either a String or a function which returns a String
   */
  this.text = null;

  this.arrowPosition = 25;
  this.arrowPositionUnit = '%';
  this.windowPaddingX = 10;
  this.windowPaddingY = 5;
  this.origin;

  /**
   * When the origin point is calculated using $element.offset(),
   * the result is absolute to the window. When positioning the tooltip, the $parent's offset must
   * be subtracted. When the given origin is already relative to the parent, set this option to
   * "true" to disable this additional calculation.
   */
  this.originRelativeToParent = false;
  this.$anchor;
  this.autoRemove = true;
  this.cssClass;
  this.tooltipPosition = 'top';
  this.scrollType = 'position';
  this.htmlEnabled = false;
  this.$content;
  this.menus = [];
  this._addWidgetProperties(['menus']);

  this._openLater = false;
};
scout.inherits(scout.Tooltip, scout.Widget);

scout.Tooltip.prototype.render = function($parent) {
  // Use entry point by default
  var $tooltipParent = $parent || this.entryPoint();
  // when the parent is detached it is not possible to render the popup -> do it later
  if (!$tooltipParent || !$tooltipParent.length || !$tooltipParent.isAttached()) {
    this._openLater = true;
    this.$parent = $tooltipParent;
    return;
  }
  scout.Tooltip.parent.prototype.render.call(this, $tooltipParent);
};

scout.Tooltip.prototype._render = function() {
  this.$container = this.$parent
    .appendDiv('tooltip')
    .data('tooltip', this);

  if (this.cssClass) {
    this.$container.addClass(this.cssClass);
  }

  this.$arrow = this.$container.appendDiv('tooltip-arrow');
  this.$content = this.$container.appendDiv('tooltip-content');
  this._renderText();
  this._renderSeverity();
  this._renderMenus();

  if (this.autoRemove) {
    // Every user action will remove the tooltip
    this._mouseDownHandler = this._onDocumentMouseDown.bind(this);
    // The listener needs to be executed in the capturing phase -> Allows for having context menus inside the tooltip, otherwise click on context menu header would close the tooltip
    this.$container.document(true).addEventListener('mousedown', this._mouseDownHandler, true); // true=the event handler is executed in the capturing phase

    this._keydownHandler = this._onDocumentKeyDown.bind(this);
    this.$container.document()
      .on('keydown', this._keydownHandler);
  }

  if (this.$anchor && this.scrollType) {
    this._anchorScrollHandler = this._onAnchorScroll.bind(this);
    scout.scrollbars.onScroll(this.$anchor, this._anchorScrollHandler);
  }

  // If the tooltip is rendered inside a (popup) dialog, get a reference to the dialog.
  this.dialog = null;
  var parent = this.parent;
  while (parent) {
    if (parent instanceof scout.Form && parent.isDialog()) {
      this.dialog = parent;
      break;
    }
    parent = parent.parent;
  }

  // If inside a dialog, attach a listener to reposition the tooltip when the dialog is moved
  if (this.dialog) {
    this._moveHandler = this.position.bind(this);
    this.dialog.on('move', this._moveHandler);
  }
};

scout.Tooltip.prototype._postRender = function() {
  scout.Tooltip.parent.prototype._postRender.call(this);
  this.position();
};

scout.Tooltip.prototype._remove = function() {
  if (this._mouseDownHandler) {
    this.$container.document(true).removeEventListener('mousedown', this._mouseDownHandler, true);
    this._mouseDownHandler = null;
  }
  if (this._keydownHandler) {
    this.$container.document().off('keydown', this._keydownHandler);
    this._keydownHandler = null;
  }
  if (this._anchorScrollHandler) {
    scout.scrollbars.offScroll(this._anchorScrollHandler);
    this._anchorScrollHandler = null;
  }
  if (this._moveHandler) {
    if (this.dialog) {
      this.dialog.off('move', this._moveHandler);
    }
    this._moveHandler = null;
  }
  this.dialog = null;
  this.$menus = null;
  scout.Tooltip.parent.prototype._remove.call(this);
};

scout.Tooltip.prototype._onAttach = function() {
  scout.Tooltip.parent.prototype._onAttach.call(this);
  if (this._openLater && !this.rendered) {
    this._openLater = false;
    this.render();
  }
};

scout.Tooltip.prototype._renderOnDetach = function() {
  this._openLater = true;
  this.remove();
  scout.FieldStatus.parent.prototype._onDetach.call(this);
};

scout.Tooltip.prototype._isRemovalPrevented = function() {
  // Never prevent. Default returns true if removal is pending by an animation, but tooltips should be closed before the animation starts
  return false;
};

scout.Tooltip.prototype.setText = function(text) {
  this.setProperty('text', text);
};

scout.Tooltip.prototype.setSeverity = function(severity) {
  this.setProperty('severity', severity);
};

scout.Tooltip.prototype._renderText = function() {
  var text = this.text || '';
  if (this.htmlEnabled) {
    this.$content.html(text);
  } else {
    // use nl2br to allow tooltips with line breaks
    this.$content.html(scout.strings.nl2br(text));
  }
  this.$content.setVisible(!!text);
  this.$container.toggleClass('no-text', !text);
  if (!this.rendering) {
    this.position();
  }
};

scout.Tooltip.prototype._renderSeverity = function() {
  this.$container.removeClass(scout.Status.SEVERITY_CSS_CLASSES);
  this.$container.addClass(scout.Status.cssClassForSeverity(this.severity));
};

scout.Tooltip.prototype.setMenus = function(menus) {
  menus = scout.arrays.ensure(menus);
  this.setProperty('menus', menus);
};

scout.Tooltip.prototype._renderMenus = function() {
  var maxIconWidth = 0,
    menus = this.menus;

  if (menus.length > 0 && !this.$menus) {
    this.$menus = this.$container.appendDiv('tooltip-menus');
  } else if (menus.length === 0 && this.$menus) {
    this.$menus.remove();
    this.$menus = null;
  }

  // Render menus
  menus.forEach(function(menu) {
    var iconWidth = 0;
    menu.render(this.$menus);
    if (menu.iconId) {
      iconWidth = menu.get$Icon().outerWidth(true);
      maxIconWidth = Math.max(iconWidth, maxIconWidth);
    }
  }, this);

  // Align menus if there is one with an icon
  if (maxIconWidth > 0) {
    menus.forEach(function(menu) {
      if (!menu.iconId) {
        menu.$text.cssPaddingLeft(maxIconWidth);
      } else {
        menu.$text.cssPaddingLeft(maxIconWidth - menu.get$Icon().outerWidth(true));
      }
    }, this);
  }

  if (!this.rendering) {
    this.position();
  }
};

scout.Tooltip.prototype.position = function() {
  var top, left, arrowSize, overlapX, overlapY, x, y, origin,
    tooltipWidth, tooltipHeight, arrowDivWidth, arrowPosition, inView;

  if (this.origin) {
    origin = this.origin;
    x = origin.x;
  } else {
    origin = scout.graphics.offsetBounds(this.$anchor);
    x = origin.x + origin.width / 2;
  }
  y = origin.y;

  if (this.$anchor) {
    // Sticky tooltip must only be visible if the location where the tooltip points is in view (prevents that the tooltip points at an invisible anchor)
    inView = scout.scrollbars.isLocationInView(origin, this.$anchor.scrollParent());
    this.$container.setVisible(inView);
  }

  // this.$parent might not be at (0,0) of the document
  if (!this.originRelativeToParent) {
    var parentOffset = this.$parent.offset();
    x -= parentOffset.left;
    y -= parentOffset.top;
  }

  arrowDivWidth = this.$arrow.outerWidth();
  // Arrow is a div rotated by 45 deg -> visible height is half the div
  arrowSize = scout.Tooltip.computeHypotenuse(arrowDivWidth) / 2;

  tooltipHeight = this.$container.outerHeight();
  tooltipWidth = this.$container.outerWidth();

  // Compute actual arrow position if position is provided in percentage
  arrowPosition = this.arrowPosition;
  if (this.arrowPositionUnit === '%') {
    arrowPosition = tooltipWidth * this.arrowPosition / 100;
  }

  top = y - tooltipHeight - arrowSize;
  left = x - arrowPosition;
  overlapX = left + tooltipWidth + this.windowPaddingX - this.$parent.width();
  overlapY = top - this.windowPaddingY;

  // Move tooltip to the left until it gets fully visible
  if (overlapX > 0) {
    left -= overlapX;
    arrowPosition = x - left;
  }

  // Move tooltip to the bottom, arrow on top
  this.$arrow.removeClass('arrow-top arrow-bottom');
  if (this.tooltipPosition === 'bottom' || overlapY < 0) {
    this.$arrow.addClass('arrow-top');
    top = y + origin.height + arrowSize;
  } else {
    this.$arrow.addClass('arrow-bottom');
  }

  // Make sure arrow is never positioned outside of the tooltip
  arrowPosition = Math.min(arrowPosition, this.$container.outerWidth() - arrowSize);
  arrowPosition = Math.max(arrowPosition, arrowSize);
  this.$arrow.cssLeft(arrowPosition);
  this.$container
    .cssLeft(left)
    .cssTop(top);

  // If there are menu popups make sure they are positioned correctly
  this.menus.forEach(function(menu) {
    if (menu.popup) {
      menu.popup.position();
    }
  }, this);
};

scout.Tooltip.prototype._onAnchorScroll = function(event) {
  if (!this.rendered) {
    // Scroll events may be fired delayed, even if scroll listener are already removed.
    return;
  }
  if (this.scrollType === 'position') {
    this.position();
  } else if (this.scrollType === 'remove') {
    this.destroy();
  }
};

scout.Tooltip.prototype._onDocumentMouseDown = function(event) {
  if (!this.rendered) {
    return false;
  }
  if (this._isMouseDownOutside(event)) {
    this._onMouseDownOutside(event);
  }
};

scout.Tooltip.prototype._isMouseDownOutside = function(event) {
  var $target = $(event.target),
    targetWidget = scout.widget($target);

  // Only remove the tooltip if the click is outside of the container or the $anchor (= status icon)
  // Also ignore clicks if the tooltip is covert by a glasspane
  return !this.isOrHas(targetWidget) &&
    (this.$anchor && !this.$anchor.isOrHas($target[0])) &&
    !this.session.focusManager.isElementCovertByGlassPane(this.$container[0]);
};

/**
 * Method invoked once a mouse down event occurs outside the tooltip.
 */
scout.Tooltip.prototype._onMouseDownOutside = function() {
  this.destroy();
};

scout.Tooltip.prototype._onDocumentKeyDown = function(event) {
  if (scout.isOneOf(event.which,
      scout.keys.CTRL, scout.keys.SHIFT, scout.keys.ALT,
      scout.keys.NUM_LOCK, scout.keys.CAPS_LOCK, scout.keys.SCROLL_LOCK,
      scout.keys.WIN_LEFT, scout.keys.WIN_RIGHT, scout.keys.SELECT,
      scout.keys.PAUSE, scout.keys.PRINT_SCREEN)) {
    return;
  }

  this.destroy();
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.Tooltip
 */
scout.Tooltip.computeHypotenuse = function(x) {
  return Math.sqrt(Math.pow(x, 2) + Math.pow(x, 2));
};
