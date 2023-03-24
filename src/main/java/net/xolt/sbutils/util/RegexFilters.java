package net.xolt.sbutils.util;

import java.util.regex.Pattern;

public class RegexFilters {

    // Auto Advert

    public static final Pattern skyblockTitleFilter = Pattern.compile("\u00a7a\u00a7o\u00a7lSkyblock Survival");
    public static final Pattern economyTitleFilter = Pattern.compile("\u00a7d\u00a7o\u00a7lSkyblock Economy");
    public static final Pattern classicTitleFilter = Pattern.compile("\u00a76\u00a7o\u00a7lSkyblock Classic");

    
    // Chat Filters

    public static final Pattern tipsFilter = Pattern.compile("\\[Skyblock\\].*");
    public static final Pattern advancementsFilter = Pattern.compile("[\u00a7_ \\[\\]a-zA-Z0-9]+ has made the advancement \\[[ \"?a-zA-Z]+\\]");
    public static final Pattern welcomeFilter = Pattern.compile("Welcome [\u00a7_a-zA-Z0-9]+ to Skyblock!");
    public static final Pattern friendJoinFilter = Pattern.compile("\\[Friends\\] [\u00a7_a-zA-Z0-9]+ has joined [a-zA-Z0-9]+");
    public static final Pattern motdFilter = Pattern.compile("============ Welcome to [\u00a7_a-zA-Z0-9]+'s Island ============|\\+ .*");
    public static final Pattern voteFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ voted at vote.skyblock.net for 1x Grass Block and 1x Voter Key! /vote");
    public static final Pattern voteRewardFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ was (super |super duper )?lucky and received [a-zA-Z0-9/! ]+! ?");
    public static final Pattern lotteryFilter = Pattern.compile("\\[SBLottery\\].*");
    public static final Pattern cratesFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ has just opened a (Rare|Epic|Legendary) Crate!");
    public static final Pattern clearLagFilter = Pattern.compile("WARNING Ground items will be removed in [0-9]+ seconds!|\\[SB\\] Removed [0-9]+ Entities!");
    public static final Pattern perishedInVoidFilter = Pattern.compile("\\[\u2620\\] [0-9]+ players have perished in the void today\\.");
    public static final Pattern skyChatFilter = Pattern.compile("\\[\u270e\\] .*|The word was [a-z0-9 ]+");
    

    // Chat Logger
    
    public static final Pattern incomingBuyFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ bought [0-9]+ [ a-zA-Z()]+ from you for [0-9]+ Grass\\.");
    public static final Pattern incomingSellFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ sold [0-9]+ [ a-zA-Z()]+ to you for [0-9]+ Grass\\.");
    public static final Pattern incomingBarterFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ bartered [0-9]+ of their [ a-zA-Z()]+ for [0-9]+ of your [ a-zA-Z()]+\\.");
    public static final Pattern outgoingBuyFilter = Pattern.compile("You bought [0-9]+ [ a-zA-Z()]+ from [\u00a7_a-zA-Z0-9]+ for [0-9]+ Grass\\.");
    public static final Pattern outgoingSellFilter = Pattern.compile("You sold [0-9]+ [ a-zA-Z()]+ to [\u00a7_a-zA-Z0-9]+ for [0-9]+ Grass\\.");
    public static final Pattern outgoingBarterFilter = Pattern.compile("You bartered [0-9]+ of your [ a-zA-Z()]+ for [0-9]+ of [\u00a7_a-zA-Z0-9]+'s [ a-zA-Z()]+\\.");
    public static final Pattern incomingMsgFilter = Pattern.compile("\\[([\u00a7_a-zA-Z0-9]+) -> me\\] .*");
    public static final Pattern outgoingMsgFilter = Pattern.compile("\\[me -> [\u00a7_a-zA-Z0-9]+\\] .*");
    public static final Pattern visitFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ is now visiting your island\\.");
    
    
    // Auto Fix
    
    public static final Pattern fixSuccessFilter = Pattern.compile("You have successfully repaired your: .*\\.");
    public static final Pattern fixFailFilter = Pattern.compile("You cannot type that command for ((([0-9]+) minutes )?(([0-9]+) seconds)?|now)\\.|Error: This item cannot be repaired\\.");

    
    // Auto Lottery
    
    public static final Pattern lotteryEndFilter = Pattern.compile("\\[SBLottery\\] Congratulations go to [\u00a7_a-zA-Z0-9]+ for winning [0-9]+ Grass with [0-9]+ tickets");


    // Auto Crate

    public static final Pattern voterKeyFilter = Pattern.compile("Click the (Voting|Voter) Crate to use this key");
    public static final Pattern commonKeyFilter = Pattern.compile("Click the Common Crate to use this key");
    public static final Pattern voterCrateFilter = Pattern.compile("Voter");
    public static final Pattern commonCrateFilter = Pattern.compile("Common");


    // Staff Detector

    public static final Pattern staffFilter = Pattern.compile("\\[Helper\\].*|\\[Mod\\].*|\\[Dev\\].*|\\[Manager\\].*");
}
