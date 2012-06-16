/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.swssf.policy.assertionStates;

import org.apache.ws.secpolicy.AssertionState;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.apache.ws.secpolicy.model.RequiredElements;
import org.apache.ws.secpolicy.model.XPath;
import org.swssf.policy.Assertable;
import org.swssf.policy.PolicyUtils;
import org.swssf.wss.ext.WSSUtils;
import org.swssf.wss.securityEvent.RequiredElementSecurityEvent;
import org.swssf.wss.securityEvent.SecurityEvent;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * WSP1.3, 4.3.1 RequiredElements Assertion
 *
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class RequiredElementsAssertionState extends AssertionState implements Assertable {

    private final Map<List<QName>, Boolean> pathElements = new HashMap<List<QName>, Boolean>();

    public RequiredElementsAssertionState(AbstractSecurityAssertion assertion, boolean asserted) {
        super(assertion, asserted);

        if (assertion instanceof RequiredElements) {
            RequiredElements requiredElements = (RequiredElements) assertion;
            for (int i = 0; i < requiredElements.getXPaths().size(); i++) {
                XPath xPath = requiredElements.getXPaths().get(i);
                List<QName> elements = PolicyUtils.getElementPath(xPath);
                pathElements.put(elements, Boolean.FALSE);
            }
        }
    }

    public void addElement(List<QName> pathElement) {
        this.pathElements.put(pathElement, Boolean.FALSE);
    }

    @Override
    public SecurityEvent.Event[] getSecurityEventType() {
        return new SecurityEvent.Event[]{
                SecurityEvent.Event.RequiredElement
        };
    }

    @Override
    public boolean assertEvent(SecurityEvent securityEvent) throws WSSPolicyException {
        RequiredElementSecurityEvent requiredElementSecurityEvent = (RequiredElementSecurityEvent) securityEvent;

        Iterator<Map.Entry<List<QName>, Boolean>> elementMapIterator = pathElements.entrySet().iterator();
        while (elementMapIterator.hasNext()) {
            Map.Entry<List<QName>, Boolean> next = elementMapIterator.next();
            List<QName> qNameList = next.getKey();
            if (WSSUtils.pathMatches(qNameList, requiredElementSecurityEvent.getElementPath(), true, false)) {
                next.setValue(Boolean.TRUE);
                break;
            }
        }
        //if we return false here other required elements will trigger a PolicyViolationException
        return true;
    }

    @Override
    public boolean isAsserted() {
        Iterator<Map.Entry<List<QName>, Boolean>> elementMapIterator = pathElements.entrySet().iterator();
        while (elementMapIterator.hasNext()) {
            Map.Entry<List<QName>, Boolean> next = elementMapIterator.next();
            if (Boolean.FALSE.equals(next.getValue())) {
                setErrorMessage("Element " + WSSUtils.pathAsString(next.getKey()) + " must be present");
                return false;
            }
        }
        return true;
    }
}
