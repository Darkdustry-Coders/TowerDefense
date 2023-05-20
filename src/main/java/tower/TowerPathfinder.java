package tower;

import mindustry.ai.Pathfinder;
import mindustry.gen.PathTile;
import mindustry.world.Tile;

import static mindustry.Vars.*;
import static tower.Main.*;

public class TowerPathfinder extends Pathfinder {

    public static final int impassable = -1, notPath = 999999;

    public static void load() {
        pathfinder = new TowerPathfinder();
    }

    public TowerPathfinder() {
        costTypes.set(costGround, (team, tile) -> (PathTile.allDeep(tile) || ((PathTile.team(tile) == 0 || PathTile.team(tile) == team) && PathTile.solid(tile))) ? impassable : 1 +
                (PathTile.deep(tile) ? notPath : 0) +
                (PathTile.damages(tile) ? 50 : 0) +
                (PathTile.nearSolid(tile) ? 50 : 0) +
                (PathTile.nearLiquid(tile) ? 10 : 0)
        );

        costTypes.set(costLegs, (team, tile) -> (PathTile.allDeep(tile) || PathTile.legSolid(tile)) ? impassable : 1 +
                (PathTile.deep(tile) ? notPath : 0) +
                (PathTile.damages(tile) ? 50 : 0) +
                (PathTile.nearSolid(tile) ? 10 : 0)
        );

        costTypes.set(costNaval, (team, tile) -> (PathTile.solid(tile) || !PathTile.liquid(tile) ? notPath : 1) +
                (PathTile.damages(tile) ? 50 : 0) +
                (PathTile.nearSolid(tile) ? 10 : 0) +
                (PathTile.nearGround(tile) ? 10 : 0)
        );
    }

    @Override
    public int packTile(Tile tile) {
        boolean nearLiquid = false, nearSolid = false, nearGround = false, allDeep = tile.floor().isDeep();

        for (int i = 0; i < 4; i++) {
            var other = tile.nearby(i);
            if (other == null) continue;

            if (other.floor().isLiquid) nearLiquid = true;
            if (other.solid() || !isPath(other)) nearSolid = true; // !isPath -> nearSolid
            if (!other.floor().isLiquid) nearGround = true;
            if (!other.floor().isDeep()) allDeep = false;
        }

        return PathTile.get(
                0,
                tile.getTeamID(),
                tile.solid(),
                tile.floor().isLiquid,
                tile.staticDarkness() > 1 || tile.floor().solid, // staticDarkness > 1 or floor.solid -> legSolid
                nearLiquid,
                nearGround,
                nearSolid,
                tile.floor().isDeep() || !isPath(tile), // !isPath -> deep
                tile.floor().damageTaken > 0f || !isPath(tile), // !isPath -> damages
                allDeep,
                tile.block().teamPassable
        );
    }
}