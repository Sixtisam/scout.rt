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
package org.eclipse.scout.rt.ui.swt.window;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swt.form.ISwtScoutForm;
import org.eclipse.swt.graphics.Image;

public interface ISwtScoutPart {
  String MARKER_SCOLLED_FORM = "SCROLLED_FORM";

  IForm getForm();

  ISwtScoutForm getUiForm();

  void closePart() throws ProcessingException;

  boolean isVisible();

  void activate();

  boolean isActive();

  void setStatusLineMessage(Image image, String message);
}
