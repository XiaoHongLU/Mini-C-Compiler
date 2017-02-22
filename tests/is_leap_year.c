#include "io.h"

void main() {
  int year;

  print_s("Enter year> ");
  year = read_i();

  print_i(year);
  if ( year % 400 == 0 ) print_s(" is a leap year.\n");
  else if ( year % 100 == 0) print_s(" is not a leap year.\n");
  else if ( year % 4 == 0 ) print_s(" is a leap year.\n");
  else print_s(" is not a leap year.\n");
}
