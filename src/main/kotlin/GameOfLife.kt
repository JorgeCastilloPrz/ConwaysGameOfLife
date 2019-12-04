import arrow.core.*
import arrow.core.extensions.list.semigroupal.times
import arrow.mtl.State
import arrow.mtl.run
import arrow.typeclasses.internal.IdBimonad

private data class Cell(val isAlive: Boolean)

private fun Cell.isDead() = !isAlive

private typealias Universe = List<List<Cell>>

private val Universe.sizeX: Int get() = size

private val Universe.sizeY: Int get() = get(0).size

private data class Position(val x: Int, val y: Int)

private fun Universe.cellPosition(cell: Cell): Option<Position> =
    this.flatten().indexOf(cell).let { index ->
        if (index == -1) {
            None
        } else {
            val x = index / sizeY
            val y = index / sizeX
            Some(Position(x, y))
        }
    }

// Neighbors

private fun Cell.neighbours(universe: Universe): List<Cell> {
    val deltas = listOf(-1, 0, 1) * listOf(-1, 0, 1) // ListK cartesian product to get all combinations
    return deltas
        .map { Position(it.a, it.b) }
        .filter { pos ->
            pos.x in 0 until universe.sizeX &&
                    pos.y in 0 until universe.sizeY &&
                    pos.some() != universe.cellPosition(this)
        }
        .map { universe[it.x][it.y] }
}

private fun Cell.aliveNeighbours(universe: Universe): List<Cell> = neighbours(universe).filter { it.isAlive }

private fun Universe.tick(): Tuple2<Universe, Unit> {
    val newGeneration = this.map { column ->
        column.map { cell ->
            val aliveNeighbors = cell.aliveNeighbours(this).size
            when {
                aliveNeighbors < 2 -> Cell(isAlive = false)
                aliveNeighbors > 3 -> Cell(isAlive = false)
                aliveNeighbors == 3 && cell.isDead() -> Cell(isAlive = true)
                else -> Cell(isAlive = true)
            }
        }.k()
    }.k()
    return Tuple2(newGeneration, Unit)
}

private fun gameOfLife(): State<Universe, Unit> =
    State { universe: Universe ->
        println(universe)
        universe.tick()
    }.flatMap(IdBimonad) {
        Thread.sleep(1000)
        gameOfLife()
    }

fun main() {
    // State provides a convenient run method to run it using an initial state.
    gameOfLife().run(initialSeed())
}

private fun initialSeed(): List<List<Cell>> =
    listOf(
        listOf(Cell(false), Cell(true)),
        listOf(Cell(false), Cell(false))
    )
