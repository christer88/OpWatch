package org.hopto.smither.opwatch;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.SASLCapHandler;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

class OpBot {
    private static OpBot instance=null;
    private PircBotX bot=null;
    private Channel channel;
    private String name;
    private String serverAddress;
    private String authPswd;
    private String channelPass;
    private OpWatch plugin;
    
    static OpBot getInstance(){
        if (instance==null){
            
        }
        return instance;
    }
    
    private OpBot() {                                   // Constructor, loads config
        this.plugin=(OpWatch) Bukkit.getPluginManager().getPlugin("OpWatch");
        FileConfiguration config=plugin.returnConfig();
        channel=bot.getUserChannelDao().getChannel(config.getString("channel"));
        name=config.getString("botName");
        serverAddress=config.getString("serverAddress");
        authPswd=config.getString("authPswd");
        channelPass=config.getString("channelPass");
        newBot();
    }
    
    private void newBot(){
        Builder builder = new Configuration.Builder()
                .setAutoReconnect(true)
                .addServer(serverAddress)
                .setName(name) //Nick of the bot. CHANGE IN YOUR CODE
                .setLogin(name) //Login part of hostmask, eg name:login@host
                .setAutoNickChange(true) //Automatically change nick when the current one is in use
                .addAutoJoinChannel(channel.getName(),channelPass) //Join #pircbotx channel on connect
                .addCapHandler(new SASLCapHandler(name, authPswd))
                .addListener(listener);
        bot=new PircBotX(builder.buildConfiguration());
    }
    
    ListenerAdapter listener=new ListenerAdapter() {
        
        @Override
        public void onPrivateMessage(PrivateMessageEvent event) {
            parseMessage(event.getUser().getNick(),event.getUser().getNick(),event.getMessage());
        }
        
        @Override
        public void onMessage(MessageEvent event) {
            parseMessage(channel.getName(),event.getUser().getNick(),event.getMessage());
        }
    };
    
    private void parseMessage(String sender,String sender2,String message){
        if (message.startsWith(",")){
            if(message.toLowerCase().equalsIgnoreCase(",emergencykill")){
                bot.close();
            }else if(message.equalsIgnoreCase(",wipesign")){
                report(plugin.wipeLastSign(),sender);
            }else if(message.toLowerCase().startsWith(",wipesign ")){
                report(plugin.wipeSign(Integer.parseInt(message.substring(10))),sender);
            }else if(message.toLowerCase().startsWith(",aboutplayer ")){
                report(plugin.aboutPlayer(message.substring(13)),sender);
            }else if(message.toLowerCase().startsWith(",lookupip")){
                report(plugin.lookUpIP(message.substring(10)),sender);
            }else if(message.toLowerCase().equalsIgnoreCase(",about")){
                report(plugin.about(),sender);
            }else if(message.toLowerCase().equalsIgnoreCase(",help")){
                report("Available commands: wipesign [ID], lookupIP <PlayerName>, getOfflinePlayer <playerName>, emergencyKill, about",sender);
                report("Available Commands:",sender2);
                report(",wipesign [ID]  -   Will wipe sign[ID] or last sign placed if no ID, and display default message.",sender2);
                report(",aboutPlayer <PlayerName>  -   Will display names of all players linked to <PlayerName>.",sender2);
                report(",lookupIP <PlayerName>  -   Will display the known IPs associated with <playerName>",sender2);
                report(",emergencyKill  -   Kills the IRC bot part of the plugin ('/OpBot reconnect' from within Minecraft to reconnect bot).",sender2);
                report(",about  -   Version and Author info.",sender2);
                report("Commands list ends.",sender2);
            }else{
                report("Command not recognised, sorry "+sender2,sender2);
            }
        }
    }
    
    void report(String message){
        channel.send().message(message);
        return;
    }
    
    void report(String message, String channel){
        bot.getUserChannelDao().getChannel(channel).send().message(message);
        return;
    }
    
    void reconnect(){
        kill();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("OpBot reconnection interrupted!");
        }
        newBot();
    }
    
    void kill(){
        bot.close();
    }

    
}
