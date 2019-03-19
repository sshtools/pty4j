package com.pty4j.unix;

public interface FDSet {
	public void FD_SET(int fd);

	public void FD_CLR(int fd);

	public boolean FD_ISSET(int fd);

	public void FD_ZERO();
}