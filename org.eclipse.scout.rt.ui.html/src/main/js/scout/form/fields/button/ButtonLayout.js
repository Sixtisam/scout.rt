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
/**
 * Button Layout, for fields with a button.
 */
scout.ButtonLayout = function(button) {
  scout.ButtonLayout.parent.call(this, button);
  this.button = button;
};
scout.inherits(scout.ButtonLayout, scout.FormFieldLayout);

scout.ButtonLayout.prototype.naturalSize = function() {
  var fieldSize = scout.graphics.prefSize(this.button.$field, true),
    iconSize = new scout.Dimension(0, 0);

  if (this.button.iconId) {
    // FIXME AWE: (button) read size from image dynamically
    // maybe add event-handler when IMG is loaded and invalidate layout
    iconSize = new scout.Dimension(16, 16);
    if (this.button.label) {
      iconSize.width += 8; // add gap between text and icon
    }
  }

  var prefSize = fieldSize;
  prefSize.width += iconSize.width;
  prefSize.height = Math.max(prefSize.height, iconSize.height);

  // Workaround for IE/Safari: The element's actual width is a fractional value in these browsers
  // (e.g. 60.4), but jQuery always rounds them to the nearest integer (e.g. 60). This is not
  // only the case when reading the height, but also when setting it! It is a bug in jQuery
  // (https://github.com/jquery/jquery/issues/1724), but it won't be fixed until jQuery 3.0.
  // To ensure that button texts are always fully visible, we add a pixel. This prevents ellipsis
  // ("...") being shown even when the text would fit.
  // TODO BSH/AWE Check if there a better solution
  prefSize.width += 1;
  // </Workaround>

  return prefSize;
};
