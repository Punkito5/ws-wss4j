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

package org.apache.ws.security.dom.saml;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ws.security.dom.WSDerivedKeyTokenPrincipal;
import org.apache.ws.security.dom.WSDocInfo;
import org.apache.ws.security.dom.WSSecurityEngine;
import org.apache.ws.security.dom.WSSecurityEngineResult;
import org.apache.ws.security.common.crypto.AlgorithmSuite;
import org.apache.ws.security.common.crypto.AlgorithmSuiteValidator;
import org.apache.ws.security.common.ext.WSSecurityException;
import org.apache.ws.security.common.saml.SAMLKeyInfo;
import org.apache.ws.security.common.saml.SAMLKeyInfoProcessor;
import org.apache.ws.security.dom.handler.RequestData;
import org.apache.ws.security.dom.message.token.SecurityTokenReference;
import org.apache.ws.security.dom.processor.EncryptedKeyProcessor;
import org.apache.ws.security.dom.str.STRParser;
import org.apache.ws.security.dom.str.SignatureSTRParser;
import org.apache.xml.security.utils.Base64;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * This interface allows the user to plug in custom ways of processing a SAML KeyInfo.
 */
public class WSSSAMLKeyInfoProcessor implements SAMLKeyInfoProcessor {
    
    private static final String WST_NS = "http://schemas.xmlsoap.org/ws/2005/02/trust";
    private static final String WST_NS_05_12 = 
        "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
    
    private static final QName BINARY_SECRET = 
        new QName(WST_NS, "BinarySecret");
    private static final QName BINARY_SECRET_05_12 = 
        new QName(WST_NS_05_12, "BinarySecret");
    
    private RequestData data;
    private WSDocInfo docInfo;
    
    public WSSSAMLKeyInfoProcessor(RequestData data, WSDocInfo docInfo) {
        this.data = data;
        this.docInfo = docInfo;
    }
    
    public SAMLKeyInfo processSAMLKeyInfo(Element keyInfoElement) throws WSSecurityException {
        //
        // First try to find an EncryptedKey, BinarySecret or a SecurityTokenReference via DOM
        //
        Node node = keyInfoElement.getFirstChild();
        while (node != null) {
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                QName el = new QName(node.getNamespaceURI(), node.getLocalName());
                if (el.equals(WSSecurityEngine.ENCRYPTED_KEY)) {
                    EncryptedKeyProcessor proc = new EncryptedKeyProcessor();
                    List<WSSecurityEngineResult> result =
                        proc.handleToken((Element)node, data, docInfo, data.getSamlAlgorithmSuite());
                    byte[] secret = 
                        (byte[])result.get(0).get(
                            WSSecurityEngineResult.TAG_SECRET
                        );
                    return new SAMLKeyInfo(secret);
                } else if (el.equals(BINARY_SECRET) || el.equals(BINARY_SECRET_05_12)) {
                    Text txt = (Text)node.getFirstChild();
                    try {
                        return new SAMLKeyInfo(Base64.decode(txt.getData()));
                    } catch (Exception e) {
                        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE,
                                "empty", e, "decoding.general");
                    }
                } else if (SecurityTokenReference.STR_QNAME.equals(el)) {
                    STRParser strParser = new SignatureSTRParser();
                    strParser.parseSecurityTokenReference(
                        (Element)node, data, docInfo, new HashMap<String, Object>()
                    );
                    SAMLKeyInfo samlKeyInfo = new SAMLKeyInfo(strParser.getCertificates());
                    samlKeyInfo.setPublicKey(strParser.getPublicKey());
                    samlKeyInfo.setSecret(strParser.getSecretKey());
                    
                    Principal principal = strParser.getPrincipal();
                    
                    // Check for compliance against the defined AlgorithmSuite
                    AlgorithmSuite algorithmSuite = data.getSamlAlgorithmSuite(); 
                    if (algorithmSuite != null && principal instanceof WSDerivedKeyTokenPrincipal) {
                        AlgorithmSuiteValidator algorithmSuiteValidator = new
                            AlgorithmSuiteValidator(algorithmSuite);

                        algorithmSuiteValidator.checkDerivedKeyAlgorithm(
                            ((WSDerivedKeyTokenPrincipal)principal).getAlgorithm()
                        );
                        algorithmSuiteValidator.checkSignatureDerivedKeyLength(
                            ((WSDerivedKeyTokenPrincipal)principal).getLength()
                        );
                    }
                    
                    return samlKeyInfo;
                }
            }
            node = node.getNextSibling();
        }
        
        return null;
    }
}