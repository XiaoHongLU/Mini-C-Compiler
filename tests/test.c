#include "io.h"

void main()
{
	int sum[10];
	int i;
	i = 2;
	sum[0] = 1;
	sum[1] = 1;
	while(i<10)
	{
		sum[i] = sum[i-1] + sum[i-2];
		i = i +1;
	}
	i = 0;
	while(i<10)
	{
		print_i(sum[i]);
		i = i+1;
	}
}