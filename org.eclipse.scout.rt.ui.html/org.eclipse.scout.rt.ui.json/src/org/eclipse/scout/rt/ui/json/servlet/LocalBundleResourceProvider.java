package org.eclipse.scout.rt.ui.json.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * Serve files from the contributing bundles 'WebContent' folder as "/"
 */
public class LocalBundleResourceProvider extends AbstractService implements IServletResourceProvider {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LocalBundleResourceProvider.class);
  private static final String LAST_MODIFIED = "Last-Modified"; //$NON-NLS-1$
  private static final String IF_MODIFIED_SINCE = "If-Modified-Since"; //$NON-NLS-1$
  private static final String IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
  private static final String ETAG = "ETag"; //$NON-NLS-1$

  private Bundle m_bundle;
  private String m_bundleWebContentFolder;
  private IndexResolver m_indexResolver;

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    setBundle(registration.getReference().getBundle());
    setBundleWebContentFolder("WebContent");
    setIndexResolver(new IndexResolver());
  }

  @Override
  public boolean handle(AbstractJsonServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = req.getPathInfo();
    pathInfo = resolvePath(req, pathInfo);
    URL url = resolveBundleResource(pathInfo);
    if (url == null) {
      return false;
    }

    URLConnection connection = url.openConnection();
    long lastModified = connection.getLastModified();
    int contentLength = connection.getContentLength();
    int status = processCacheHeaders(req, resp, lastModified, contentLength);
    if (status == HttpServletResponse.SC_NOT_MODIFIED) {
      resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return true;
    }

    //Return file regularly if the client (browser) does not already have it or if the file has changed in the meantime
    byte[] content = fileContent(url);
    resp.setContentLength(content.length);

    //Prefer mime type mapping from container
    String contentType = servlet.getServletContext().getMimeType(pathInfo);
    if (contentType == null) {
      int lastDot = pathInfo.lastIndexOf('.');
      contentType = FileUtility.getContentTypeForExtension(lastDot >= 0 ? pathInfo.substring(lastDot + 1) : pathInfo);
    }
    if (contentType == null) {
      LOG.warn("Could not determine content type of file " + pathInfo);
    }
    else {
      resp.setContentType(contentType);
    }

    resp.getOutputStream().write(content);
    return true;
  }

  protected Bundle getBundle() {
    return m_bundle;
  }

  protected void setBundle(Bundle bundle) {
    m_bundle = bundle;
  }

  protected String getBundleWebContentFolder() {
    return m_bundleWebContentFolder;
  }

  protected void setBundleWebContentFolder(String folder) {
    m_bundleWebContentFolder = folder;
  }

  protected IndexResolver getIndexResolver() {
    return m_indexResolver;
  }

  protected void setIndexResolver(IndexResolver indexResolver) {
    m_indexResolver = indexResolver;
  }

  protected String resolvePath(HttpServletRequest req, String pathInfo) {
    if (pathInfo == null) {
      return null;
    }
    if (pathInfo.equals("/")) {
      pathInfo = getIndexResolver().resolve(req);
    }
    return pathInfo;
  }

  /**
   * resolve a web path /res/scout.css to a bundle resource WebContent/res/scout.css
   */
  protected URL resolveBundleResource(String pathInfo) {
    if (pathInfo == null) {
      return null;
    }
    return getBundle().getEntry(getBundleWebContentFolder() + pathInfo);
  }

  /**
   * Checks whether the file needs to be returned or not, depending on the request headers and file modification state.
   * Also writes cache headers (last modified and etag) if the file needs to be returned.
   * 
   * @return {@link HttpServletResponse#SC_NOT_MODIFIED} if the file hasn't changed in the meantime or
   *         {@link HttpServletResponse#SC_ACCEPTED} if the content of the file needs to be returned.
   */
  protected int processCacheHeaders(final HttpServletRequest req, final HttpServletResponse resp, long lastModified, int contentLength) {
    String etag = null;
    if (lastModified != -1 && contentLength != -1) {
      etag = "W/\"" + contentLength + "-" + lastModified + "\""; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    // Check for cache revalidation.
    // We should prefer ETag validation as the guarantees are stronger and all
    // HTTP 1.1 clients should be using it
    String ifNoneMatch = req.getHeader(IF_NONE_MATCH);
    if (ifNoneMatch != null && etag != null && ifNoneMatch.indexOf(etag) != -1) {
      return HttpServletResponse.SC_NOT_MODIFIED;
    }
    else {
      long ifModifiedSince = req.getDateHeader(IF_MODIFIED_SINCE);
      // for purposes of comparison we add 999 to ifModifiedSince since the
      // fidelity
      // of the IMS header generally doesn't include milli-seconds
      if (ifModifiedSince > -1 && lastModified > 0 && lastModified <= (ifModifiedSince + 999)) {
        return HttpServletResponse.SC_NOT_MODIFIED;
      }
    }

    // File needs to be returned regularly, write cache headers
    if (lastModified > 0) {
      resp.setDateHeader(LAST_MODIFIED, lastModified);
    }
    if (etag != null) {
      resp.setHeader(ETAG, etag);
    }

    return HttpServletResponse.SC_ACCEPTED;
  }

  protected byte[] fileContent(URL url) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    InputStream is = url.openStream();
    try {
      byte[] buffer = new byte[8192];
      int bytesRead = is.read(buffer);
      while (bytesRead != -1) {
        os.write(buffer, 0, bytesRead);
        bytesRead = is.read(buffer);
      }
    }
    finally {
      is.close();
    }
    return os.toByteArray();
  }

}
