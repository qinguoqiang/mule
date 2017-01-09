/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extension.dsl;

import static org.mule.runtime.api.dsl.DslConstants.REDELIVERY_POLICY_ELEMENT_IDENTIFIER;
import static org.mule.runtime.core.util.IOUtils.getResourceAsString;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.compareXML;
import org.mule.runtime.api.dsl.config.ComponentConfiguration;
import org.mule.runtime.extension.api.dsl.converter.XmlDslElementModelConverter;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class IntegrationDslModelSerializerTestCase extends AbstractElementModelTestCase {

  private Element flow;
  private String expectedAppXml;

  @Before
  public void createDocument() throws Exception {
    initializeMuleApp();

    Element flow = doc.createElement("flow");
    flow.setAttribute("name", "testFlow");
    flow.setAttribute("initialState", "stopped");
    this.flow = flow;
  }

  @Before
  public void loadExpectedResult() throws IOException {
    expectedAppXml = getResourceAsString(getConfigFile(), getClass());
  }

  @Test
  public void serialize() throws Exception {
    XmlDslElementModelConverter converter = XmlDslElementModelConverter.getDefault(this.doc);

    doc.getDocumentElement().appendChild(converter.asXml(resolve(getAppElement(applicationModel, DB_CONFIG))));
    doc.getDocumentElement().appendChild(converter.asXml(resolve(getAppElement(applicationModel, HTTP_LISTENER_CONFIG))));
    doc.getDocumentElement().appendChild(converter.asXml(resolve(getAppElement(applicationModel, HTTP_REQUESTER_CONFIG))));

    ComponentConfiguration componentsFlow = getAppElement(applicationModel, COMPONENTS_FLOW);
    Element httpListenerSource = converter.asXml(resolve(componentsFlow.getNestedComponents().get(LISTENER_PATH)));

    // For some reason mule provides the `redelivery-policy` as an external component, but we need to serialize it
    // as an http child element to match the original application
    addRedeliveryPolicy(componentsFlow, httpListenerSource);
    flow.appendChild(httpListenerSource);

    flow.appendChild(converter.asXml(resolve(componentsFlow.getNestedComponents().get(DB_INSERT_PATH))));
    flow.appendChild(converter.asXml(resolve(componentsFlow.getNestedComponents().get(REQUESTER_PATH))));

    doc.getDocumentElement().appendChild(flow);

    muleContext.getExtensionManager().getExtensions().forEach(e -> addSchemaLocation(doc, e));

    String serializationResult = write();

    compareXML(expectedAppXml, serializationResult);
  }

  private void addRedeliveryPolicy(ComponentConfiguration componentsFlow, Element httpListenerSource) {
    ComponentConfiguration redeliveryPolicy = componentsFlow.getNestedComponents().get(1);
    Element policyElement = doc.createElement(REDELIVERY_POLICY_ELEMENT_IDENTIFIER);
    redeliveryPolicy.getParameters().forEach(policyElement::setAttribute);
    httpListenerSource.insertBefore(policyElement, httpListenerSource.getFirstChild());
  }

}
