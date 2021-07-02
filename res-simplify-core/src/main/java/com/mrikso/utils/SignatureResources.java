/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mrikso.utils;

import com.android.apksig.internal.util.ByteStreams;
import com.android.apksig.internal.util.X509CertificateUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Assorted methods to obtaining test input from resources.
 */
public class SignatureResources {
    public SignatureResources() {
    }

    public static byte[] toByteArray(String resourceName) throws IOException {
        //try (FileInputStream in = new FileInputStream(resourceName)) {
        return ByteStreams.toByteArray(toInputStream(SignatureResources.class, resourceName));
        // }
    }

    public static InputStream toInputStream(Class<?> cls, String resourceName) throws IOException {
        InputStream in = cls.getResourceAsStream(resourceName);
        if (in == null) {
            throw new IllegalArgumentException("Resource not found: " + resourceName);
        }
        return in;
    }


    public static List<X509Certificate> toCertificateChain(String resourceName) throws IOException, CertificateException {
        Collection<? extends Certificate> certs;
        //try (FileInputStream in = new FileInputStream(resourceName)) {
        //  if (in == null) {
        //   throw new IllegalArgumentException("Resource not found: " + resourceName);
        //  }
        certs = X509CertificateUtils.generateCertificates(toInputStream(SignatureResources.class, resourceName));
        // }
        List<X509Certificate> result = new ArrayList<>(certs.size());
        for (Certificate cert : certs) {
            result.add((X509Certificate) cert);
        }
        return result;
    }

    public static PrivateKey toPrivateKey(String resourceName)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        int delimiterIndex = resourceName.indexOf('-');
        if (delimiterIndex == -1) {
            throw new IllegalArgumentException(
                    "Failed to autodetect key algorithm from resource name: " + resourceName);
        }
        String keyAlgorithm = resourceName.substring(0, delimiterIndex).toUpperCase(Locale.US);
        return toPrivateKey(resourceName, keyAlgorithm);
    }

    public static PrivateKey toPrivateKey(String resourceName, String keyAlgorithm)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] encoded = toByteArray(resourceName);

        // Keep overly strictly linter happy by limiting what JCA KeyFactory algorithms are used
        // here
        KeyFactory keyFactory;
        switch (keyAlgorithm.toUpperCase(Locale.US)) {
            case "RSA":
                keyFactory = KeyFactory.getInstance("rsa");
                break;
            case "DSA":
                keyFactory = KeyFactory.getInstance("dsa");
                break;
            case "EC":
                keyFactory = KeyFactory.getInstance("ec");
                break;
            default:
                throw new InvalidKeySpecException("Unsupported key algorithm: " + keyAlgorithm);
        }

        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }

}
