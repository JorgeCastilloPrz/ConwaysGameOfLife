import GenerationCount.Finite
import arrow.core.some
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
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
        forAll(universeGenWith2or3AliveNeighbours(Cell.Alive())) { universe ->
            val finalState = gameOfLife(Finite(1)).run(IO.monad(), universe).fix().unsafeRunSync().a

            finalState[1][1].isAlive()
        }
    }

    "Any dead cell with three live neighbors becomes a live cell" {
        forAll(UniverseGen(center = Cell.Dead(), aliveNeighbors = 3)) { universe ->
            val finalState = gameOfLife(Finite(1)).run(IO.monad(), universe).fix().unsafeRunSync().a

            finalState[1][1].isAlive()
        }
    }

    "Any live cell with fewer than two live neighbours dies, as if by underpopulation." {
        forAll(universeGenFewerThan2AliveNeighbours(Cell.Alive())) { universe ->
            val finalState = gameOfLife(Finite(1)).run(IO.monad(), universe).fix().unsafeRunSync().a

            finalState[1][1].isDead()
        }
    }

    "Any live cell with more than three live neighbours dies, as if by overpopulation." {
        forAll(universeGenMoreThan3AliveNeighbours(Cell.Alive())) { universe ->
            val finalState = gameOfLife(Finite(1)).run(IO.monad(), universe).fix().unsafeRunSync().a

            finalState[1][1].isDead()
        }
    }
})
