package com.alekseyzhelo.lbm.core.lattice

import com.alekseyzhelo.lbm.core.cell.CellD2Q9
import com.alekseyzhelo.lbm.core.dynamics.Dynamics2DQ9

/**
 * @author Aleks on 17-05-2016.
 */
// TODO: initialization
// TODO: some kind of statistics, output, etc
// TODO: units
// TODO?: boundary conditions
// TODO?: solids, etc
class LatticeD2Q9(val LX: Int, val LY: Int, val dynamics: Dynamics2DQ9) {

    val cells = Array(LX, { x -> Array(LY, { x -> CellD2Q9() }) })

    fun streamPeriodic(): Unit {
        for (i in cells.indices) {
            val iSub = if (i > 0) (i - 1) else (LX - 1)
            val iPlus = if (i < LX - 1) (i + 1) else (0)
            for (j in cells[i].indices) { // TODO performance?
                val jSub = if (j > 0) (j - 1) else (LY - 1)
                val jPlus = if (j < LY - 1) (j + 1) else (0)
                // TODO if (!is_interior_solid_node[i][j]) {
                cells[i][j].fBuf[0] = cells[i][j].f[0];
                cells[iPlus][j].fBuf[1] = cells[i][j].f[1];
                cells[i][jPlus].fBuf[2] = cells[i][j].f[2];
                cells[iSub][j].fBuf[3] = cells[i][j].f[3];
                cells[i][jSub].fBuf[4] = cells[i][j].f[4];
                cells[iPlus][jPlus].fBuf[5] = cells[i][j].f[5];
                cells[iSub][jPlus].fBuf[6] = cells[i][j].f[6];
                cells[iSub][jSub].fBuf[7] = cells[i][j].f[7];
                cells[iPlus][jSub].fBuf[8] = cells[i][j].f[8];
                // TODO }
            }
        }
    }

    fun collide(): Unit {
        for (i in cells.indices) {
            for (j in cells[i].indices) { // TODO performance?
                dynamics.collide(cells[i][j])
            }
        }
    }

    override fun toString(): String {
        return buildString {
            appendln("LX: $LX, LY: $LY")
            appendln("Dynamics: $dynamics")
        }
    }

}