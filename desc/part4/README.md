# Part IV : Introduction to LLVM
The goal of part IV is to write a simple LLVM pass.
If you have no previous experience with C++, we suggest that you take a look at these [C++ for Java programmers](https://www.cs.cmu.edu/afs/cs/academic/class/15494-s12/lectures/c++forjava.pdf) slides.

The project counts for 30% of your grade: 15% for writing a simple dead code elimination pass using the skeleton code provided, and 15% for implementing liveness and writing your own method to determine dead code.

## 0. Setup

You will use Git to clone the LLVM sources and Cmake to generate the Makefiles to build LLVM on Linux. The version of Cmake installed in /usr/bin on DICE is too old to use 
with LLVM. You need a minimum of Cmake 3.4 or later. If you already have Cmake installed (for example on your own machine) you can skip this step. Otherwise, the first step 
towards building LLVM is to update your path to point to the latest version of Cmake which is installed in my home directory.

Append the path to Cmake to the beginning of your existing path so that when you run Cmake you will pick up the new version and not the 
old one installed in /usr/bin. 

```
export PATH=/afs/inf.ed.ac.uk/user/v/v1asmi18/cmake-3.7.0-Linux-x86_64/bin:$PATH 
```

Check that you have version 3.7 of Cmake in your path (or version 3.4 or later if you are using your own machine).

```
cmake --version
```

Now that you have the correct version of Cmake, clone the LLVM and Clang sources into your home directory. You will need about 1.6GB of 
free disk space. The sources are available over AFS from my DICE home directory (or you can clone from github but that's not covered 
here).

```
cd ~
git clone /afs/inf.ed.ac.uk/user/v/v1asmi18/ug3-compilers/llvm llvm
git clone /afs/inf.ed.ac.uk/user/v/v1asmi18/ug3-compilers/llvm/tools/clang llvm/tools/clang
```

Create a directory in /tmp to build LLVM and change to this directory.

```
mkdir -p /tmp/$USER/build
cd /tmp/$USER/build
```

Now run Cmake to create the Makefiles to build LLVM. The Debug build of LLVM requires around 10GBs of disk space! To save space we will build 
the "Minimum Size Release" and only the x86 target, which requires about 1GB of free space. We'll build in /tmp to make sure you 
don't fill up your home directory.


```
cmake -DLLVM_TARGETS_TO_BUILD=X86 -DCMAKE_BUILD_TYPE=MinSizeRel ~/llvm
```

After Cmake finishes creating the Makefiles the next step is to actually build LLVM. This can take anywhere from 10-30 minutes depending 
on your machine.

```
make -j8
```

After make finishes you will have a bin directory with the LLVM tools (clang, clang++, llc, etc). Try to compile a simple C example with 
Clang to LLVM bitcode to make sure it works.

```
echo "int main() { return 1; }" > test.c
/tmp/$USER/build/bin/clang -c -S -emit-llvm test.c -o test.ll
cat test.ll
```

Your test.ll should look SOMETHING like this (this output is from Mac OS X),

```
; ModuleID = 'test.c'
source_filename = "test.c"
target datalayout = "e-m:o-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.12.0"

; Function Attrs: nounwind ssp uwtable
define i32 @main() #0 {
  %1 = alloca i32, align 4
  store i32 0, i32* %1, align 4
  ret i32 1
}

attributes #0 = { nounwind ssp uwtable "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="penryn" "target-features"="+cx16,+fxsr,+mmx,+sse,+sse2,+sse3,+sse4.1,+ssse3" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.module.flags = !{!0}
!llvm.ident = !{!1}

!0 = !{i32 1, !"PIC Level", i32 2}
!1 = !{!"Apple LLVM version 8.0.0 (clang-800.0.42.1)"}
```

Congratulations you have just built LLVM!


## 1. Writing a Skeleton LLVM Pass

You have LLVM and are able to compile C programs. The next step is to create a pass of your own. 
Adrian Sampson at Cornell has put a simple skeleton pass online that you can use as a starting point. 
Change to your home directory and clone Adrian's git repository with the code.

```
cd ~
git clone https://github.com/sampsyo/llvm-pass-skeleton.git
```

Change to the directory for the skeleton pass and take a look at the source. It does nothing except 
print the name of whatever function it encounters.

```
cd llvm-pass-skeleton
less skeleton\Skeleton.cpp
```

Let's build the pass now. Create a build directory and change to the directory.

```
mkdir build
cd build
```

Before running Cmake and building the pass, you need to set LLVM_DIR to your LLVM build directory in 
/tmp. Otherwise when you build the skeleton pass it will try to build with the version of LLVM that 
is already installed on DICE and fail.

```
export LLVM_DIR=/tmp/$USER/build
```

You're ready to create the Makefiles and build the pass.

```
cmake ..
make
```

There should be a shared library for your new pass in skeleton/libSkeletonPass.so. When you compile a 
program with LLVM it will load your pass and automatically call it. You'll need a C file to use as a 
test. You can use the test.c you created in Step 0 or create a new file.

```
/tmp/$USER/build/bin/clang -Xclang -load -Xclang skeleton/libSkeletonPass.so /tmp/$USER/build/test.c
I saw a function called main!
```

Congratulations you've just created an LLVM pass and successfully executed it with your own LLVM!


## 2. Write a Pass to Count Instructions

Use the skeleton pass above and the [lecture notes from 
class](http://www.inf.ed.ac.uk/teaching/courses/ct/slides-16-17/llvm/4-lab3_intro.pdf) to write a simple pass to print the number of 
instructions in a function. 

NOTE!! LLVM will run your pass on EACH function. You do not need to use any module iterators. Only function and basic block iterators.

The lecture notes are for an older version of LLVM and you need to make the following changes:

Slide 5: Include the C++ header for vector with the other includes and add namespace std so the compiler can find vector.

```
#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include <vector>
using namespace llvm;
using namespace std;
```

For your project you only need the code from Slide 5. If you would like to try out the loop analysis code on Slide 17 you need to make the following 
change. This is completely optional for your project.

Slide 17: The getAnalysis() method to access the LoopInfo structure in runOnFunction() has changed. Here is the correct definition.

```
LoopInfo &LI = getAnalysis<LoopInfoWrapperPass>().getLoopInfo();
```

## 3. Implement a Simple Dead Code Elimination Pass

Add a new method to your instruction counting pass to eliminate dead code. In the C program below, 'd' is dead because it is not used after it's assignment in the program. The assignment to 'c' is dead 
because it's only use is in the assignment to 'd' which is dead.

```
int foo() {
  int a = 7;
  int b = a * 2;
  int c = b - a;   // dead 
  int d = c / a;   // dead
  return b;
}
```

LLVM has a method to detect dead code and a method to remove instructions that you can use in your pass.

```
isInstructionTriviallyDead()
eraseFromParent()
```

You will use the LLVM iterators we discussed in class to find the dead instructions. It is illegal to remove an instruction while 
you are iterating over them. You need to first identify the instructions that are dead and then in a second loop remove them. Use 
the LLVM SmallVector data structure to store the dead instructions you find while iterating and a second loop to remove them. 

```
SmallVector<Instruction*, 64> Worklist;
```

You need to run LLVM's 'mem2reg' pass before your DCE pass to convert the bitcode into a form that will work with your optimization. 
Without running 'mem2reg' all instructions will store their destinations operands to the stack and load their source operands from 
the stack. The memory instructions will block the ability for you to discover dead code. When you run 'mem2reg', you are converting 
the stack allocated code in non-SSA form, into SSA form with virtual registers.

Use the 'opt' tool to run 'mem2reg' before your DCE pass. Give your pass a command line option called 'mydce'.

```
/tmp/$USER/build/bin/clang -emit-llvm -S dead.c
/tmp/$USER/build/bin/opt -load skeleton/libSkeletonPass.so -mem2reg -mydce dead.ll
``` 

## 4. Implement Iterative Liveness Analysis

For the last part of your project you will replace the isInstructionTriviallyDead() method from LLVM with your own method to identify dead code. This 
relies on computing liveness which you learned about in [Lecture 15](http://www.inf.ed.ac.uk/teaching/courses/ct/slides-16-17/15-regalloc.pdf).

## 5. Submitting Your Project

NOTE: THIS INSTRUCTIONS ARE A WORK IN PROGRESS - LIABLE TO CHANGE AT ANY MOMENT, AND NOT COMPLETE!

### 5.1 Getting started

Two skeletons have been provided for you. The first for the simple dead code elimination by using the llvm live variable analyisis can be found in
part-4/llvm-pass-simple-dce. 

The second is for the dead code implementation with your own liveness analysis, and can be found in part-4/llvm-pass-my-dce. Remember that your are not
allowed to use the live variable analysis provided by LLVM in this second implementation!

In both cases, your are to modify the skeleton/Skeleton.cpp source file. Do not modify the directory structure, any other file, and do not change the LLVM
pass registration names, as the testing suite relies on the supplied configuration to function properly.

You can build and run your simple pass with these commands

Build:

```
$ cd part-4/llvm-pass-simple-dce
$ mkdir build
$ cd build
$ cmake ..
$ make
$ cd ..
```

Run:

```
$ /tmp/$USER/build/bin/opt -load build/skeleton/libSkeletonPass.so -mem2reg -simpledce dead.ll
```
For the advanced dce, use

Build:

```
$ cd part-4/llvm-pass-my-dce
$ mkdir build
$ cd build
$ cmake ..
$ make
$ cd ..
```

Run:

```
$ /tmp/$USER/build/bin/opt -load build/skeleton/libSkeletonPass.so -mem2reg -mydce dead.ll
```
### 5.2 Expected output

For both passes, you are expected to produce an output summarizing the effect that the dead code elimantion
had on the supplied bitcode. Your output must conform to the following example:

```
BEFORE DCE
add: 4
br: 10
DCE START
add: 2
br: 4
DCE END
```

Note that the instruction names ```add``` and ```br``` are only for demonstration purposes: you will have
more instructions, and the counts will vary. Note that it is very important that each entry is on its own line.


