package fr.yurictf.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import fr.yurictf.YuriCTF;

public class ProtocolLibListener extends PacketAdapter{

    private YuriCTF thePlugin;

    public ProtocolLibListener(YuriCTF plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.SETTINGS);
        thePlugin = plugin;
    }

    public void onPacketReceiving(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.SETTINGS) {
            PacketContainer packet = event.getPacket();
            String lang = packet.getStrings().read(0);

            if (!(lang.equalsIgnoreCase("fr_fr"))){
                CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
                if (player != null){
                    player.playerTranslator.updatePlayerLanguage("english");
                }
            } else {
                CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
                if (player != null){
                    player.playerTranslator.updatePlayerLanguage("french");
                }
            }
        }
    }
}
