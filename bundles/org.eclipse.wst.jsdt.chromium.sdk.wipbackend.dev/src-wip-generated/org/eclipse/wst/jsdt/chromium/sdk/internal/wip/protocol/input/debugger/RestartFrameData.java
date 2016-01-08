// Generated source.
// Generator: org.eclipse.wst.jsdt.chromium.sdk.internal.wip.tools.protocolgenerator.Generator
// Origin: http://svn.webkit.org/repository/webkit/trunk/Source/WebCore/inspector/Inspector.json@121014

package org.eclipse.wst.jsdt.chromium.sdk.internal.wip.protocol.input.debugger;

/**
 Restarts particular call frame from the beginning.
 */
@org.eclipse.wst.jsdt.chromium.sdk.internal.protocolparser.JsonType
public interface RestartFrameData {
  /**
   New stack trace.
   */
  java.util.List<org.eclipse.wst.jsdt.chromium.sdk.internal.wip.protocol.input.debugger.CallFrameValue> callFrames();

  /**
   VM-specific description.
   */
  Result result();

  /**
   VM-specific description.
   */
  @org.eclipse.wst.jsdt.chromium.sdk.internal.protocolparser.JsonType(allowsOtherProperties=true)
  public interface Result extends org.eclipse.wst.jsdt.chromium.sdk.internal.protocolparser.JsonObjectBased {
  }
}
