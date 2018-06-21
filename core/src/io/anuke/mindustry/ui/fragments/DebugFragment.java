package io.anuke.mindustry.ui.fragments;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import io.anuke.mindustry.content.AmmoTypes;
import io.anuke.mindustry.content.UnitTypes;
import io.anuke.mindustry.content.bullets.TurretBullets;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.bullet.Bullet;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.net.Net;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.entities.EntityGroup;
import io.anuke.ucore.scene.Group;
import io.anuke.ucore.scene.builders.button;
import io.anuke.ucore.scene.builders.label;
import io.anuke.ucore.scene.builders.table;
import io.anuke.ucore.scene.ui.Label;
import io.anuke.ucore.scene.ui.ScrollPane;
import io.anuke.ucore.scene.ui.layout.Table;
import io.anuke.ucore.util.Log;
import io.anuke.ucore.util.Log.LogHandler;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.*;

public class DebugFragment implements Fragment {
    private static StringBuilder log = new StringBuilder();

    static{
        Log.setLogger(new LogHandler(){
            @Override
            public void print(String text, Object... args){
                super.print(text, args);
                if(log.length() < 1000) {
                    log.append(Log.format(text, args));
                    log.append("\n");
                }
            }
        });
    }

    @Override
    public void build(Group parent){

        Player player = players[0];
        new table(){{
           visible(() -> debug);

           abottom().aleft();

           new table("pane"){{
               defaults().fillX().width(100f);

               new label("Debug");
               row();
               new button("noclip", "toggle", () -> noclip = !noclip);
               row();
               new button("team", "toggle", () -> player.toggleTeam());
               row();
               new button("blocks", "toggle", () -> showBlockDebug = !showBlockDebug);
               row();
               new button("effect", () -> {
                   for(int i = 0; i < 20; i ++){
                       Bullet.create(TurretBullets.fireball, player, player.getTeam(), player.x, player.y, Mathf.random(360f));
                   }
               });
               row();
               new button("wave", () -> state.wavetime = 0f);
               row();
               new button("death", () -> player.damage(99999, false));
               row();
               new button("spawnf", () -> {
                   BaseUnit unit = UnitTypes.vtol.create(player.getTeam());
                   unit.set(player.x, player.y);
                   unit.add();
               });
               row();
               new button("spawng", () ->{
                   BaseUnit unit = UnitTypes.scout.create(player.getTeam());
                   unit.set(player.x, player.y);
                   unit.inventory.addAmmo(AmmoTypes.bulletIron);
                   unit.setWave();
                   unit.add();
               });
               row();
           }}.end();

           row();

        }}.end();


        new table(){{
            visible(() -> console);

            atop().aleft();

            new table("pane") {{
                defaults().fillX();

                ScrollPane pane = new ScrollPane(new Label(DebugFragment::debugInfo), "clear");

                add(pane);
                row();
                new button("dump", () -> {
                    try{
                        FileHandle file = Gdx.files.local("packet-dump.txt");
                        file.writeString("--INFO--\n", false);
                        file.writeString(debugInfo(), true);
                        file.writeString("--LOG--\n\n", true);
                        file.writeString(log.toString(), true);
                    }catch (Exception e){
                        ui.showError("Error dumping log.");
                    }
                });
            }}.end();
        }}.end();

        new table(){{
            visible(() -> console);

            atop();

            Table table = new Table("pane");
            table.label(() -> log.toString());

            ScrollPane pane = new ScrollPane(table, "clear");

            get().add(pane);
        }}.end();
    }

    public static void printDebugInfo(){
        Gdx.app.error("Minudstry Info Dump", debugInfo());
    }

    public static String debugInfo(){
        int totalUnits = 0;
        for(EntityGroup<?> group : unitGroups){
            totalUnits += group.size();
        }

        totalUnits += playerGroup.size();

        StringBuilder result = join(
                "net.active: " + Net.active(),
                "net.server: " + Net.server(),
                "net.client: " + Net.client(),
                "state: " + state.getState(),
                "units: " + totalUnits,
                "bullets: " + bulletGroup.size(),
                Net.client() ?
                "chat.open: " + ui.chatfrag.chatOpen() + "\n" +
                "chat.messages: " + ui.chatfrag.getMessagesSize() + "\n" +
                "client.connecting: " + netClient.isConnecting() + "\n" : "",
                "players: " + playerGroup.size(),
                "tiles: " + tileGroup.size(),
                "tiles.sleeping: " + TileEntity.sleepingEntities,
                "time: " + Timers.time(),
                "state.gameover: " + state.gameOver,
                "state: " + state.getState()
        );

        result.append("players: ");

        for(Player player : playerGroup.all()){
            result.append("   name: ");
            result.append(player.name);
            result.append("\n");
            result.append("   id: ");
            result.append(player.id);
            result.append("\n");
            result.append("   cid: ");
            result.append(player.clientid);
            result.append("\n");
            result.append("   dead: ");
            result.append(player.isDead());
            result.append("\n");
            result.append("   pos: ");
            result.append(player.x);
            result.append(", ");
            result.append(player.y);
            result.append("\n");
            result.append("   mech: ");
            result.append(player.mech);
            result.append("\n");
            result.append("   local: ");
            result.append(player.isLocal);
            result.append("\n");

            result.append("\n");
        }

        return result.toString();
    }

    private static StringBuilder join(String... strings){
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(string);
            builder.append("\n");
        }
        return builder;
    }
}
