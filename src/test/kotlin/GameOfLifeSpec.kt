import GenerationCount.Finite
import arrow.core.some
import arrow.mtl.run
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import io.kotlintest.specs.StringSpec
import org.junit.Assert.*

class GameOfLifeSpec : StringSpec({

    "cell position is accurate" {
        val cell = Cell.Dead()
        val universe = listOf(
            listOf(Cell.Dead(), Cell.Alive(), Cell.Dead()),
            listOf(Cell.Alive(), Cell.Alive(), Cell.Dead()),
            listOf(Cell.Dead(), cell, Cell.Dead())
        )

        assertEquals(Position(2, 1).some(), universe.cellPosition(cell))
    }

    "cell neighbours are accurate" {
        val cell = Cell.Dead()

        val universe = listOf(
            listOf(Cell.Dead(), Cell.Dead(), Cell.Dead()),
            listOf(Cell.Alive(), Cell.Alive(), Cell.Alive()),
            listOf(Cell.Alive(), cell, Cell.Alive())
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
                    listOf(Cell.Dead(), Cell.Alive(), Cell.Dead()),
                    listOf(Cell.Alive(), Cell.Alive(), Cell.Dead()),
                    listOf(Cell.Dead(), Cell.Dead(), Cell.Dead())
                )
                else -> listOf(
                    listOf(Cell.Dead(), Cell.Alive(), Cell.Dead()),
                    listOf(Cell.Alive(), Cell.Alive(), Cell.Dead()),
                    listOf(Cell.Alive(), Cell.Dead(), Cell.Dead())
                )
            }

            val finalState = gameOfLife(Finite(1)).run(initialSeed).a

            finalState[1][1].isAlive()
        }
    }

    "Any dead cell with three live neighbors becomes a live cell" {
        val initialSeed: List<List<Cell>> = listOf(
            listOf(Cell.Dead(), Cell.Alive(), Cell.Dead()),
            listOf(Cell.Alive(), Cell.Dead(), Cell.Dead()),
            listOf(Cell.Alive(), Cell.Dead(), Cell.Dead())
        )

        val finalState = gameOfLife(Finite(1)).run(initialSeed).a

        assertTrue(finalState[1][1].isAlive())
    }

    "Any live cell with fewer than two live neighbours dies, as if by underpopulation." {
        val initialSeed: List<List<Cell>> = listOf(
            listOf(Cell.Dead(), Cell.Dead(), Cell.Dead()),
            listOf(Cell.Alive(), Cell.Alive(), Cell.Dead()),
            listOf(Cell.Dead(), Cell.Dead(), Cell.Dead())
        )

        val finalState = gameOfLife(Finite(1)).run(initialSeed).a

        assertFalse(finalState[1][1].isAlive())
    }

    "Any live cell with more than three live neighbours dies, as if by overpopulation." {
        val initialSeed: List<List<Cell>> = listOf(
            listOf(Cell.Dead(), Cell.Dead(), Cell.Dead()),
            listOf(Cell.Alive(), Cell.Alive(), Cell.Alive()),
            listOf(Cell.Alive(), Cell.Dead(), Cell.Alive())
        )

        val finalState = gameOfLife(Finite(1)).run(initialSeed).a

        assertFalse(finalState[1][1].isAlive())
    }
})
