/*
 * Copyright (c) 2011, Kustaa Nyholm / SpareTimeLabs
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list 
 * of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this 
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *  
 * Neither the name of the Kustaa Nyholm or SpareTimeLabs nor the names of its 
 * contributors may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.pty4j.unix.posix;

import java.util.Arrays;
import java.util.List;

import com.pty4j.unix.FDSet;
import com.sun.jna.Structure;

public class fd_set extends Structure implements FDSet {
	private final static int NFBBITS = 32;
	private final static int fd_count = 1024;
	public int[] fd_array = new int[(fd_count + NFBBITS - 1) / NFBBITS];

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(//
				"fd_array"//
		);
	}

	public void FD_SET(int fd) {
		fd_array[fd / NFBBITS] |= (1 << (fd % NFBBITS));
	}

	public boolean FD_ISSET(int fd) {
		return (fd_array[fd / NFBBITS] & (1 << (fd % NFBBITS))) != 0;
	}

	public void FD_ZERO() {
		Arrays.fill(fd_array, 0);
	}

	public void FD_CLR(int fd) {
		fd_array[fd / NFBBITS] &= ~(1 << (fd % NFBBITS));
	}
}