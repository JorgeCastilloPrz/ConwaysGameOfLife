# Conway's Game of Life

Conway's Game of Life is an evolutionary game that requires no user input other than the initial state of the board. The game consists of a grid of cells that can either be alive or dead and follow a few rules to determine their next generation. These rules are:

- Any live cell with fewer than two live neighbours dies, as if by underpopulation. 
- Any live cell with two or three live neighbours lives on to the next generation.
- Any live cell with more than three live neighbours dies, as if by overpopulation.
- Any **dead** cell with exactly three live neighbours becomes a live cell, as if by reproduction.

These rules, which compare the behavior of the automaton to real life, can be condensed into the following:

- Any live cell with two or three neighbors survives.
- Any dead cell with three live neighbors becomes a live cell.
- All other live cells die in the next generation. Similarly, all other dead cells stay dead.

The initial pattern constitutes the seed of the system. The first generation is created by applying the above rules simultaneously to every cell in the seed; births and deaths occur simultaneously, and the discrete moment at which this happens is sometimes called a tick. Each generation is a pure function of the preceding one. The rules continue to be applied repeatedly to create further generations.

This repository contains a solution that:

- Uses [Arrow](https://arrow-kt.io/), a library for FP written in Kotlin.
- Uses [KotlinTest](https://github.com/kotlintest/kotlintest), a library for Property-based Testing written in Kotlin, to test the solution.

There Swift counter part using Bow [is here](https://github.com/truizlop/ConwaysGameOfLife).
