/*
 * JPty - A small PTY interface for Java.
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.pty4j.unix.macosx;

import java.util.Arrays;
import java.util.List;

import com.pty4j.WinSize;
import com.pty4j.unix.FDSet;
import com.pty4j.unix.Pollfd;
import com.pty4j.unix.PtyHelpers;
import com.pty4j.unix.PtyHelpers.OSFacade;
import com.pty4j.unix.Termios;
import com.pty4j.unix.TimeVal;
import com.pty4j.unix.posix.fd_set;
import com.pty4j.unix.posix.timeval;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.StringArray;
import com.sun.jna.Structure;

/**
 * Provides a {@link OSFacade} implementation for MacOSX.
 */
public class OSFacadeImpl implements PtyHelpers.OSFacade {
	// INNER TYPES
	public interface MacOSX_C_lib extends com.sun.jna.Library {
		int pipe(int[] fds);

		int execv(String command, StringArray argv);

		int execve(String command, StringArray argv, StringArray env);

		int ioctl(int fd, NativeLong cmd, PtyHelpers.winsize data);

		int kill(int pid, int signal);

		int waitpid(int pid, int[] stat, int options);

		int sigprocmask(int how, com.sun.jna.ptr.IntByReference set, com.sun.jna.ptr.IntByReference oldset);

		String strerror(int errno);

		int grantpt(int fdm);

		int unlockpt(int fdm);

		int close(int fd);

		String ptsname(int fd);

		int open(String pts_name, int o_rdwr);

		int killpg(int pid, int sig);

		int fork();

		int setsid();

		int getpid();

		int seteuid(int euid);

		int setuid(int uid);

		int geteuid();

		int getuid();

		int setpgid(int pid, int pgid);

		void dup2(int fd, int fileno);

		int getppid();

		void unsetenv(String s);

		int login_tty(int fd);

		void chdir(String dirpath);

		int write(int fd, byte[] buffer, int count);

		int read(int fd, byte[] buffer, int count);

		int select(int n, fd_set read, fd_set write, fd_set error, timeval timeout);

		int tcgetattr(int fd, termios termios);

		int tcsetattr(int fd, int cmd, termios termios);
	}

	// CONSTANTS
	private static final long TIOCGWINSZ = 0x40087468L;
	private static final long TIOCSWINSZ = 0x80087467L;
	// VARIABLES
	private static MacOSX_C_lib m_Clib = Native.loadLibrary("c", MacOSX_C_lib.class);

	// CONSTUCTORS
	/**
	 * Creates a new {@link OSFacadeImpl} instance.
	 */
	public OSFacadeImpl() {
		PtyHelpers.ONLCR = 0x02;
		PtyHelpers.VERASE = 3;
		PtyHelpers.VWERASE = 4;
		PtyHelpers.VKILL = 5;
		PtyHelpers.VREPRINT = 6;
		PtyHelpers.VINTR = 8;
		PtyHelpers.VQUIT = 9;
		PtyHelpers.VSUSP = 10;
		PtyHelpers.ECHOKE = 0x01;
		PtyHelpers.ECHOCTL = 0x40;
	}

	// METHODS
	@Override
	public int execve(String command, String[] argv, String[] env) {
		StringArray argvp = (argv == null) ? new StringArray(new String[] { command }) : new StringArray(argv);
		StringArray envp = (env == null) ? null : new StringArray(env);
		return m_Clib.execve(command, argvp, envp);
	}

	@Override
	public int getWinSize(int fd, WinSize winSize) {
		int r;
		PtyHelpers.winsize ws = new PtyHelpers.winsize();
		if ((r = m_Clib.ioctl(fd, new NativeLong(TIOCGWINSZ), ws)) < 0) {
			return r;
		}
		ws.update(winSize);
		return r;
	}

	@Override
	public int kill(int pid, int signal) {
		return m_Clib.kill(pid, signal);
	}

	@Override
	public int setWinSize(int fd, WinSize winSize) {
		PtyHelpers.winsize ws = new PtyHelpers.winsize(winSize);
		return m_Clib.ioctl(fd, new NativeLong(TIOCSWINSZ), ws);
	}

	@Override
	public int waitpid(int pid, int[] stat, int options) {
		return m_Clib.waitpid(pid, stat, options);
	}

	@Override
	public int sigprocmask(int how, com.sun.jna.ptr.IntByReference set, com.sun.jna.ptr.IntByReference oldset) {
		return m_Clib.sigprocmask(how, set, oldset);
	}

	@Override
	public String strerror(int errno) {
		return m_Clib.strerror(errno);
	}

	@Override
	public int getpt() {
		return m_Clib.open("/dev/ptmx", PtyHelpers.O_RDWR | PtyHelpers.O_NOCTTY);
	}

