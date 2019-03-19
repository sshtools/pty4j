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

import com.pty4j.unix.Termios;
import com.sun.jna.Structure;

public class termios extends Structure {

    public int c_iflag;
    public int c_oflag;
    public int c_cflag;
    public int c_lflag;
    public byte[] c_cc = new byte[20];
    public int c_ispeed;
    public int c_ospeed;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(//
                "c_iflag",//
                "c_oflag",//
                "c_cflag",//
                "c_lflag",//
                "c_cc",//
                "c_ispeed",//
                "c_ospeed"//
        );
    }

    public termios() {
    }

    public termios(Termios t) {
        c_iflag = t.c_iflag;
        c_oflag = t.c_oflag;
        c_cflag = t.c_cflag;
        System.arraycopy(t.c_cc, 0, c_cc, 0, Math.min(t.c_cc.length, c_cc.length));
        c_ispeed = t.c_ispeed;
        c_ospeed = t.c_ospeed;
    }

    public void update(Termios t) {
        t.c_iflag = c_iflag;
        t.c_oflag = c_oflag;
        t.c_cflag = c_cflag;
        System.arraycopy(c_cc, 0, t.c_cc, 0, Math.min(t.c_cc.length, c_cc.length));
        t.c_ispeed = c_ispeed;
        t.c_ospeed = c_ospeed;
    }
}