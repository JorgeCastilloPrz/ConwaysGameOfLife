import GenerationCount.Finite
import arrow.core.some
import arrow.mtl.run
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import io.kotlintest.specs.StringSpec
import org.junit.Assert.*

class GameOfLifeSpec : StringSpec({

    "cell position is accurate" {
        val cell = Cell(false)
        val universe = listOf(
            listOf(Cell(false), Cell(true), Cell(false)),
            listOf(Cell(true), Cell(true), Cell(false)),
            listOf(Cell(false), cell, Cell(false))
        )

        assertEquals(Position(2, 1).some(), universe.cellPosition(cell))
    }

    "cell neighbours are accurate" {
        val cell = Cell(false)

        val universe = listOf(
            listOf(Cell(false), Cell(false), Cell(false)),
            listOf(Cell(true), Cell(true), Cell(true)),
            listOf(Cell(true), cell, Cell(true))
        )

        assertEquals(
            listOf(Position(1, 0), Position(1, 1), Position(1, 2), Position(2, 0), Position(2, 2)).map { it.some() },
            cell.neighbours(universe).map { universe.cellPosition(it) }
        )
    }

    "Any live cell with two or three neighbors survives" {
        forAll(Gen.choose(2, 3)) { aliveNeighbours ->
            val initialSeed: List<List<Cell>> = when (aliveNeighbours) {
                2 -> listOf(
                    listOf(Cell(false), Cell(true), Cell(false)),
                    listOf(Cell(true), Cell(true), Cell(false)),
                    listOf(Cell(false), Cell(false), Cell(false))
                )
                else -> listOf(
                    listOf(Cell(false), Cell(true), Cell(false)),
                    listOf(Cell(true), Cell(true), Cell(false)),
                    listOf(Cell(true), Cell(false), Cell(false))
                )
            }

            val finalState = gameOfLife(Finite(1)).run(initialSeed).a

            finalState[1][1].isAlive
        }
    }

    "Any dead cell with three live neighbors becomes a live cell" {
        val initialSeed: List<List<Cell>> = listOf(
            listOf(Cell(false), Cell(true), Cell(false)),
            listOf(Cell(true), Cell(false), Cell(false)),
            listOf(Cell(true), Cell(false), Cell(false))
        )

        val finalState = gameOfLife(Finite(1)).run(initialSeed).a

        assertTrue(finalState[1][1].isAlive)
    }

    "Any live cell with fewer than two live neighbours dies, as if by underpopulation." {
        val initialSeed: List<List<Cell>> = listOf(
            listOf(Cell(false), Cell(false), Cell(false)),
            listOf(Cell(true), Cell(true), Cell(false)),
            listOf(Cell(false), Cell(false), Cell(false))
        )

        val finalState = gameOfLife(Finite(1)).run(initialSeed).a

        assertFalse(finalState[1][1].isAlive)
    }

    "Any live cell with more than three live neighbours dies, as if by overpopulation." {
        val initialSeed: List<List<Cell>> = listOf(
            listOf(Cell(false), Cell(false), Cell(false)),
            listOf(Cell(true), Cell(true), Cell(true)),
            listOf(Cell(true), Cell(false), Cell(true))
        )

        val finalState = gameOfLife(Finite(1)).run(initialSeed).a

        assertFalse(finalState[1][1].isAlive)
    }
})
