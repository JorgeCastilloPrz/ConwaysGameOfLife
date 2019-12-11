@file:Suppress("CanSealedSubClassBeObject")

import GenerationCount.Finite
import GenerationCount.Infinite
import arrow.core.*
import arrow.core.extensions.list.functorFilter.filterMap
import arrow.core.extensions.list.semigroupal.times
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.mtl.StateT

sealed class Cell {
    class Alive : Cell()
    class Dead : Cell()
}

fun Cell.isDead() = !isAlive()
fun Cell.isAlive() = this is Cell.Alive

typealias Universe = List<List<Cell>>

private val Universe.sizeX: Int get() = size

private val Universe.sizeY: Int get() = get(0).size

data class Position(val x: Int, val y: Int)

fun Option<Position>.shiftBy(universe: Universe, delta: Tuple2<Int, Int>): Option<Position> =
    flatMap { pos ->
        val shifted = pos.copy(x = pos.x + delta.a, y = pos.y + delta.b)
        if (shifted.x in 0 until universe.sizeX && shifted.y in 0 until universe.sizeY) {
            shifted.some()
        } else {
            None
        }
    }

fun Universe.cellPosition(cell: Cell): Option<Position> =
    this.flatten().indexOf(cell).let { index ->
        if (index == -1) {
            None
        } else {
            val x = index / sizeX
            val y = index % sizeX
            Some(Position(x, y))
        }
    }

fun Cell.neighbours(universe: Universe): List<Cell> {
    val allDeltaCombinations = listOf(-1, 0, 1) * listOf(-1, 0, 1) // ListK cartesian product to get all combinations
    val deltas = allDeltaCombinations - Tuple2(0, 0)
    return deltas
        .filterMap { universe.cellPosition(this).shiftBy(universe, it) }
        .map { universe[it.x][it.y] }
}

private fun Cell.aliveNeighbours(universe: Universe): List<Cell> = neighbours(universe).filter { it is Cell.Alive }

private fun Universe.tick(): Tuple2<Universe, Unit> {
    val newGeneration = this.map { column ->
        column.map { cell ->
            val aliveNeighbors = cell.aliveNeighbours(this).size
            when {
                aliveNeighbors < 2 || aliveNeighbors > 3 -> Cell.Dead()
                aliveNeighbors == 3 && cell.isDead() -> Cell.Alive()
                else -> Cell.Alive()
            }
        }.k()
    }.k()
    return newGeneration toT Unit
}

sealed class GenerationCount {
    object Infinite : GenerationCount()
    data class Finite(val count: Int) : GenerationCount()
}

/**
 * StateT is pure since it defers the execution until you call run. Once we do it, it'll become unsafe, that's why we've
 * picked StateT over IO.
 */
fun gameOfLife(
    maxGenerations: GenerationCount = Infinite,
    currentGeneration: Int = 0
): StateT<ForIO, Universe, Unit> =
    StateT(IO.monad()) { universe: Universe ->
        IO {
            println(universe)
            universe.tick()
        }
    }.flatMap(IO.monad()) {
        when (maxGenerations) {
            is Infinite -> gameOfLife(maxGenerations, currentGeneration + 1)
            is Finite -> if (currentGeneration < maxGenerations.count - 1) {
                gameOfLife(maxGenerations, currentGeneration + 1)
            } else {
                StateT.just(IO.monad(), it)
            }
        }
    }

fun main() {
    // State provides a convenient run method to run it using an initial state.
    gameOfLife(Finite(3)).run(IO.monad(), initialSeed())
}

private fun initialSeed(): List<List<Cell>> =
    listOf(
        listOf(Cell.Dead(), Cell.Alive(), Cell.Dead()),
        listOf(Cell.Alive(), Cell.Dead(), Cell.Alive()),
        listOf(Cell.Dead(), Cell.Alive(), Cell.Dead())
    )
