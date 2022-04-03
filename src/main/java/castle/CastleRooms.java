package castle;

import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import castle.components.Bundle;
import castle.components.CastleIcons;
import castle.components.PlayerData;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Liquids;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Iconc;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LiquidTurret;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.units.RepairPoint;
import mindustry.world.consumers.ConsumeType;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class CastleRooms {

    public static final Seq<Room> rooms = new Seq<>();

    public static ObjectMap<Block, Integer> blockCosts;

    public static final int size = 8;
    public static Tile shardedSpawn, blueSpawn;

    public static void load() {
        // TODO сделать формулу для высчета стоимости турели по ее урону, хп, размеру?

        blockCosts = ObjectMap.of(
                Blocks.duo, 100,
                Blocks.scatter, 250,
                Blocks.scorch, 200,
                Blocks.hail, 450,
                Blocks.wave, 300,
                Blocks.lancer, 350,
                Blocks.arc, 150,
                Blocks.parallax, 500,
                Blocks.swarmer, 1250,
                Blocks.salvo, 500,
                Blocks.segment, 750,
                Blocks.tsunami, 850,
                Blocks.fuse, 1500,
                Blocks.ripple, 1500,
                Blocks.cyclone, 1750,
                Blocks.foreshadow, 4000,
                Blocks.spectre, 3000,
                Blocks.meltdown, 3000,

                Blocks.commandCenter, 750,
                Blocks.repairPoint, 300,
                Blocks.repairTurret, 1200
        );
    }

    // TODO прокачиваемые комнаты?

    public static class Room {
        public int x;
        public int y;

        public int startx;
        public int starty;
        public int endx;
        public int endy;

        public int cost;
        public int size;

        public Tile tile;
        public String label;
        public boolean showLabel;

        public Room(int x, int y, int cost, int size) {
            this.x = x;
            this.y = y;

            this.startx = x - size / 2;
            this.starty = y - size / 2;
            this.endx = x + size / 2;
            this.endy = y + size / 2;

            this.cost = cost;
            this.size = size;

            this.tile = world.tile(x, y);
            this.label = "";
            this.showLabel = true;

            rooms.add(this);
        }

        public void update() {}

        public void buy(PlayerData data) {
            data.money -= cost;
        }

        public boolean canBuy(PlayerData data) {
            return data.money >= cost;
        }

        public boolean check(float x, float y) {
            return x > this.startx * tilesize && y > this.starty * tilesize && x < this.endx * tilesize && y < this.endy * tilesize;
        }

        // TODO убрать этот говнокод
        public void spawn() {
            for (int x = 0; x <= size; x++) {
                for (int y = 0; y <= size; y++) {
                    Floor floor = (x == 0 || y == 0 || x == size || y == size ? Blocks.metalFloor5 : Blocks.metalFloor).asFloor();
                    Tile tile = world.tiles.getc(this.startx + x, this.starty + y);
                    if (tile != null) {
                        tile.remove();
                        tile.setFloor(floor);
                    }
                }
            }
        }
    }



    public static class BlockRoom extends Room {
        public Block block;
        public Team team;

        public boolean bought;

        public BlockRoom(Block block, Team team, int x, int y, int cost, int size) {
            super(x, y, cost, size);
            this.block = block;
            this.team = team;

            this.bought = false;

            this.label = CastleIcons.get(block) + " :[white] " + cost;
        }

        public BlockRoom(Block block, Team team, int x, int y, int cost) {
            this(block, team, x, y, cost, block.size + 1);
        }

        @Override
        public void buy(PlayerData data) {
            super.buy(data);
            tile.setNet(block, team, 0);
            tile.build.health(Float.MAX_VALUE);

            Tile source = world.tile(startx, y);

            // TODO внизу говнокод, поменять или убрать
            if (block instanceof ItemTurret turret) {
                Item item = Seq.with(turret.ammoTypes.keys()).random();
                source.setNet(Blocks.itemSource, team, 0);
                source.build.health(Float.MAX_VALUE);
                source.build.configure(item);

                if (turret.consumes.has(ConsumeType.power)) {
                    source.nearby(1).setNet(Blocks.powerSource, team, 0);
                    source.nearby(1).build.health(Float.MAX_VALUE);
                }

                if (turret == Blocks.meltdown) {
                    source.nearby(3).setNet(Blocks.liquidSource, team, 0);
                    source.nearby(3).build.configure(Liquids.cryofluid);
                    source.nearby(3).build.health(Float.MAX_VALUE);
                }

                source.nearby(-1, 0).removeNet();
                source.nearby(-1, 1).removeNet();
                source.nearby(-1, -1).removeNet();
            } else if (block instanceof LiquidTurret) {
                source.setNet(Blocks.liquidSource, team, 0);
                source.build.health(Float.MAX_VALUE);
                source.build.configure(Liquids.cryofluid);

                source.nearby(-1, 0).removeNet();
                source.nearby(-1, 1).removeNet();
                source.nearby(-1, -1).removeNet();
            } else if (block instanceof PowerTurret || block instanceof RepairPoint) {
                source.setNet(Blocks.powerSource, team, 0);
                source.build.health(Float.MAX_VALUE);
            }

            bought = true;
            showLabel = false;
            Groups.player.each(p -> p.team() == team, p -> Call.label(p.con, Bundle.format("events.buy", Bundle.findLocale(p), data.player.coloredName()), 4f, x * tilesize, starty * tilesize));
        }

        @Override
        public boolean canBuy(PlayerData data) {
            return super.canBuy(data) && !bought && data.player.team() == team && world.build(x, y) == null;
        }

        @Override
        public void update() {
            if (bought && world.build(x, y) == null) {
                bought = false;
                showLabel = true;
            }
        }
    }

    public static class MinerRoom extends BlockRoom {
        public ItemStack stack;
        public Interval interval;

        public MinerRoom(ItemStack stack, Team team, int x, int y, int cost) {
            super(Blocks.laserDrill, team, x, y, cost);

            this.stack = stack;
            this.interval = new Interval();

            this.label = "[" + CastleIcons.get(stack.item) + "] " + CastleIcons.get(block) + " :[white] " + cost;
        }

        @Override
        public void update() {
            super.update();

            // TODO прокачка скорости добычи?
            if (bought && interval.get(300f)) {
                Call.effect(Fx.mineHuge, x * tilesize, y * tilesize, 0f, team.color);
                Call.transferItemTo(null, stack.item, stack.amount, x * tilesize, y * tilesize, team.core());
            }
        }
    }



    public static class CoreRoom extends BlockRoom {

        public CoreRoom(Team team, int x, int y, int cost) {
            super(Blocks.coreNucleus, team, x, y, cost, Blocks.coreShard.size + 1);
        }

        @Override
        public void update() {}

        // TODO очень похоже на код из BlockRoom, объединить?
        @Override
        public void buy(PlayerData data) {
            data.money -= cost;

            tile.setNet(block, team, 0);

            bought = true;
            showLabel = false;
            Groups.player.each(p -> p.team() == team, p -> Call.label(p.con, Bundle.format("events.buy", Bundle.findLocale(p), data.player.coloredName()), 4f, x * tilesize, starty * tilesize));
        }

        @Override
        public boolean canBuy(PlayerData data) {
            return data.money >= cost && !bought && data.player.team() == team;
        }

        @Override
        public void spawn() {

        }
    }



    public static class UnitRoom extends Room {

        public enum UnitRoomType {
            attack, defend
        }

        public UnitType unitType;
        public UnitRoomType roomType;

        public int income;

        public UnitRoom(UnitType unitType, UnitRoomType roomType, int income, int x, int y, int cost) {
            super(x, y, cost, 4);
            this.unitType = unitType;
            this.roomType = roomType;
            this.income = income;

            // TODO упростить?
            StringBuilder str = new StringBuilder();

            str.append(" ".repeat(Math.max(0, (String.valueOf(income).length() + String.valueOf(cost).length() + 2) / 2))).append(CastleIcons.get(unitType));

            if (roomType == UnitRoomType.attack) str.append(" [accent]").append(Iconc.modeAttack);
            else str.append(" [scarlet]").append(Iconc.defense);

            str.append("\n[gray]").append(cost).append("\n[white]").append(Iconc.blockPlastaniumCompressor).append(" : ");

            this.label = str.append(income < 0 ? "[crimson]" : income > 0 ? "[lime]+" : "[gray]").append(income).toString();
        }

        @Override
        public void buy(PlayerData data) {
            super.buy(data);
            data.income += income;

            if (roomType == UnitRoomType.attack) {
                unitType.spawn(data.player.team(), (data.player.team() == Team.sharded ? blueSpawn.drawx() : shardedSpawn.drawx()) + Mathf.random(-40, 40), (data.player.team() == Team.sharded ? blueSpawn.drawy() : shardedSpawn.drawy()) + Mathf.random(-40, 40));
            } else if (data.player.team().core() != null) {
                unitType.spawn(data.player.team(), data.player.team().core().x + 40, data.player.team().core().y + Mathf.random(-40, 40));
            }
        }

        @Override
        public boolean canBuy(PlayerData data) {
            return super.canBuy(data) && (income > 0 || data.income + income > 0) && Units.getCap(data.player.team()) > data.player.team().data().unitCount;
        }
    }
}
