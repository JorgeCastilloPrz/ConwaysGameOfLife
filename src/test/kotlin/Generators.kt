import io.kotlintest.properties.Gen

class CellGen : Gen<Cell> {
    override fun constants(): Iterable<Cell> = emptyList()

    override fun random(): Sequence<Cell> =
        generateSequence {
            val random = Gen.bool().random().first()
            if (random) Cell.Alive() else Cell.Dead()
        }
}

class UniverseGen(private val center: Cell, private val aliveNeighbors: Int) : Gen<Universe> {

    override fun constants(): Iterable<Universe> = emptyList()

    override fun random(): Sequence<Universe> = generateSequence {
        val alive = (1..aliveNeighbors).map { Cell.Alive() }.toList()
        val dead = (1..(8 - aliveNeighbors)).map { Cell.Dead() }.toList()
        val all = alive + dead

        all.shuffled().toGrid(center)
    }

    private fun List<Cell>.toGrid(center: Cell): List<List<Cell>> =
        listOf(
            listOf(get(0), get(1), get(2)),
            listOf(get(3), center, get(4)),
            listOf(get(5), get(6), get(7))
        )
}