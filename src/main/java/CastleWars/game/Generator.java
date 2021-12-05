package CastleWars.game;

import CastleWars.Main;
import CastleWars.logic.*;
import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.storage.CoreBlock;

public class Generator implements Cons<Tiles> {

    Tiles saved;
    int width, height;
    Seq<Tile> cores;

    public Generator() {
        cores = new Seq<>();

        Vars.world.loadMap(Vars.maps.getNextMap(Gamemode.pvp, Vars.state.map), Main.rules.copy());
        saved = Vars.world.tiles;
        width = saved.width;
        height = saved.height * 2 + (Room.ROOM_SIZE * 6);
    }

    public void run() {
        Vars.world.loadGenerator(width, height, this);
        for (Teams.TeamData teamData : Vars.state.teams.active) {
            for (CoreBlock.CoreBuild core : teamData.cores) {
                core.kill();
            }
        }
    }

    @Override
    public void get(Tiles t) {
        for (int x = 0; x < t.width; x++) {
            for (int y = 0; y < t.height; y++) {
                t.set(x, y, new Tile(x, y, Blocks.space, Blocks.air, Blocks.air));
            }
        }
        for (int x = 0; x < t.width; x++) {
            for (int y = 0; y < saved.height; y++) {
                t.getn(x, y).setFloor(saved.getn(x, y).floor());
                t.getn(x, y).setBlock(saved.getn(x, y).block());
                int yy = y + saved.height + (Room.ROOM_SIZE * 6);
                t.getn(x, yy).setFloor(saved.getn(x, y).floor());
                if (!saved.getn(x, y).block().isAir()) {
                    t.getn(x, yy).setBlock(saved.getn(x, y).block());
                }
            }
        }
        postGeneration(t);
    }

    public void postGeneration(Tiles t) {
        for (int x = 0; x < t.width; x++) {
            for (int y = 0; y < saved.height; y++) {
                int yy = y + saved.height + (Room.ROOM_SIZE * 6);
                // Core Build
                if (saved.getn(x, y).floor().equals(Blocks.darkPanel1)) {
                    final int cx = x, cy = y, cyy = yy;
                    Timer.schedule(() -> {
                        t.getn(cx, cy).setNet(Blocks.coreShard, Team.sharded, 0);
                        t.getn(cx, cyy).setNet(Blocks.coreShard, Team.blue, 0);
                    }, 1);

                    cores.add(t.getn(x, y));
                    cores.add(t.getn(x, yy));

                    addCoreRoom(t.getn(x, y), yy);
                }
                // Turret Build
                if (saved.getn(x, y).floor().equals(Blocks.darkPanel3)) {
                    turretGen(t.getn(x, y), yy);
                }
                // Drill room
                if (saved.getn(x, y).floor().equals(Blocks.metalFloorDamaged)) {
                    addDrillRoom(t.getn(x, y), yy);
                }
                // Command room
                if (saved.getn(x, y).floor().equals(Blocks.metalFloor4)) {
                    addTurret(Blocks.commandCenter, t.getn(x, y), yy, 750, 3);
                }
                // Spawners place
                if (saved.getn(x, y).floor().equals(Blocks.darkPanel2)) {
                    UnitRoom.shardedSpawn = t.get(x, y);
                    UnitRoom.blueSpawn = t.get(x, yy);
                }
            }
        }
        // UnitShop in centre
        shopInit();

        for (Room room : Room.rooms) {
            room.spawn(t);
        }
    }

