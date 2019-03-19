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
package com.pty4j.unix.openbsd;

import com.pty4j.WinSize;
import com.pty4j.unix.FDSet;
import com.pty4j.unix.Pollfd;
import com.pty4j.unix.PtyHelpers;
import com.pty4j.unix.PtyHelpers.OSFacade;
import com.pty4j.unix.Termios;
import com.pty4j.unix.TimeVal;
import com.pty4j.unix.posix.fd_set;
import com.pty4j.unix.posix.pollfd;
import com.pty4j.unix.posix.termios;
import com.pty4j.unix.posix.timeval;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.IntByReference;

/**
 * Provides a {@link OSFacade} implementation for OpenBSD.
 */
public class OSFacadeImpl implements PtyHelpers.OSFacade {
	// INNER TYPES
	public interface OpenBSD_C_lib extends Library {
		int pipe(int[] fds);

		int posix_openpt(int oflag);

		int execv(String command, StringArray argv);

		int execve(String command, StringArray argv, StringArray env);

		int ioctl(int fd, NativeLong cmd, PtyHelpers.winsize data);

		int kill(int pid, int signal);

		int waitpid(int pid, int[] stat, int options);

		int sigprocmask(int how, IntByReference set, IntByReference oldset);

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

		int setpgid(int pid, int pgid);

		void dup2(int fd, int fileno);

		int getppid();

		void unsetenv(String s);

		int login_tty(int fd);

		void chdir(String dirpath);

		int seteuid(int suid);

		int setuid(int suid);

		int geteuid();

		int getuid();

		int write(int fd, byte[] buffer, int count);

		int select(int n, fd_set read, fd_set write, fd_set error, timeval timeout);

		int read(int fd, byte[] buffer, int count);

		int poll(pollfd.ByReference pfds, int nfds, int timeout);

        int tcgetattr(int fd, termios termios);

        int tcsetattr(int fd, int cmd, termios termios);
	}

	public interface OpenBSD_Util_lib extends Library {
		int login_tty(int fd);
	}

	// CONSTANTS
	private static final long TIOCGWINSZ = 0x40087468L;
	private static final long TIOCSWINSZ = 0x80087467L;
	// VARIABLES
	private static OpenBSD_C_lib m_Clib = Native.loadLibrary("c", OpenBSD_C_lib.class);
	private static OpenBSD_Util_lib m_Utillib = Native.loadLibrary("util", OpenBSD_Util_lib.class);

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
	public int sigprocmask(int how, IntByReference set, IntByReference oldset) {
		return m_Clib.sigprocmask(how, set, oldset);
	}

	@Override
	public String strerror(int errno) {
		return m_Clib.strerror(errno);
	}

	@Override
	public int getpt() {
		return m_Clib.posix_openpt(PtyHelpers.O_RDWR | PtyHelpers.O_NOCTTY);
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
	public int close(int fd) {
		return m_Clib.close(fd);
	}

	@Override
	public String ptsname(int fd) {
		return m_Clib.ptsname(fd);
	}

	@Override
	public int open(String path, int flags) {
		return m_Clib.open(path, flags);
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
	public int pipe(int[] pipe2) {
		return m_Clib.pipe(pipe2);
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
		return m_Utillib.login_tty(fd);
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
		return m_Clib.seteuid(uid);
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
		if (nfds <= 0 || nfds > fds.length) {
			throw new java.lang.IllegalArgumentException("nfds " + nfds + " must be <= fds.length " + fds.length);
		}
		pollfd.ByReference parampfds = new pollfd.ByReference();
		pollfd[] pfds = (pollfd[]) parampfds.toArray(nfds);
		for (int i = 0; i < nfds; i++) {
			pfds[i].fd = fds[i].fd;
			pfds[i].events = fds[i].events;
		}
		int ret = m_Clib.poll(parampfds, nfds, timeout);
		for (int i = 0; i < nfds; i++) {
			fds[i].revents = pfds[i].revents;
		}
		return ret;
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
}
