package tower;

import arc.Events;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.ctype.MappableContent;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.Plugin;
import mindustry.net.Administration.ActionType;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.defense.ShockMine;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.UnitBlock;
import mindustry.world.meta.BlockFlag;

import java.awt.Color;

import static arc.util.Strings.kebabToCamel;
import static mindustry.Vars.*;
import static mindustry.content.Blocks.*;
import static mindustry.content.Fx.*;
import static mindustry.content.Items.*;
import static mindustry.content.StatusEffects.disarmed;
import static mindustry.content.UnitTypes.*;
import static mindustry.type.ItemStack.with;

public class Main extends Plugin {

    public static ObjectMap<UnitType, ItemStack[]> drops;
    public static float multiplier = 1f;

    public static char getIcon(MappableContent content) {
        try {
            return Reflect.get(Iconc.class, kebabToCamel(content.getContentType().name() + "-" + content.name));
        } catch (Exception e) {
            return '?';
        }
    }

    public static String trafficLightColor(float value) {
        return Integer.toHexString(Color.HSBtoRGB(value / 3f, 1f, 1f)).substring(2);
    }

    public static boolean isPath(Tile tile) {
        return tile.floor() == darkPanel4 || tile.floor() == darkPanel5;
    }

    public static boolean proximityCheck(Tile tile, Block block) {
        return !tile.getLinkedTilesAs(block, new Seq<>()).contains(Main::isPath);
    }

