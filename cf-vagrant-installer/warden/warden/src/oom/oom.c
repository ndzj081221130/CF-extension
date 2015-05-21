#include <assert.h>
#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/eventfd.h>
#include <sys/param.h>
#include <sys/prctl.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>


 
/* `detect_oom` returns zero when OOM was detected, non-zero otherwise. */
int detect_oom(int event_fd, const char *event_control_path) {
  fd_set rfds;
  int rv;

  FD_ZERO(&rfds);

  /* Stick around for read on eventfd */
  FD_SET(event_fd, &rfds);
     printf("---zhang:stdout6.1 this is a printf\n");
     fflush(stdout);
      //event_fd=3
  do {
    printf("before select\n");fflush(stdout);
    rv = select(event_fd + 1, &rfds, NULL, NULL, NULL);
	printf("---after select %d",rv);fflush(stdout);
      
  } while(rv == -1 && errno == EINTR);
     
  /* Return if something other than an OOM happened */
  if (!FD_ISSET(event_fd, &rfds)) {
    //printf("---zhang:stdout6.3");fflush(stdout);
    return -1;
  }

  uint64_t result;

  do {
    rv = read(event_fd, &result, sizeof(result));
    printf("after read %d",rv);fflush(stdout);
  } while (rv == -1 && errno == EINTR);
  printf("---zhang:out6.4");fflush(stdout);
  if (rv == -1) {
    printf("read");fflush(stdout);
    return -1;
  }
  printf("---zhang:out6.5");fflush(stdout);
  assert(rv == sizeof(result));

  /* Check if the event_fd triggered because the cgroup was removed */
  rv = access(event_control_path, W_OK);
  if (rv == -1 && errno == ENOENT) {
    perror("access");
    return -1;
  }
 // perror("---zhang:out6.6");
  if (rv == -1) {
    perror("access");
    return -1;
  }

  /* Read from event_fd, and cgroup is present */
  printf("---zhang:oom ");fflush(stdout);
  //char s[64];
  //perror(itoa(rv,s));
  return 0;
}

int main(int argc, char **argv) {
  int event_fd = -1;
  char oom_control_path[PATH_MAX];
  size_t oom_control_path_len;
  int oom_control_fd = -1;
  char event_control_path[PATH_MAX];
  size_t event_control_path_len;
  int event_control_fd = -1;
  char line[LINE_MAX];
  size_t line_len;
  int rv;
//	perror("---zhang:out1");
//char ss[64];
 // perror(itoa(argc,ss));
//perror(argv[0]);
  if (argc != 2) {
    fprintf(stderr, "Usage: %s <path to cgroup>\n", argv[0]);
    return 1;
  }
//perror("---zhang:out2");
  /* Die when parent dies */
  rv = prctl(PR_SET_PDEATHSIG, SIGKILL, 0, 0, 0);
  if (rv == -1) {
    perror("prctl");
    return 1;
  }
//perror("---zhang:out3");
  /* Open event fd */
  event_fd = eventfd(0, 0);
  if (event_fd == -1) {
    perror("eventfd");
    return 1;
  }
//perror("---zhang:out4");
  /* Open oom control file */
  oom_control_path_len = snprintf(oom_control_path, sizeof(oom_control_path), "%s/memory.oom_control", argv[1]);
  assert(oom_control_path_len < sizeof(oom_control_path));

  oom_control_fd = open(oom_control_path, O_RDONLY);
  if (oom_control_fd == -1) {
    perror("open");
    return 1;
  }
//perror("---zhang:out5");
  /* Open event control file */
  event_control_path_len = snprintf(event_control_path, sizeof(event_control_path), "%s/cgroup.event_control", argv[1]);
  assert(event_control_path_len < sizeof(event_control_path));

  event_control_fd = open(event_control_path, O_WRONLY);
  if (event_control_fd == -1) {
    perror("open");
    return 1;
  }
//perror("---zhang:out5.1");
  /* Write event fd and oom control fd to event control fd */
  line_len = snprintf(line, sizeof(line), "%d %d\n", event_fd, oom_control_fd);
  assert(line_len < sizeof(line));

  rv = write(event_control_fd, line, line_len);
  if (rv == -1) {
    perror("write");
    return 1;
  }
  //perror("---zhang:out6");
  rv =  detect_oom(event_fd, event_control_path);
  if (rv == -1) {
    return 1;
  }
perror("---zhang:out7");
  /* OOM happened */
  return 0;
}
