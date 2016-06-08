package org.hopto.smither.opwatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

public class ServerIps implements Serializable{
    private static final long serialVersionUID = 1L;
    private HashMap<Ip,List<UUID>> ips=new HashMap<Ip,List<UUID>>();
    private HashMap<UUID,List<Ip>> uuids=new HashMap<UUID,List<Ip>>();
    private HashMap<UUID,List<String>> names=new HashMap<UUID,List<String>>();
   
    String get1stLinks(Player player){
        String reply="";
        Ip ip=new Ip(player.getAddress().getAddress().getAddress());
        if (this.ips.get(ip)!=null){
            for (UUID playerUuid:this.ips.get(ip)){
                for (String name:names.get(playerUuid)){
                    if ((!(reply.contains(name))) && (!name.equalsIgnoreCase(player.getDisplayName()))){
                        reply+=name + ", ";
                    }
                }
            }
            if (reply.length()!=0){
                reply=reply.substring(0, reply.length()-2);
            }
        }else{
            reply="No Known AKAs";
        }
        return reply;
    }
    
    String get2ndLinks(Player player){
        return get2ndLinks(player,"");
    }
    
    String get2ndLinks(Player player, String links1st){
        String reply="";
        Ip ip=new Ip(player.getAddress().getAddress().getAddress());
        if (this.ips.get(ip)!=null){
            for (UUID playerUuid:this.ips.get(ip)){
                if (this.uuids.get(playerUuid)!=null){
                    for (Ip ip2:this.uuids.get(playerUuid)){
                        if (this.ips.get(ip2)!=null){
                            for (UUID playerUuid2:this.ips.get(ip2)){
                                for (String name:names.get(playerUuid2)){
                                    if ((!(reply.contains(name))) && (!name.equalsIgnoreCase(player.getDisplayName())) && (!(links1st.contains(name)))){
                                        reply+=name + ", ";
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (reply.length()!=0){
                reply=reply.substring(0, reply.length()-2);
            }
        }else{
            reply="No Known AKAs";
        }
        return reply;
    }
    void addLinks(Player player){
        Ip ip=new Ip(player.getAddress().getAddress().getAddress());
        UUID uuid=player.getUniqueId();
        String name=player.getDisplayName().toLowerCase();
        this.ips.putIfAbsent(ip,new ArrayList<UUID>());  // create IP in array
        if (!(this.ips.get(ip).contains(uuid))){ // if UUID is not associated with IP
            this.ips.get(ip).add(uuid);  // add UUID to IP
        }
        this.names.putIfAbsent(uuid,new ArrayList<String>());  // create IP in array
        if (!(this.names.get(uuid).contains(name))){ // if UUID is not associated with IP
            this.names.get(uuid).add(name);  // add UUID to IP
        }
        if (this.uuids.get(uuid) != null){   // If player exists in UUID array
            if (!(this.uuids.get(uuid).contains(ip))){ // if UUID is not associated with IP
                this.uuids.get(uuid).add(ip);  // add UUID to IP
            }
        }else{
            this.uuids.put(uuid,new ArrayList<Ip>());  // add UUID to IP
            this.uuids.get(uuid).add(ip);  // add UUID to IP
        }
        ip=new Ip(player.getAddress().getAddress().getAddress());
        uuid=player.getUniqueId();
        return;
    }

    String getLinks(String player){
        String reply="";
        for (UUID uuid : names.keySet()){
            for (String name : names.get(uuid)){
                if (name.equalsIgnoreCase(player)){
                    for (Ip ip : uuids.get(uuid)){
                        for (UUID linkedUuid : ips.get(ip)){
                            for (String linkedName : names.get(linkedUuid)){
                                if ((!(reply.contains(linkedName))) && (!linkedName.equalsIgnoreCase(player))){
                                    reply+=linkedName + ", ";
                                }
                            }
                        }
                    }
                }
            }
        }
        if (reply.length()!=0){
            reply.substring(0, reply.length()-2);
        }
        return reply;
    }
    
    String getUuids(String player){
        player=player.toLowerCase();
        String uuids="";
        for (UUID uuid : names.keySet()){
            if (names.get(uuid).contains(player)){
                uuids+=uuid.toString();
            }
        }
        return uuids;
    }
    
    List<Ip> lookUpIP(String player){
        player=player.toLowerCase();
        List<Ip> ips=new ArrayList<Ip>();
        try{
            for (UUID uuid:names.keySet()){
                if(names.get(uuid).contains(player)){
                    ips.addAll(uuids.get(uuid));
                }
            }
            return ips;
        }catch(IllegalArgumentException e){
            return null;
        }
    }
    
}
