// Solve Towers of Hanoi with three pillars and n disks.
// This program prints the solution trace.
// Written by Daniel Hillerström
#include "io.h"

void solve_toh(int ndisks, char a, char b, char c) {
  if (ndisks > 0) {
    ndisks = ndisks - 1;
    solve_toh(ndisks, a, c, b);
    print_c(a); print_s(" -> "); print_c(b); print_s("\n");
    solve_toh(ndisks, c, b, a);
  }
}

void main() {
  int ndisks;
  char a; char b; char c;
  a = 'A'; b = 'B'; c = 'C';
  print_s("Enter number of disks> "); ndisks = read_i();
  solve_toh(ndisks, a, b, c);
}