    private void shopInit() {
        int cx = 2, cy = saved.height + 2;
        int Padding = Room.ROOM_SIZE + 2;
        // Ground
        addUnit(UnitTypes.dagger, cx, cy + 2, 100, 0);
        addUnit(UnitTypes.mace, cx + Padding, cy + 2, 150, 1);
        addUnit(UnitTypes.fortress, cx + Padding * 2, cy + 2, 525, 4);
        addUnit(UnitTypes.scepter, cx + Padding * 3, cy + 2, 3000, 20);
        addUnit(UnitTypes.reign, cx + Padding * 4, cy + 2, 8000, 55);
        // Support 
        cx += 1;
        addUnit(UnitTypes.nova, cx + Padding * 5, cy + 2, 100, 0);
        addUnit(UnitTypes.pulsar, cx + Padding * 6, cy + 2, 175, 1);
        addUnit(UnitTypes.quasar, cx + Padding * 7, cy + 2, 500, 4);
        addUnit(UnitTypes.vela, cx + Padding * 8, cy + 2, 3500, 22);
        addUnit(UnitTypes.corvus, cx + Padding * 9, cy + 2, 8500, 65);
        // Naval Support
        cx += 1;
        addUnit(UnitTypes.retusa, cx + Padding * 10, cy + 2, 200, 1);
        addUnit(UnitTypes.oxynoe, cx + Padding * 11, cy + 2, 525, 3);
        addUnit(UnitTypes.cyerce, cx + Padding * 12, cy + 2, 1450, 9);
        addUnit(UnitTypes.aegires, cx + Padding * 13, cy + 2, 4500, 25);
        addUnit(UnitTypes.navanax, cx + Padding * 14, cy + 2, 10000, 65);
        // Spiders
        cx -= 2;
        addUnit(UnitTypes.crawler, cx, cy + 2 + Padding * 2, 70, 0);
        addUnit(UnitTypes.atrax, cx + Padding, cy + 2 + Padding * 2, 175, 1);
        addUnit(UnitTypes.spiroct, cx + Padding * 2, cy + 2 + Padding * 2, 500, 4);
        addUnit(UnitTypes.arkyid, cx + Padding * 3, cy + 2 + Padding * 2, 4000, 24);
        addUnit(UnitTypes.toxopid, cx + Padding * 4, cy + 2 + Padding * 2, 9000, 60);
        // Naval 
        cx += 1;
        addUnit(UnitTypes.risso, cx + Padding * 5, cy + 2 + Padding * 2, 150, 1);
        addUnit(UnitTypes.minke, cx + Padding * 6, cy + 2 + Padding * 2, 350, 2);
        addUnit(UnitTypes.bryde, cx + Padding * 7, cy + 2 + Padding * 2, 1200, 8);
        addUnit(UnitTypes.sei, cx + Padding * 8, cy + 2 + Padding * 2, 3750, 24);
        addUnit(UnitTypes.omura, cx + Padding * 9, cy + 2 + Padding * 2, 10000, 65);
        // Resources
        cx += 1;
        addResources(Items.copper, cx + Padding * 10, cy + 2 + Padding * 2, 100);
        addResources(Items.silicon, cx + Padding * 10, cy + 2 + Padding * 2 + Room.ROOM_SIZE + 2, 125);

        addResources(Items.titanium, cx + Padding * 11, cy + 2 + Padding * 2, 200);
        addResources(Items.pyratite, cx + Padding * 11, cy + 2 + Padding * 2 + Room.ROOM_SIZE + 2, 200);

        addResources(Items.plastanium, cx + Padding * 12, cy + 2 + Padding * 2, 300);
        addEffectRoom(StatusEffects.overdrive, cx + Padding * 12, cy + 2 + Padding * 2 + Room.ROOM_SIZE + 2, 2000, "Overdrive\neffect");

        addResources(Items.phaseFabric, cx + Padding * 13, cy + 2 + Padding * 2, 400);
        addEffectRoom(StatusEffects.boss, cx + Padding * 13, cy + 2 + Padding * 2 + Room.ROOM_SIZE + 2, 3000, "Boss\neffect");

        addResources(Items.surgeAlloy, cx + Padding * 14, cy + 2 + Padding * 2, 500);
        addShieldRoom(cx + Padding * 14, cy + 2 + Padding * 2 + Room.ROOM_SIZE + 2);
    }

