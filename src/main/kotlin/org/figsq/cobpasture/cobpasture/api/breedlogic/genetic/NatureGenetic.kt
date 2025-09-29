package org.figsq.cobpasture.cobpasture.api.breedlogic.genetic

import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.helditem.CobblemonHeldItemManager
import org.figsq.cobpasture.cobpasture.CobPasture

/**
 * 参考： https://wiki.52poke.com/wiki/%E5%AE%9D%E5%8F%AF%E6%A2%A6%E5%9F%B9%E8%82%B2#%E6%80%A7%E6%A0%BC
 * 只实现了不变之石
 * 类名是性格遗传实际也有形态遗传
 */
object NatureGenetic : IGeneticHandler {
    override fun handle(
        parent1: Pokemon,
        parent2: Pokemon,
        egg: Pokemon
    ) {
        hasEverstone(parent1, parent2)?.let {
            tryConsume(it)
            egg.nature = it.nature
            if (it.form.labels.contains(CobblemonPokemonLabels.REGIONAL)) egg.form = it.form
        }
    }

    fun tryConsume(pokemon: Pokemon) {
        if (!everstoneConsume || !hasEverstone(pokemon)) return
        val copy = pokemon.heldItem().copy()
        copy.count--
        pokemon.swapHeldItem(copy,false)
    }

    fun hasEverstone(parent1: Pokemon, parent2: Pokemon): Pokemon? {
        if (hasEverstone(parent1)) return parent1
        if (hasEverstone(parent2)) return parent2
        return null
    }

    fun hasEverstone(pokemon: Pokemon): Boolean{
        return "everstone" == CobblemonHeldItemManager.showdownId(pokemon.heldItem())
    }


    var everstoneConsume: Boolean = true

    fun load(){
        everstoneConsume = CobPasture.INSTANCE.config.getBoolean("everstone-consume", true)
    }
}
