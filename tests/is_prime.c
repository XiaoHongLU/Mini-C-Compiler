#include "io.h"

int is_prime(int n) {
  int i; int flag;
  i = 2;
  flag = 1;
  while (i < n) {
    if (n % i == 0) flag = 0;
    i = i + 1;
  }
  return flag;
}

void main() {
  int n;
  n = read_i();
  print_i(n); print_s(" is ");
  if (is_prime(n) == 0) {
    print_s("not ");
  }
  print_s("prime.\n");
}
