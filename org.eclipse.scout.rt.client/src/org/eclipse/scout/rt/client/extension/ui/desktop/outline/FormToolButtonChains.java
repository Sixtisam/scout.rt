package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class FormToolButtonChains {

  private FormToolButtonChains() {
  }

  protected abstract static class AbstractFormToolButtonChain<FORM extends IForm> extends AbstractExtensionChain<IFormToolButtonExtension<? extends IForm, ? extends AbstractFormToolButton<? extends IForm>>> {

    public AbstractFormToolButtonChain(List<? extends IFormToolButtonExtension<? extends IForm, ? extends AbstractFormToolButton<? extends IForm>>> extensions) {
      super(extensions);
    }
  }

  public static class FormToolButtonInitFormChain<FORM extends IForm> extends AbstractFormToolButtonChain<FORM> {

    public FormToolButtonInitFormChain(List<? extends IFormToolButtonExtension<FORM, ? extends AbstractFormToolButton<? extends IForm>>> extensions) {
      super(extensions);
    }

    public void execInitForm() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IFormToolButtonExtension<? extends IForm, ? extends AbstractFormToolButton<? extends IForm>> next) throws ProcessingException {
          next.execInitForm(FormToolButtonInitFormChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
