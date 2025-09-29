package org.figsq.cobpasture.cobpasture.api.breedlogic.genetic

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.helditem.CobblemonHeldItemManager
import org.figsq.cobpasture.cobpasture.CobPasture
import kotlin.random.Random

/**
 * 能力遗传
 * 参考: https://wiki.52poke.com/wiki/%E5%AE%9D%E5%8F%AF%E6%A2%A6%E5%9F%B9%E8%82%B2#%E8%83%BD%E5%8A%9B
 * #只实现了红线逻辑!
 * TODO 六代前的还没写
 */
object IVGenetic : IGeneticHandler {
    override fun handle(
        parent1: Pokemon,
        parent2: Pokemon,
        egg: Pokemon
    ) {
        //宝可梦是否拥有狗圈影响是否有红线的处理
        val stat1 = getPowerHeldStat(parent1)
        val stat2 = getPowerHeldStat(parent2)

        //都有狗圈
        if (stat1 != null && stat2 != null) {
            //各百分之50遗传
            if (Random.nextBoolean()) egg.setIV(stat1, parent1.ivs[stat1]!!)
            if (Random.nextBoolean()) egg.setIV(stat2, parent1.ivs[stat2]!!)
            return
        }

        val oneStat = stat1?.to(parent1) ?: stat2?.to(parent2)

        whoHasDestinyKnot(parent1, parent2)?.let {
            //其中一个拥有红线
            tryConsumeKnot(pokemon = it)

            val notGenetic = Stats.PERMANENT.random()

            oneStat?.let { it ->
                //有一个狗圈参与
                egg.setIV(it.first, it.second.ivs[it.first]!!)
                geneticIV(parent1,parent2, egg, notGenetic, it.first)
            } ?:
            //正常只有红线的遗传
            geneticIV(parent1, parent2, egg, notGenetic)
        }
    }

    fun tryConsumeKnot(pokemon: Pokemon) {
        if (!destinyknotConsume || !hasDestinyKnot(pokemon)) return
        val copy = pokemon.heldItem().copy()
        copy.count--
        pokemon.swapHeldItem(copy, false)
    }

    /**
     * 遗传5个个体从父母宝可梦随机，排除其中一个
     * @param notGenetic 排除的一项
     * @param parent1 父母宝可梦
     * @param parent2 父母宝可梦
     * @param target 目标宝可梦
     */
    fun geneticIV(parent1: Pokemon, parent2: Pokemon, target: Pokemon,vararg notGenetic: Stat) {
        for (stat in Stats.PERMANENT) {
            if (notGenetic.contains(stat)) continue
            target.setIV(stat, if (Random.nextBoolean()) parent1.ivs[stat]!! else parent2.ivs[stat]!!)
        }
    }

    /**
     * 返回是哪知得红线生效了，多个也只会有一个
     * @return 没有宝可梦拥有则返回空
     */
    fun whoHasDestinyKnot(parent1: Pokemon, parent2: Pokemon): Pokemon? {
        return if (hasDestinyKnot(parent1)) parent1 else
            if (hasDestinyKnot(parent2)) parent2 else null
    }

    /**
     * 拥有狗圈时
     */
    fun getPowerHeldStat(pokemon: Pokemon): Stat? {
        return powerHeldItemMap[CobblemonHeldItemManager.showdownId(pokemon.heldItem())]
    }

    fun hasDestinyKnot(pokemon: Pokemon): Boolean {
        return "destinyknot" == CobblemonHeldItemManager.showdownId(pokemon.heldItem())
    }

    //默认消耗
    var destinyknotConsume: Boolean = true
    val powerHeldItemMap = mutableMapOf<String, Stat>(
        CobblemonHeldItemManager.showdownIdOf(CobblemonItems.POWER_WEIGHT)!! to Stats.HP,
        CobblemonHeldItemManager.showdownIdOf(CobblemonItems.POWER_BRACER)!! to Stats.ATTACK,
        CobblemonHeldItemManager.showdownIdOf(CobblemonItems.POWER_LENS)!! to Stats.SPECIAL_ATTACK,
        CobblemonHeldItemManager.showdownIdOf(CobblemonItems.POWER_BELT)!! to Stats.DEFENCE,
        CobblemonHeldItemManager.showdownIdOf(CobblemonItems.POWER_BAND)!! to Stats.SPECIAL_DEFENCE,
        CobblemonHeldItemManager.showdownIdOf(CobblemonItems.POWER_ANKLET)!! to Stats.SPEED

    )

    //加载配置所需项
    fun load() {
        destinyknotConsume = CobPasture.INSTANCE.config.getBoolean("destinyknot-consume")
    }
}
