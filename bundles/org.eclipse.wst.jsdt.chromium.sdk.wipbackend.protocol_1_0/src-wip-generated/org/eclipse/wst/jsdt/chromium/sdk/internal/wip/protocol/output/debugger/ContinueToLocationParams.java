// Generated source.
// Generator: org.eclipse.wst.jsdt.chromium.sdk.internal.wip.tools.protocolgenerator.Generator
// Origin: http://svn.webkit.org/repository/webkit/trunk/Source/WebCore/inspector/Inspector.json@84351

package org.eclipse.wst.jsdt.chromium.sdk.internal.wip.protocol.output.debugger;

/**
Continues execution until specific location is reached.
 */
public class ContinueToLocationParams extends org.eclipse.wst.jsdt.chromium.sdk.internal.wip.protocol.output.WipParams {
  /**
   @param location Location to continue to.
   */
  public ContinueToLocationParams(org.eclipse.wst.jsdt.chromium.sdk.internal.wip.protocol.output.debugger.LocationParam location) {
    this.put("location", location);
  }

  public static final String METHOD_NAME = org.eclipse.wst.jsdt.chromium.sdk.internal.wip.protocol.BasicConstants.Domain.DEBUGGER + ".continueToLocation";

  @Override protected String getRequestName() {
    return METHOD_NAME;
  }

}
