package org.figsq.cobpasture.cobpasture.api.breedlogic.genetic

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.pokemon.Pokemon
import me.fullidle.ficore.ficore.common.bukkit.inventory.CraftItemStack
import net.minecraft.world.item.ItemStack
import org.figsq.cobpasture.cobpasture.api.breedlogic.MovesData
import org.figsq.cobpasture.cobpasture.api.breedlogic.SpeciesData
import kotlin.math.min

/**
 * 实现参考: https://wiki.52poke.com/wiki/%E8%9B%8B%E6%8B%9B%E5%BC%8F#%E4%BB%8E%E4%BA%B2%E4%BB%A3%E5%AD%A6%E4%B9%A0
 *
 * 并没有实现 连锁遗传
 */
object MoveGenetic : IGeneticHandler {
    override fun handle(parent1: Pokemon, parent2: Pokemon, egg: Pokemon) {
        val listOf = mutableListOf<MoveTemplate>()
        for (move in parent1.moveSet + parent2.moveSet) if (isEggMove(egg, move)) listOf.add(move.template)
        if (listOf.isEmpty()) return
        listOf.shuffle()
        for (i in 0 until min(MoveSet.MOVE_COUNT, listOf.size)) egg.moveSet.setMove(i, listOf[i].create())
        //     * 特殊的蛋招式：皮丘的伏特攻击需要父方或母方生蛋时持有电气球才能学会。（《绿宝石》起）
        //皮卡丘或雷丘携带电气球生蛋，孵化出的皮丘即会学会伏特攻击。
        tryLearnPPPMove(parent1,parent2, egg)
    }

    /**
     * 判断是否是 父母 皮卡丘/雷丘 蛋是否是皮丘 进行 是否学会伏特攻击
     */
    fun tryLearnPPPMove(parent1: Pokemon,parent2: Pokemon, egg: Pokemon) {
        if (
            egg.species != SpeciesData.PICHU ||
            (parent1.species.let { it != SpeciesData.RAICHU && it != SpeciesData.PIKACHU } &&
            parent2.species.let { it != SpeciesData.RAICHU && it != SpeciesData.PIKACHU }) ||
            !isLightBall(parent1.heldItem()) && !isLightBall(parent2.heldItem()) ||
            egg.moveSet.any { it.template == MovesData.VOLT_TACKLE }
            ) {
            return
        }
        egg.moveSet.setMove(0, MovesData.VOLT_TACKLE.create())
    }

    fun isLightBall(itemStack: ItemStack): Boolean {
        return CraftItemStack.asBukkitCopy(itemStack).type.name.contains("light_ball", true)
    }


    /**
     * 获取子代得蛋技能
     */
    fun isEggMove(egg: Pokemon, move: Move): Boolean {
        return egg.form.moves.eggMoves.contains(move.template)
    }
}