	@Override
	public int grantpt(int fd) {
		return m_Clib.grantpt(fd);
	}

	@Override
	public int unlockpt(int fd) {
		return m_Clib.unlockpt(fd);
	}

	@Override
	public int open(String path, int flags) {
		return m_Clib.open(path, flags);
	}

	@Override
	public int close(int fd) {
		return m_Clib.close(fd);
	}

	@Override
	public String ptsname(int fd) {
		return m_Clib.ptsname(fd);
	}

	@Override
	public int killpg(int pid, int sig) {
		return m_Clib.killpg(pid, sig);
	}

	@Override
	public int fork() {
		return m_Clib.fork();
	}

	@Override
	public int pipe(int[] fds) {
		return m_Clib.pipe(fds);
	}

	@Override
	public int setsid() {
		return m_Clib.setsid();
	}

	@Override
	public void execv(String path, String[] argv) {
		StringArray argvp = (argv == null) ? new StringArray(new String[] { path }) : new StringArray(argv);
		m_Clib.execv(path, argvp);
	}

	@Override
	public int getpid() {
		return m_Clib.getpid();
	}

	@Override
	public int setpgid(int pid, int pgid) {
		return m_Clib.setpgid(pid, pgid);
	}

	@Override
	public void dup2(int fds, int fileno) {
		m_Clib.dup2(fds, fileno);
	}

	@Override
	public int getppid() {
		return m_Clib.getppid();
	}

	@Override
	public void unsetenv(String s) {
		m_Clib.unsetenv(s);
	}

	@Override
	public int login_tty(int fd) {
		return m_Clib.login_tty(fd);
	}

	@Override
	public void chdir(String dirpath) {
		m_Clib.chdir(dirpath);
	}

	@Override
	public int seteuid(int euid) {
		return m_Clib.seteuid(euid);
	}

	@Override
	public int geteuid() {
		return m_Clib.geteuid();
	}

	@Override
	public int setuid(int uid) {
		return m_Clib.setuid(uid);
	}

	@Override
	public int getuid() {
		return m_Clib.getuid();
	}

	@Override
	public int read(int fd, byte[] buffer, int len) {
		return m_Clib.read(fd, buffer, len);
	}

	@Override
	public int write(int fd, byte[] buffer, int len) {
		return m_Clib.write(fd, buffer, len);
	}

	public int select(int nfds, FDSet rfds, FDSet wfds, FDSet efds, TimeVal timeout) {
		timeval tout = null;
		if (timeout != null) {
			tout = new timeval(timeout);
		}
		return m_Clib.select(nfds, (fd_set) rfds, (fd_set) wfds, (fd_set) efds, tout);
	}

	public int poll(Pollfd fds[], int nfds, int timeout) {
		throw new UnsupportedOperationException("Poll not supported");
	}

	@Override
	public FDSet newFDSet() {
		return new fd_set();
	}

	public int tcgetattr(int fd, Termios termios) {
		termios t = new termios();
		int ret = m_Clib.tcgetattr(fd, t);
		t.update(termios);
		return ret;
	}

	public int tcsetattr(int fd, int cmd, Termios termios) {
		return m_Clib.tcsetattr(fd, cmd, new termios(termios));
	}

	static public class termios extends Structure {
		public NativeLong c_iflag;
		public NativeLong c_oflag;
		public NativeLong c_cflag;
		public NativeLong c_lflag;
		public byte[] c_cc = new byte[20];
		public NativeLong c_ispeed;
		public NativeLong c_ospeed;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(//
					"c_iflag", //
					"c_oflag", //
					"c_cflag", //
					"c_lflag", //
					"c_cc", //
					"c_ispeed", //
					"c_ospeed"//
			);
		}

		public termios() {
		}

		public termios(Termios t) {
			c_iflag.setValue(t.c_iflag);
			c_oflag.setValue(t.c_oflag);
			c_cflag.setValue(t.c_cflag);
			c_lflag.setValue(t.c_lflag);
			System.arraycopy(t.c_cc, 0, c_cc, 0, Math.min(t.c_cc.length, c_cc.length));
			c_ispeed.setValue(t.c_ispeed);
			c_ospeed.setValue(t.c_ospeed);
		}

		public void update(Termios t) {
			t.c_iflag = c_iflag.intValue();
			t.c_oflag = c_oflag.intValue();
			t.c_cflag = c_cflag.intValue();
			t.c_lflag = c_lflag.intValue();
			System.arraycopy(c_cc, 0, t.c_cc, 0, Math.min(t.c_cc.length, c_cc.length));
			t.c_ispeed = c_ispeed.intValue();
			t.c_ospeed = c_ospeed.intValue();
		}
	}
}
