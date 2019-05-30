/*
 * BSD 3-Clause License
 * 
 * Copyright (c) 2019, Open Communications Security
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.opencs.util.dto.sign;

import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class implements a DTO signer. It can be used to check the
 * integrity of the selected properties of a DTO against malicious
 * changes.
 * 
 * <p>The signature is created by concatenating the string values of the
 * properties into a string and then create a MAC value based on that.</p>
 * 
 * <p>Once this is done, the MAC can now be used to make sure that the
 * marked DTO values were not modified. This is possible because only
 * the component that knows the MAC key will be able to modify the DTO
 * and regenerate it again.</p>
 * 
 * @author Fabio Jun Takada Chino
 */
public class DTOSigner {
	
	public static final String DEFAULT_ALGORITHM = "HmacSHA256";
	
	private final String algorithm;
	
	private final SecretKeySpec macKey;
	
	/**
	 * Creates a new signer.
	 * 
	 * @param key The key to be used to sign the DTOs.
	 * @throws DTOSignerException In case of error.
	 */
	public DTOSigner(byte [] key) throws DTOSignerException {
		this(key, DEFAULT_ALGORITHM);
	}
	
	/**
	 * Creates a new signer.
	 * 
	 * @param key The key to be used to sign the DTOs.
	 * @param algorithm The MAC algorithm to be used (JCE).
	 * @throws DTOSignerException In case of error.
	 */
	public DTOSigner(byte [] key, String algorithm) throws DTOSignerException {
	
		this.algorithm = algorithm;
		this.macKey = new SecretKeySpec(key, algorithm);

		// Create a MAC instance just to check if the algorithm is valid. It should
		// help to prevent errors during the execution later.
		createMac();
	}

	private Mac createMac()  throws DTOSignerException {
		try {
			Mac mac = Mac.getInstance(algorithm);
			mac.init(this.macKey);
			return mac;
		} catch (Exception e) {
			throw new DTOSignerException(e.getMessage(), e);
		}

	}
	
	/**
	 * Computes the signature of all properties marked with SignedProperty annotation.
	 * 
	 * @param dto The DTO.
	 * @return The signature of the DTO.
	 * @throws DTOSignerException In case of error.
	 */
	public byte[] createSignature(Object dto) throws DTOSignerException {
		
		SignedPropertyExtractor extractor = SignedPropertyExtractorManager.getExtractor(dto.getClass());

		Mac mac = this.createMac();
		return mac.doFinal(extractor.extract(dto));
	}

	/**
	 * Computes the signature of the DTO and stores it in the property DTOSignature.
	 * 
	 * @param signed The DTO to be signed.
	 * @throws DTOSignerException In case of error.
	 */
	public void sign(SignedDTO<?> signed) throws DTOSignerException {
		signed.setSignature(createSignature(signed.get()));
	}
	
	/**
	 * Checks if the DTO signature still holds.
	 * 
	 * @param signed The DTO to be checked.
	 * @return true if the signature is valid or false otherwise.
	 * @throws DTOSignerException In case of error.
	 */
	public boolean checkSignature(SignedDTO<?> signed) throws DTOSignerException {

		if (signed.getSignature() != null) {
			byte [] sig  = createSignature(signed.get());
			if (sig != null) {
				return Arrays.equals(sig, signed.getSignature());
			} else {
				throw new DTOSignerException("The DTO is not signed.");
			}
		} else {
			return false;
		}
	}
}