    @Override
    public void init() {
        drops = ObjectMap.of(
                crawler, with(copper, 20, lead, 10, silicon, 3),
                atrax, with(copper, 30, lead, 40, graphite, 10, titanium, 5),
                spiroct, with(lead, 100, graphite, 40, silicon, 40, thorium, 10),
                arkyid, with(copper, 300, graphite, 80, metaglass, 80, titanium, 80, thorium, 20, phaseFabric, 10),
                toxopid, with(copper, 400, lead, 400, graphite, 120, silicon, 120, thorium, 40, plastanium, 40, surgeAlloy, 15, phaseFabric, 5),

                dagger, with(copper, 20, lead, 10, silicon, 3),
                mace, with(copper, 30, lead, 40, graphite, 10, titanium, 5),
                fortress, with(lead, 100, graphite, 40, silicon, 40, thorium, 10),
                scepter, with(copper, 300, silicon, 80, metaglass, 80, titanium, 80, thorium, 20, phaseFabric, 10),
                reign, with(copper, 400, lead, 400, graphite, 120, silicon, 120, thorium, 40, plastanium, 40, surgeAlloy, 15, phaseFabric, 5),

                nova, with(copper, 20, lead, 10, metaglass, 3),
                pulsar, with(copper, 30, lead, 40, metaglass, 10),
                quasar, with(lead, 100, metaglass, 40, silicon, 40, titanium, 80, thorium, 10),
                vela, with(copper, 300, metaglass, 80, graphite, 80, titanium, 60, plastanium, 20, surgeAlloy, 5),
                corvus, with(copper, 400, lead, 400, graphite, 100, silicon, 100, metaglass, 120, titanium, 120, thorium, 60, surgeAlloy, 10, phaseFabric, 10),

                flare, with(copper, 20, lead, 10, graphite, 3),
                horizon, with(copper, 30, lead, 40, graphite, 10),
                zenith, with(lead, 100, graphite, 40, silicon, 40, titanium, 30, plastanium, 10),
                antumbra, with(copper, 300, graphite, 80, metaglass, 80, titanium, 60, surgeAlloy, 15),
                eclipse, with(copper, 400, lead, 400, graphite, 120, silicon, 120, titanium, 120, thorium, 40, plastanium, 40, surgeAlloy, 5, phaseFabric, 10),

                mono, with(copper, 20, lead, 10, silicon, 3),
                poly, with(copper, 30, lead, 40, silicon, 10, titanium, 5),
                mega, with(lead, 100, silicon, 40, graphite, 40, thorium, 10),
                quad, with(copper, 300, silicon, 80, metaglass, 80, titanium, 80, thorium, 20, phaseFabric, 10),
                oct, with(copper, 400, lead, 400, graphite, 120, silicon, 120, thorium, 40, plastanium, 40, surgeAlloy, 15, phaseFabric, 5),

                risso, with(copper, 20, lead, 10, metaglass, 3),
                minke, with(copper, 30, lead, 40, metaglass, 10),
                bryde, with(lead, 100, metaglass, 40, silicon, 40, titanium, 80, thorium, 10),
                sei, with(copper, 300, metaglass, 80, graphite, 80, titanium, 60, plastanium, 20, surgeAlloy, 5),
                omura, with(copper, 400, lead, 400, graphite, 100, silicon, 100, metaglass, 120, titanium, 120, thorium, 60, surgeAlloy, 10, phaseFabric, 10),

                retusa, with(copper, 20, lead, 10, metaglass, 3),
                oxynoe, with(copper, 30, lead, 40, metaglass, 10),
                cyerce, with(lead, 100, metaglass, 40, silicon, 40, titanium, 80, thorium, 10),
                aegires, with(copper, 300, metaglass, 80, graphite, 80, titanium, 60, plastanium, 20, surgeAlloy, 5),
                navanax, with(copper, 400, lead, 400, graphite, 100, silicon, 100, metaglass, 120, titanium, 120, thorium, 60, surgeAlloy, 10, phaseFabric, 10),

                alpha, with(copper, 30, lead, 30, graphite, 20, silicon, 20, metaglass, 20),
                beta, with(titanium, 40, thorium, 20),
                gamma, with(plastanium, 20, surgeAlloy, 10, phaseFabric, 10),

                stell, with(beryllium, 20, silicon, 25),
                locus, with(beryllium, 20, graphite, 20, silicon, 20, tungsten, 15),
                precept, with(beryllium, 45, graphite, 25, silicon, 50, tungsten, 50, surgeAlloy, 75, thorium, 40),
                vanquish, with(beryllium, 80, graphite, 50, silicon, 100, tungsten, 120, oxide, 60, surgeAlloy, 125, thorium, 100, phaseFabric, 60),
                conquer, with(beryllium, 250, graphite, 225, silicon, 125, tungsten, 140, oxide, 120, carbide, 240, surgeAlloy, 250, thorium, 240, phaseFabric, 120),

                elude, with(beryllium, 6, graphite, 25, silicon, 35),
                avert, with(beryllium, 24, graphite, 50, silicon, 30, tungsten, 20, oxide, 20),
                obviate, with(beryllium, 48, graphite, 75, silicon, 50, tungsten, 45, carbide, 50, thorium, 40, phaseFabric, 75),
                quell, with(beryllium, 96, graphite, 100, silicon, 140, tungsten, 70, oxide, 60, carbide, 75, surgeAlloy, 60, thorium, 100, phaseFabric, 125),
                disrupt, with(beryllium, 122, graphite, 125, silicon, 155, tungsten, 100, oxide, 120, carbide, 240, surgeAlloy, 120, thorium, 240, phaseFabric, 250),

                merui, with(beryllium, 25, silicon, 35, tungsten, 10),
                cleroi, with(beryllium, 35, graphite, 20, silicon, 25, tungsten, 20, oxide, 20),
                anthicus, with(beryllium, 50, graphite, 25, silicon, 50, tungsten, 65, oxide, 75, thorium, 40),
                tecta, with(beryllium, 100, graphite, 50, silicon, 140, tungsten, 120, oxide, 125, surgeAlloy, 60, thorium, 100, phaseFabric, 125),
                collaris, with(beryllium, 135, graphite, 90, silicon, 175, tungsten, 155, oxide, 250, carbide, 240, surgeAlloy, 120, thorium, 240, phaseFabric, 120),

                evoke, with(beryllium, 50, graphite, 50, silicon, 50),
                incite, with(tungsten, 25, oxide, 25, carbide, 50),
                emanate, with(surgeAlloy, 25, thorium, 25, phaseFabric, 50)
        );

        TowerPathfinder.load();

        content.units().each(type -> {
            type.mineWalls = type.mineFloor = type.targetAir = type.targetGround = false;
            type.payloadCapacity = type.legSplashDamage = type.range = type.maxRange = type.mineRange = 0;

            type.targetFlags = new BlockFlag[] {BlockFlag.core};

            type.aiController = type.flying ? FlyingAI::new : GroundAI::new;
        });

        netServer.admins.addActionFilter(action -> {
            if (action.tile == null) return true;

            if (action.type == ActionType.placeBlock || action.type == ActionType.breakBlock) {
                if (!(proximityCheck(action.tile, action.block) || action.block instanceof ShockMine || action.block instanceof CoreBlock || action.tile.block() instanceof ConstructBlock)) {
                    Call.label(action.player.con, "[scarlet]\uE868", 4f, action.tile.drawx(), action.tile.drawy());
                    return false;
                }
            }

            if ((action.type == ActionType.depositItem || action.type == ActionType.withdrawItem) && action.tile.block() instanceof CoreBlock) {
                Call.label(action.player.con, "[scarlet]\uE868", 4f, action.tile.drawx(), action.tile.drawy());
                return false;
            }

            return true;
        });

        Timer.schedule(() -> state.rules.waveTeam.data().units.each(unit -> {
            var core = unit.closestEnemyCore();
            if (core == null || unit.dst(core) > 80f) return;

            float damage = unit.health / Mathf.sqrt(multiplier / 16) / 4f;
            core.damage(damage, true);

            Call.effect(damage > 50000f ? massiveExplosion : damage > 20000f ? reactorExplosion : blastExplosion, core.x(), core.y(), Math.max(1f, damage / 500f), state.rules.waveTeam.color);
            unit.kill();
        }), 0f, 1f);

        Events.on(WorldLoadEvent.class, event -> multiplier = 1f);
        Events.on(WaveEvent.class, event -> multiplier = Mathf.clamp(((state.wave * state.wave / 3175f) + 0.5f), multiplier, 100f));

        Events.on(PlayEvent.class, event -> {
            state.rules.bannedBlocks.addAll(content.blocks().select(block -> block instanceof UnitBlock));
            state.rules.defaultTeam.rules().buildSpeedMultiplier = 2f;
        });

        Events.on(UnitDestroyEvent.class, event -> {
            if (event.unit.team != state.rules.waveTeam) return;

            var core = state.rules.defaultTeam.core();
            var stacks = drops.get(event.unit.type);

            if (core == null || stacks == null || state.gameOver) return;

            var builder = new StringBuilder();

            for (var stack : stacks) {
                int amount = Mathf.random(stack.amount - stack.amount / 2, stack.amount + stack.amount / 2);

                builder.append("[accent]+").append(amount).append(" [white]").append(getIcon(stack.item)).append("  ");

                Call.transferItemTo(event.unit, stack.item, core.tile.build.acceptStack(stack.item, amount, core), event.unit.x + Mathf.range(8f), event.unit.y + Mathf.range(8f), core);
            }

            Call.label(builder.toString(), 1f, event.unit.x + Mathf.range(4f), event.unit.y + Mathf.range(4f));
        });

        Events.on(UnitSpawnEvent.class, event -> {
            if (event.unit.team != state.rules.waveTeam) return;

            if (!event.unit.isBoss()) event.unit.clearStatuses();

            event.unit.health = event.unit.maxHealth *= multiplier;
            event.unit.damageMultiplier = 0f;
            event.unit.apply(disarmed, Float.MAX_VALUE);
        });

        Timer.schedule(() -> Groups.player.each(player -> Call.infoPopup(player.con, Strings.format("[yellow]\uE86D[accent] Units health multiplier: [#@]@x", trafficLightColor(multiplier), Strings.autoFixed(multiplier, 3)), 1f, 20, 50, 20, 450, 0)), 0f, 1f);
    }
}