    private void turretGen(Tile tile, int yy) {
        if (tile.nearby(1, 1).floor().equals(Blocks.darkPanel6) && tile.nearby(-1, -1).floor().equals(Blocks.darkPanel6) && tile.nearby(-1, 1).floor().equals(Blocks.darkPanel6) && tile.nearby(1, -1).floor().equals(Blocks.darkPanel6)) {
            addTurret(Blocks.foreshadow, tile, yy, 4000, 5);
        }
        else if (tile.nearby(1, 1).floor().equals(Blocks.darkPanel4) && tile.nearby(-1, -1).floor().equals(Blocks.darkPanel4) && tile.nearby(-1, 1).floor().equals(Blocks.darkPanel4) && tile.nearby(1, -1).floor().equals(Blocks.darkPanel4)) {
            addTurret(Blocks.spectre, tile, yy, 3250, 5);
        }
        else if (tile.nearby(1, 1).floor().equals(Blocks.darkPanel6) && tile.nearby(-1, -1).floor().equals(Blocks.darkPanel6)) {
            addTurret(Blocks.meltdown, tile, yy, 2750, 5);
        }
        else if (tile.nearby(1, 1).floor().equals(Blocks.darkPanel4) && tile.nearby(-1, -1).floor().equals(Blocks.darkPanel4)) {
            addTurret(Blocks.cyclone, tile, yy, 1750, 4);
        }
        else if (tile.nearby(0, 1).floor().equals(Blocks.darkPanel4) && tile.nearby(0, -1).floor().equals(Blocks.darkPanel4)) {
            addTurret(Blocks.ripple, tile, yy, 1250, 4);
        }
        else if (tile.nearby(1, 0).floor().equals(Blocks.darkPanel4) && tile.nearby(-1, 0).floor().equals(Blocks.darkPanel4)) {
            addTurret(Blocks.fuse, tile, yy, 1000, 4);
        }
        else if (tile.nearby(0, 1).floor().equals(Blocks.darkPanel4)) {
            addTurret(Blocks.segment, tile, yy, 600, 3);
        }
        else if (tile.nearby(-1, -1).floor().equals(Blocks.darkPanel4)) {
            addTurret(Blocks.lancer, tile, yy, 350, 3);
        }
        else if (tile.nearby(-1, -1).floor().equals(Blocks.darkPanel6)) {
            addTurret(Blocks.salvo, tile, yy, 500, 3);
        }
    }

    private void addTurret(Block block, Tile tile, int yy, int cost, int size) {
        Room.rooms.add(new TurretRoom(Team.sharded, block, tile.x - size / 2, tile.y - size / 2, cost, size));
        Room.rooms.add(new TurretRoom(Team.blue, block, tile.x - size / 2, yy - size / 2, cost, size));
    }

    private void addUnit(UnitType type, int x, int y, int cost, int income) {
        Room.rooms.add(new UnitRoom(type, x, y, type == UnitTypes.crawler ? cost * 20 : cost, income, UnitRoom.Type.Attacker));
        Room.rooms.add(new UnitRoom(type, x, y + Room.ROOM_SIZE + 2, cost, -income, UnitRoom.Type.Defender));
    }

    private void addCoreRoom(Tile tile, int yy) {
        Room.rooms.add(new CoreRoom(Team.sharded, tile.x - 2, tile.y - 2, 5000));
        Room.rooms.add(new CoreRoom(Team.blue, tile.x - 2, yy - 2, 5000));
    }

    private void addDrillRoom(Tile tile, int yy) {
        Room.rooms.add(new DrillRoom(Team.sharded, tile.x - 2, tile.y - 2));
        Room.rooms.add(new DrillRoom(Team.blue, tile.x - 2, yy - 2));
    }

    private void addResources(Item item, int x, int y, int cost) {
        Room.rooms.add(new ResourceRoom(item, x, y, cost, 240));
    }

    private void addEffectRoom(StatusEffect effect, int x, int y, int cost, String name) {
        Room.rooms.add(new EffectRoom(effect, x, y, cost, name));
    }

    private void addShieldRoom(int x, int y) {
        Room.rooms.add(new ShieldRoom(x, y, 5000, "Shield\neffect"));
    }
}
