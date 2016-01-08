// Generated source.
// Generator: org.eclipse.wst.jsdt.chromium.sdk.internal.wip.tools.protocolgenerator.Generator
// Origin: http://svn.webkit.org/repository/webkit/trunk/Source/WebCore/inspector/Inspector.json@135591

package org.eclipse.wst.jsdt.chromium.sdk.internal.wip.protocol.input.runtime;

/**
 Object property descriptor.
 */
@org.eclipse.wst.jsdt.chromium.sdk.internal.protocolparser.JsonType
public interface PropertyDescriptorValue {
  /**
   Property name.
   */
  String name();

  /**
   The value associated with the property.
   */
  @org.eclipse.wst.jsdt.chromium.sdk.internal.protocolparser.JsonOptionalField
  org.eclipse.wst.jsdt.chromium.sdk.internal.wip.protocol.input.runtime.RemoteObjectValue value();

  /**
   True if the value associated with the property may be changed (data descriptors only).
   */
  @org.eclipse.wst.jsdt.chromium.sdk.internal.protocolparser.JsonOptionalField
  Boolean writable();

  /**
   A function which serves as a getter for the property, or <code>undefined</code> if there is no getter (accessor descriptors only).
   */
  @org.eclipse.wst.jsdt.chromium.sdk.internal.protocolparser.JsonOptionalField
  org.eclipse.wst.jsdt.chromium.sdk.internal.wip.protocol.input.runtime.RemoteObjectValue get();

  /**
   A function which serves as a setter for the property, or <code>undefined</code> if there is no setter (accessor descriptors only).
   */
  @org.eclipse.wst.jsdt.chromium.sdk.internal.protocolparser.JsonOptionalField
  org.eclipse.wst.jsdt.chromium.sdk.internal.wip.protocol.input.runtime.RemoteObjectValue set();

  /**
   True if the type of this property descriptor may be changed and if the property may be deleted from the corresponding object.
   */
  boolean configurable();

  /**
   True if this property shows up during enumeration of the properties on the corresponding object.
   */
  boolean enumerable();

  /**
   True if the result was thrown during the evaluation.
   */
  @org.eclipse.wst.jsdt.chromium.sdk.internal.protocolparser.JsonOptionalField
  Boolean wasThrown();

  /**
   True if the property is owned for the object.
   */
  @org.eclipse.wst.jsdt.chromium.sdk.internal.protocolparser.JsonOptionalField
  Boolean isOwn();

}
