#include "io.h"

int a[10];

int bs1(int c, int l, int h)
{
    if(l > h){
        return 0;
    }
    if(l == h){
        if(a[l] == c){
            return 1;
        }
        else{
            return 0;
        }
    }
    if(a[(l+h)/2] == c){
        return 1;
    }
    if(a[(l+h)/2] > c){
        return bs1(c, l, (l+h)/2);
    }
    if(a[(l+h)/2]< c){
        return bs1(c, (l+h)/2+1, h);
    }
    return 0;   
}

int bs2(int c, int l, int h){
    if(l > h){
        return 0;
    }
    while(l != h){
        if(a[(l+h)/2] == c){
            return 1;
        }
        if(a[(l+h)/2]> c){
            h = (l+h)/2;
        }
        else{
            l = (l+h)/2;
        }
    }
    if(a[l] == c){
        return 1;
    }
    else{
        return 0;
    }
    return 0;
}

int main()
{
    int i;
    int c;
    i = 0;
    c = 1;
    while(i < 10){
        a[i] = c;
        i = i + 1;
        c = read_i();
    }
    print_i(bs1(c,c,c));
    print_i(bs2(c,c,c));
    return 0;
}