/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Wind River Systems, Inc.  
 *     Mikhail Zabaluev (Nokia) - bug 82744
 *     Mikhail Sennikovsky - bug 145737
 *******************************************************************************/
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <stdbool.h>

#include <grp.h>
#include "exec_pty.h"
#include <sys/stat.h>

int test();

/* from pfind.c */
extern char *pfind(const char *name, char * const envp[]);

/* from openpty.c */

extern int ptys_open(int fdm, const char *pts_name, bool acquire);

extern void set_noecho(int fd);


pid_t exec_pty(const char *path, char *const argv[], char *const envp[], const char *dirpath,
		       const char *pts_name, int fdm, const char *err_pts_name, int err_fdm, int console, uid_t euid)
{
	pid_t childpid;
	uid_t  ruid;
	char *full_path;

	/*
	 * We use pfind() to check that the program exists and is an executable.
	 * If not pass the error up.  Also execve() wants a full path.
	 */ 
	full_path = pfind(path, envp);
	if (full_path == NULL) {
		fprintf(stderr, "Unable to find full path for \"%s\"\n", (path) ? path : "");
		return -1;
	}

	childpid = fork();

	if (childpid < 0) {
		fprintf(stderr, "%s(%d): returning due to error: %s\n", __FUNCTION__, __LINE__, strerror(errno));
		free(full_path);
		return -1;
	} else if (childpid == 0) {

		/* child */

		if(!console && euid > 0) {
			gid_t gid = getgrnam("tty");
			if (chown(pts_name, euid, gid) != 0 ||
					    chmod(pts_name, S_IRUSR | S_IWUSR | S_IWGRP) != 0) {
				fprintf(stderr, "%s(%d): failed to chown or chmod %s. %s\n", __FUNCTION__, __LINE__, pts_name, strerror(errno));
				free(full_path);
				return -1;
			}
		}

		chdir(dirpath);

		int fds;
		int err_fds;

		if (!console && setsid() < 0) {
			perror("setsid()");
			return -1;
		}

		fds = ptys_open(fdm, pts_name, true);
		if (fds < 0) {
			fprintf(stderr, "%s(%d): returning due to error: %s\n", __FUNCTION__, __LINE__, strerror(errno));
			return -1;
		}

		if (console) {
			err_fds = ptys_open(err_fdm, err_pts_name, false);
			if (err_fds < 0) {
				fprintf(stderr, "%s(%d): returning due to error: %s\n", __FUNCTION__, __LINE__, strerror(errno));
				return -1;
			}
		}

		/* close masters, no need in the child */
		close(fdm);
		if (console) close(err_fdm);

		if (console) {
			set_noecho(fds);
			if (setpgid(getpid(), getpid()) < 0) {
				perror("setpgid()");
				return -1;
			}
		}

		/* redirections */
		dup2(fds, STDIN_FILENO);   /* dup stdin */
		dup2(fds, STDOUT_FILENO);  /* dup stdout */
		dup2(console ? err_fds : fds, STDERR_FILENO);  /* dup stderr */

		close(fds);  /* done with fds. */
		if (console) close(err_fds);

		/* Close all the fd's in the child */
		{
			int fdlimit = sysconf(_SC_OPEN_MAX);
			int fd = 3;

			while (fd < fdlimit)
				close(fd++);
		}



		if(!console && euid > 0) {
			int status;
			status = setuid (euid);
			if (status < 0) {
				perror("seteuid()");
				return -1;
			}
		}

		execve(full_path, argv, envp);

		_exit(127);

	} else if (childpid != 0) { /* parent */
		if (console) {
			set_noecho(fdm);
		}

		free(full_path);
		return childpid;
	}

	free(full_path);
	return -1;                  /*NOT REACHED */
}

