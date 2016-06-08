package org.hopto.smither.opwatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.NotDirectoryException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class OpWatch extends JavaPlugin implements Listener{
    private static OpWatch instance=null;
    private FileConfiguration config;
    private OpBot bot;
    private ServerIps players=new ServerIps();
    private Sign lastSign;
    private Map<Integer,Sign> signs;
    private Integer maxSignId;
    
    public static OpWatch getInstance(){
        return instance;
    }
    
    // Fired when plugin is first enabled
    @Override
    public void onEnable() {
        reload();
        instance=this;
        bot = OpBot.getInstance();
        Bukkit.getServer().getPluginManager().registerEvents(this , this);
        Bukkit.broadcastMessage("[§9OpWatch§r] Loaded");
        checkConfigExists();
        String verbReport="";
        switch(config.getString("playerVerb").substring(0, 1)){
            case "0":
                verbReport+="Report NO 1st degree links, ";
            break;
            case "1":
                verbReport+="Report only 1st degree links on watch list, ";
            break;
            case "2":
                verbReport+="Report ALL 1st degree links, ";
            break;
            default:
                verbReport+="verb not set default to All 1st degree, ";
            break;
        }
        switch(config.getString("playerVerb").substring(1)){
            case "0":
                verbReport+="NO 2nd degree links.";
            break;
            case "1":
                verbReport+="only 2nd degree links on watch list.";
            break;
            case "2":
                verbReport+="ALL 2nd degree links.";
            break;
            default:
                verbReport+="ALL 2nd degree links.";
            break;
        }
        Bukkit.broadcastMessage("[§9OpWatch§r] "+verbReport);
        bot.report(verbReport);
        File linksSaveFile = new File("plugins/"+this.getName()+"/ServerPlayers.data");
        new File("plugins/"+this.getName()+"/").mkdirs();
        try {
            FileInputStream fis = new FileInputStream(linksSaveFile);
            ObjectInputStream inStream = new ObjectInputStream(fis);
            this.players=(ServerIps)inStream.readObject();
            inStream.close();
            System.out.println("[§9OpWatch§r] §aPlayers info loaded§r");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[OpWatch] Players info load failed");
            Bukkit.broadcastMessage("[§9OpWatch§r] §cPlayers info load failed§r");
            bot.report("[OpWatch] Players info load failed");
        }
        maxSignId=0;
        signs=new HashMap<Integer,Sign>();
        config.set("regexWatchList", config.getString("regexWatchList").replace("\\", "\\\\"));
        config.set("playerWatchList", config.getString("playerWatchList").replace("\\", "\\\\"));
        if (config.get("regexWatchList")==""){
            config.set("regexWatchList","\\.{50}");
        }
    }
    
    // Fired when plugin is disabled
    @Override
    public void onDisable() {
        bot.kill();
        File linksSaveFile = new File("plugins/"+this.getName()+"/ServerPlayers.data");
        new File("plugins/"+this.getName()+"/").mkdirs();
        try {
            linksSaveFile.createNewFile();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            FileOutputStream fos = new FileOutputStream(linksSaveFile);
            ObjectOutputStream outStream = new ObjectOutputStream(fos);
            outStream.writeObject( players );
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
        }
        checkConfigExists();
    }
    
    private void checkConfigExists(){
        File configDir=new File("plugins/"+this.getName());
        if (!configDir.exists()){
            boolean result=false;
            System.out.println("[OpWatch] creating config Directory");
            try{
                configDir.mkdir();
                result = true;
            } 
            catch(SecurityException se){
                se.printStackTrace();
            }        
            if(result) {    
                System.out.println("DIR created");  
            }
        } else if (configDir.isFile()){
            try {
                throw new NotDirectoryException("plugins/"+this.getName());
            } catch (NotDirectoryException e) {
                e.printStackTrace();
            }
        }
        File configFile = new File("plugins/"+this.getName()+"/config.yml");
        if (!configFile.exists()){
            saveDefaultConfig();
        }
    }
    private void reload(){
        reloadConfig();
        config=getConfig();
        return;
    }
    FileConfiguration returnConfig(){
        return config;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent join){
        players.addLinks(join.getPlayer());
        String reply="NOT YET ASSIGNED";
        switch (config.getString("playerVerb").substring(0, 1)){
        case "0":
            break;
        case "1":
            if (!watchListCheck(join.getPlayer())){
                break;
            }else{
                reply=join.getPlayer().getDisplayName()+" joined, 1st degree links: "+players.get1stLinks(join.getPlayer());
                bot.report(reply);
                break;
            }
        case "2":
            reply=join.getPlayer().getDisplayName()+" joined, 1st degree links: "+players.get1stLinks(join.getPlayer());
            if (!reply.equals(join.getPlayer().getDisplayName()+" joined, 1st degree links: ")){
                bot.report(reply);
                break;
            }
        }
        switch (config.getString("playerVerb").substring(1, 2)){
        case "0":
            return;
        case "1":
            if (!watchListCheck(join.getPlayer())){
                return;
            }else{
                reply=join.getPlayer().getDisplayName()+" joined, 2nd degree links: "+players.get2ndLinks(join.getPlayer(),reply);
                bot.report(reply);
                return;
            }
        case "2":
            reply=join.getPlayer().getDisplayName()+" joined, 2nd degree links: "+players.get2ndLinks(join.getPlayer(),reply);
            if (!reply.equals(join.getPlayer().getDisplayName()+" joined, 2nd degree links: ")){
                bot.report(reply);
                return;
            }
        }
    }
    @EventHandler
    public void onSignChange(SignChangeEvent se){
        switch (config.getString("signVerb")){
        case "0":
            return;
        case "1":
            String signLines="";
            for (String line : se.getLines()){
                signLines+=" "+line;
            }
            if (!(regexCheck(signLines) || watchListCheck(se.getPlayer()))){
                return;
            }
        case "2":
            if (Arrays.toString(se.getLines()).equals("[, , , ]")){
                return;
            }
            this.lastSign=(Sign) se.getBlock().getState();
            maxSignId++;
            int id=maxSignId;
            signs.put(id, this.lastSign);
            bot.report("Sign ID="+id+" by "+se.getPlayer().getName()+" : "+Arrays.toString(se.getLines())+" at "+se.getBlock().getX()+", "+se.getBlock().getY()+", "+se.getBlock().getZ());
            return;
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((command.getName().equalsIgnoreCase("OpWatch") || command.getName().equalsIgnoreCase("OpBot")) && sender.hasPermission("OpWatch.admin")){
            switch (args[0].toLowerCase()) {
            case "reload":
            case "reconnect":
                sender.sendMessage("[§9OpWatch§r] §6Reloading Bot§r");
                bot.reconnect();
                sender.sendMessage("[§9OpWatch§r] §2Successfully Reconnected§r");
                return true;
            case "about":
                sender.sendMessage("[§9OpWatch§r]"+about());
                return true;
            default:
                    sender.sendMessage("[§9OpWatch§r] Command not recognised, type §b/OpWatch help§r for options");
                    return false;
            }
        }
        return false;
    }
    String wipeLastSign(){
        lastSign.setLine(0, "Sign wiped");
        lastSign.setLine(1, "By The");
        lastSign.setLine(2, "Minecraft Team");
        lastSign.setLine(3, "");
        if (lastSign.update()){
            return "Sign succesfully wiped";
        } else {
            return "Problem wiping sign";
        }
        
    }
    String wipeSign(Integer id){
        bot.report("Breaking sign id "+id);
        Sign sign=signs.get(id);
        sign.setLine(0, "Sign Wiped");
        sign.setLine(1, "By The");
        sign.setLine(2, "Minecraft Team");
        sign.setLine(3, "");
        if (lastSign.update()){
            return "Sign succesfully wiped";
        } else {
            return "Problem wiping sign";
        }
    }
    
    String aboutPlayer(String playerName){
        String uuid=players.getUuids(playerName);
        String report="";
        if (!uuid.equals("")){
            report+=playerName+"'s uuid: "+uuid+"\n";
            String playerLinks=players.getLinks(playerName);
            if (playerLinks!=""){
                return(report+playerName+" is associated with: "+playerLinks);
            }else{
                return(report+playerName+": No associations found.");
            }
        }else{
            return("Player not found");
        }
    }
    
    String lookUpIP(String player){
        String report="";
        List<Ip> ips=players.lookUpIP(player);
        for (Ip ip:ips){
            report+=ip.toString()+", ";
        }
        report.substring(0, report.length()-2);
        if (report==""){
            report="Name given not valid.";
        }
        report="Ip for "+player+": "+report;
        return report;
    }

    String about(){
        InputStream is;
        try {
            is = new FileInputStream("plugin.yml");
            PluginDescriptionFile pluginDesc;
            try {
                pluginDesc = new PluginDescriptionFile(is);
                return pluginDesc.getFullName()+" V"+pluginDesc.getVersion()+" By "+pluginDesc.getAuthors();
            } catch (InvalidDescriptionException e) {
                e.printStackTrace();
                return "OpWatch Error!";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "OpWatch Error!";
        }
    }
    
    private boolean regexCheck(String message){
        Pattern regex = Pattern.compile(".*?"+config.getString("regexWatchList")+".*?", Pattern.CASE_INSENSITIVE);
        Matcher m=regex.matcher(message);
        if (m.find()){
            return true;
        }else{
            return false;
        }
    }
    
    private boolean watchListCheck(Player player){
        String links=player.getDisplayName()+players.get1stLinks(player);
        links+=","+players.get2ndLinks(player);
        String[] names=links.split("\\s*,\\s*");
        Pattern regex = Pattern.compile(".*?"+config.getString("playerWatchList")+".*?", Pattern.CASE_INSENSITIVE);
        for (String name : names){
            Matcher m=regex.matcher(name);
            if (m.matches()){
                return true;
            }
        }
        return false;       
    }
}
 