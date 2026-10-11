package dungeon;

import java.util.*;

public final class GridMap {
    public List<String> rows=new ArrayList<>();
    public int spawnX,spawnY,facing;
    public static final int[] DX={0,1,0,-1},DY={-1,0,1,0};
    public boolean walkable(int x,int y){return y>=0&&y<rows.size()&&x>=0&&x<rows.get(y).length()&&rows.get(y).charAt(x)!='#';}
    public char at(int x,int y){return walkable(x,y)?rows.get(y).charAt(x):'#';}
}
