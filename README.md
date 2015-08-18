# ObliVM : A Programming Framework for Secure Computation (The compiler)

================================================================================

--------------------------------------------------------------------------------
Author
--------------------------------------------------------------------------------

The ObliVM compiler is developed and currently maintained by [Chang Liu].


--------------------------------------------------------------------------------
Disclaimer
--------------------------------------------------------------------------------

The code is a research-quality proof of concept, and is still under development for more features and bug-fixing.


--------------------------------------------------------------------------------
Prerequisit
--------------------------------------------------------------------------------

Oracle Java 8
JavaCC 5.0 (to compile C.jj under the parser folder only)
Python 2.7 (to run tools)

--------------------------------------------------------------------------------
Compile
--------------------------------------------------------------------------------

In Linux:

    $ ./compile.sh

In Cygwin

    $ ./compile_cygwin.sh

--------------------------------------------------------------------------------
Run the hamming distance example (examples/hamming)
--------------------------------------------------------------------------------
Compile the example:

    $ ./run-compiler.sh examples/hamming/hamming.lcc

In Cygwin

    $ ./run-compiler_cygwin.sh examples/hamming/hamming.lcc

Run the real secure computation:

    $ ./runtogether.sh examples/hamming/input_alice.txt examples/hamming/input_bob.txt 

In Cygwin

    $ ./runtogether_cygwin.sh examples/hamming/input_alice.txt examples/hamming/input_bob.txt 

--------------------------------------------------------------------------------
Tools
--------------------------------------------------------------------------------

Under tools/ folder, the following tools are available:

  * datainitilizer: initialize the bit input file from a file containing an integer or an array of integers

--------------------------------------------------------------------------------
References
--------------------------------------------------------------------------------

\[LWNYS12] [
  _ObliVM: A Programming Framework for Secure Computation_
] (http://www.cs.umd.edu/~liuchang/paper/oakland2015-oblivm.pdf)
  Chang Liu, Xiao Shuan Wang, Kartik Nayak, Yan Huang, Elaine Shi
  IEEE S&P 2012

[Chang Liu]: http://www.cs.umd.edu/~liuchang/
