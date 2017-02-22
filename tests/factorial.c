#include "io.h"

int factorial(int n) {
  int m;
  if (n == 0) {
    print_s("1");
    return 1;
  } else if (n == 1) {
    print_s("1");
    return 1;
  } else {
    print_i(n); print_s(" * ");
    m = n - 1;
    return n * factorial(m);
  }
}

void main() {
  int n;
  n = read_i();
  factorial(n);
  print_s("\n");
}
