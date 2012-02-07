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
package org.swssf.wss.impl.securityToken;

import org.swssf.wss.ext.WSSConstants;
import org.swssf.wss.ext.WSSecurityContext;
import org.swssf.xmlsec.crypto.Crypto;
import org.swssf.xmlsec.ext.XMLSecurityException;

import javax.security.auth.callback.CallbackHandler;
import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;

/**
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class X509_V3SecurityToken extends X509SecurityToken {
    private String alias = null;
    private X509Certificate[] x509Certificates;

    X509_V3SecurityToken(WSSecurityContext wsSecurityContext, Crypto crypto, CallbackHandler callbackHandler, byte[] binaryContent,
                         String id, WSSConstants.KeyIdentifierType keyIdentifierType, Object processor) throws XMLSecurityException {
        super(WSSConstants.X509V3Token, wsSecurityContext, crypto, callbackHandler, id, keyIdentifierType, processor);
        this.x509Certificates = new X509Certificate[]{getCrypto().loadCertificate(new ByteArrayInputStream(binaryContent))};
    }

    protected String getAlias() throws XMLSecurityException {
        if (this.alias == null) {
            this.alias = getCrypto().getX509Identifier(this.x509Certificates[0]);
        }
        return this.alias;
    }

    @Override
    public X509Certificate[] getX509Certificates() throws XMLSecurityException {
        return this.x509Certificates;
    }
}
