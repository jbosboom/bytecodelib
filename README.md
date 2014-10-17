bytecodelib
===========

bytecodelib is a JVM bytecode manipulation library with an LLVM-inspired
API, built on top of the [ASM](http://asm.ow2.org/) library.  I built it
because working with SSA form is much easier than managing the operand
stack and locals manually.

It has many deficiencies, both obvious (exception handlers are not
modeled) and subtle (`invokespecial` is not modeled, except for calling
superclass constructors).  But for what it does do, it's easier to use
than ASM.

Building
--------

`ant fetch; ant jar`
