/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.swssf.config;

import org.xmlsecurity.ns.configuration.AlgorithmType;
import org.xmlsecurity.ns.configuration.JCEAlgorithmMappingsType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapping between JCE id and xmlsec uri's for algorithms
 * Class lent from apache santuario (xmlsec)
 */
public class JCEAlgorithmMapper {

    private static Map<String, String> uriToJCEName;
    private static Map<String, AlgorithmType> algorithmsMap;

    private JCEAlgorithmMapper() {
    }

    protected static void init(JCEAlgorithmMappingsType jceAlgorithmMappingsType) throws Exception {
        List<AlgorithmType> algorithms = jceAlgorithmMappingsType.getAlgorithms().getAlgorithm();
        uriToJCEName = new HashMap<String, String>(algorithms.size());
        algorithmsMap = new HashMap<String, AlgorithmType>(algorithms.size());

        for (int i = 0; i < algorithms.size(); i++) {
            AlgorithmType algorithmType = algorithms.get(i);
            uriToJCEName.put(algorithmType.getURI(), algorithmType.getJCEName());
            algorithmsMap.put(algorithmType.getURI(), algorithmType);
        }
    }

    public static AlgorithmType getAlgorithmMapping(String algoURI) {
        return algorithmsMap.get(algoURI);
    }

    public static String translateURItoJCEID(String AlgorithmURI) {
        return uriToJCEName.get(AlgorithmURI);
    }

    public static String getAlgorithmClassFromURI(String AlgorithmURI) {
        return algorithmsMap.get(AlgorithmURI).getAlgorithmClass();
    }

    public static int getKeyLengthFromURI(String AlgorithmURI) {
        return algorithmsMap.get(AlgorithmURI).getKeyLength();
    }

    public static String getJCEKeyAlgorithmFromURI(String AlgorithmURI) {
        return algorithmsMap.get(AlgorithmURI).getRequiredKey();
    }
}