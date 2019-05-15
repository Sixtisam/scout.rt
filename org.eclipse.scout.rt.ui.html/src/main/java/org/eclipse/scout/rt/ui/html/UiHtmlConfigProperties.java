package org.eclipse.scout.rt.ui.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.session.ClientSessionStopHelper;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.res.PrebuildFiles;
import org.eclipse.scout.rt.ui.html.res.loader.HtmlDocumentParser;
import org.eclipse.scout.rt.ui.html.res.loader.LocalesLoader;
import org.eclipse.scout.rt.ui.html.res.loader.TextsLoader;
import org.eclipse.scout.rt.ui.html.scriptprocessor.ScriptProcessor;

/**
 * This class provides all properties configured in the config.properties file that affect the HTML UI module.
 */
public final class UiHtmlConfigProperties {

  private UiHtmlConfigProperties() {
  }

  /**
   * Returns the name of the UI theme which is activated when the application starts. When the default theme is active
   * this property will return null.
   */
  public static class UiThemeProperty extends AbstractStringConfigProperty {

    public static final String DEFAULT_THEME = "default";

    @Override
    public String getKey() {
      return "scout.ui.theme";
    }

    @Override
    protected String getDefaultValue() {
      return DEFAULT_THEME;
    }
  }

  /**
   * When this property returns true, file pre-building is performed when the UI application server starts up. This
   * means the application start takes more time, but in return the first user request takes less time. 'File' in this
   * context means web-resources like HTML, CSS and JS - these files are typically processed by Scout's ScriptProcessor
   * and HtmlDocumentParser.
   *
   * @see PrebuildFiles
   * @see HtmlDocumentParser
   * @see ScriptProcessor
   */
  public static class UiPrebuildProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.ui.prebuild";
    }

    @Override
    protected Boolean getDefaultValue() {
      return !Platform.get().inDevelopmentMode();
    }
  }

  /**
   * Contains a comma separated list of files in <code>/WebContent/res</code> that should be pre-built when (UI)
   * application server starts up. Since it takes a while to build files, especially JS and CSS (LESS) files, we want to
   * do this when the server starts. Otherwise its always the first user who must wait a long time until all files are
   * built.
   * <p>
   * Since CSS and JS files are always referenced by a HTML file, we simply specify the main HTML files in this
   * property.
   *
   * @return unmodifiable list
   */
  public static class UiPrebuildFilesProperty extends AbstractConfigProperty<List<String>, String> {

    @Override
    public String getKey() {
      return "scout.ui.prebuild.files";
    }

    @Override
    protected List<String> parse(String value) {
      String[] tokens = StringUtility.tokenize(value, ',');
      // Prevent accidental modification by returning an unmodifiable list because property is cached and always returns the same instance
      return Arrays.asList(tokens);
    }

    @Override
    protected List<String> getDefaultValue() {
      return Collections.unmodifiableList(new ArrayList<String>(0));
    }
  }

  /**
   * Contains a comma separated list of supported locales (e.g. en,en-US,de-CH). This is only relevant if locales.json
   * and texts.json should be sent to the client, which is not the case for remote apps. So this property is only used
   * for JS only apps.
   *
   * @see {@link LocalesLoader}, {@link TextsLoader}
   * @return unmodifiable list
   */
  public static class UiLocalesProperty extends AbstractConfigProperty<List<String>, String> {

    @Override
    public String getKey() {
      return "scout.ui.locales";
    }

    @Override
    protected List<String> parse(String value) {
      String[] tokens = StringUtility.tokenize(value, ',');
      // Prevent accidental modification by returning an unmodifiable list because property is cached and always returns the same instance
      return Arrays.asList(tokens);
    }

    @Override
    protected List<String> getDefaultValue() {
      return Collections.unmodifiableList(new ArrayList<String>(0));
    }

  }

  /**
   * After expiration of this idle time in seconds without any user activity the user is logged out automatically. The
   * default is 4 hours (14400 seconds).
   */
  public static class MaxUserIdleTimeProperty extends AbstractPositiveLongConfigProperty {

    @Override
    protected Long getDefaultValue() {
      return Long.valueOf(TimeUnit.HOURS.toSeconds(4));
    }

    @Override
    public String getKey() {
      return "scout.max.user.idle.time";
    }
  }

  /**
   * The polling request which waits for a background job to complete stays open until a background job has completed or
   * this interval elapsed. The unit is seconds.
   * <p>
   * The minimum value for this property is 3 seconds, the max value is the max user idle time (see
   * {@link MaxUserIdleTimeProperty}).
   */
  public static class BackgroundPollingIntervalProperty extends AbstractPositiveLongConfigProperty {

    @Override
    protected Long getDefaultValue() {
      return Long.valueOf(TimeUnit.SECONDS.toSeconds(60));
    }

    @Override
    public String getKey() {
      return "scout.background.polling.interval";
    }
  }

  /**
   * The maximal timeout in seconds to wait for model jobs to complete during a UI request. After that timeout the model
   * jobs will be aborted so that the request may return to the client.
   */
  public static class UiModelJobsAwaitTimeoutProperty extends AbstractPositiveLongConfigProperty {

    @Override
    protected Long getDefaultValue() {
      return Long.valueOf(TimeUnit.HOURS.toSeconds(1));
    }

    @Override
    public String getKey() {
      return "scout.ui.model.jobs.await.timeout";
    }
  }

  /**
   * Number of seconds before the housekeeping job starts after a UI session has been unregistered from the store.
   */
  public static class SessionStoreHousekeepingDelayProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.sessionstore.housekeepingDelay";
    }

    @Override
    protected Integer getDefaultValue() {
      return 20;
    }
  }

  /**
   * Maximum time in seconds to wait for a client session to be stopped by the housekeeping job.<br/>
   * The value should be smaller than the session timeout (typically defined in the web.xml) and greater than
   * {@link org.eclipse.scout.rt.client.ClientConfigProperties.JobCompletionDelayOnSessionShutdown}.
   *
   * @deprecated since 6.1 not used anymore, see {@link ClientSessionStopHelper}
   */
  @Deprecated
  public static class SessionStoreHousekeepingMaxWaitShutdownProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.sessionstore.housekeepingMaxWaitForShutdown";
    }

    @Override
    protected Integer getDefaultValue() {
      return 60; // 1 minute
    }
  }

  /**
   * Maximum time in seconds to wait for the write lock when the session store is unbounded from the HTTP session. This
   * value should not be too large because waiting on the lock might suspend background processes of the application
   * server.
   *
   * @deprecated since 6.1 not used anymore, see {@link ClientSessionStopHelper}
   */
  @Deprecated
  public static class SessionStoreMaxWaitWriteLockProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.sessionStore.valueUnboundMaxWaitForWriteLock";
    }

    @Override
    protected Integer getDefaultValue() {
      return 5;
    }
  }

  /**
   * Maximum time in second to wait for all client sessions to be stopped after the HTTP session has become invalid.
   * After this amount of time, a "leak detection" test is performed. You are advised to change this value only if your
   * sessions need an unusual long time to shutdown.
   *
   * @deprecated since 6.1 not used anymore, see {@link ClientSessionStopHelper}
   */
  @Deprecated
  public static class SessionStoreMaxWaitAllShutdownProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.sessionStore.maxWaitForAllShutdown";
    }

    @Override
    protected Integer getDefaultValue() {
      return 60; // 1 minute
    }
  }

  /**
   * Specifies if scriptfiles should be rebuild on modification or with every request. False for rebuild on
   * modifications. The default value is true.
   */
  public static class ScriptfileBuildProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.dev.scriptfile.rebuild";
    }

    @Override
    protected Boolean parse(String value) {
      if (!Platform.get().inDevelopmentMode()) {
        return true;
      }
      // only works in development mode
      return super.parse(value);
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.TRUE;
    }
  }

  /**
   * Specifies a key to store the keys (HttpCacheKey) scripts in development cache. The keys of the last application
   * start in dev mode will be stored in the user.home/.eclipse/org.eclipse.scout.dev/scriptfileCacheDev_{key}.obj. For
   * the next run in dev mode the keys stored under this key are preloaded.
   */
  public static class ScriptfileBuilderDevCacheKey extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.dev.scriptfile.persist.key";
    }
  }
}
