package net.xolt.sbutils.util;

import java.util.regex.Pattern;

public class RegexFilters {

    // Auto Advert

    public static final Pattern skyblockJoinFilter = Pattern.compile("Welcome [\u00a7_a-zA-Z0-9]+, to Skyblock!");


    // Mentions

    public static final Pattern playerMsgFilter = Pattern.compile("((\u00a7[0-9a-fk-or])*\\[.*\\](\u00a7[0-9a-fk-or])* )*[\u00a7_a-zA-Z0-9]+: .*");


    // Enchant All

    public static final Pattern enchantSingleSuccess = Pattern.compile("The enchantment .* has been applied to your item in hand\\.");
    public static final Pattern enchantAllSuccess = Pattern.compile("All possible enchantments have been applied to the item in your hand\\.");
    public static final Pattern unenchantSuccess = Pattern.compile("The enchantment .* has been removed from your item in hand\\.");
    public static final Pattern enchantError = Pattern.compile("Error: .*");

    
    // Chat Filters

    public static final Pattern tipsFilter = Pattern.compile("\\[Skyblock\\].*");
    public static final Pattern advancementsFilter = Pattern.compile("[\u00a7_ \\[\\]a-zA-Z0-9]+ has made the advancement \\[[ \"?a-zA-Z]+\\]");
    public static final Pattern welcomeFilter = Pattern.compile("Welcome [\u00a7_a-zA-Z0-9]+ to Skyblock!");
    public static final Pattern friendJoinFilter = Pattern.compile("\\[Friends\\] [\u00a7_a-zA-Z0-9]+ has joined [a-zA-Z0-9]+");
    public static final Pattern motdFilter = Pattern.compile("============ Welcome to [\u00a7_a-zA-Z0-9]+'s Island ============|\\+ .*");
    public static final Pattern voteFilter = Pattern.compile("\\[Broadcast\\] [\u00a7_a-zA-Z0-9]+ voted at vote.skyblock.net for .* and 1x Voter Key! /vote");
    public static final Pattern voteRewardFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ was (super |super duper )?lucky and received [a-zA-Z0-9/! ]+! ?");
    public static final Pattern raffleFilter = Pattern.compile("\\[SBRaffle\\].*");
    public static final Pattern cratesFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ has just opened a (Rare|Epic|Legendary) Crate!");
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
    public static final Pattern dpWinnerFilter = Pattern.compile("(Winner #[0-9]+ is .*!)|(Dropping x[0-9]+ .*)");


    // Event Notifier

    public static final Pattern wanderingTraderFilter = Pattern.compile(". A Wandering Trader has been sighted somewhere at spawn offering a limited number of exclusive trades! *");
    public static final Pattern vpLlamaFilter = Pattern.compile("\\[VoteParty\\] A Vote Party Llama has been sighted at the spawn!");
    
    
    // Auto Fix
    
    public static final Pattern fixSuccessFilter = Pattern.compile("You have successfully repaired your: .*\\.");
    public static final Pattern fixFailFilter = Pattern.compile("You cannot type that command for ((([0-9]+) (minutes|minute) )?(([0-9]+) (seconds|second))?|now)\\.|Error: This item cannot be repaired\\.");

    
    // Auto Raffle
    
    public static final Pattern raffleEndFilter = Pattern.compile("\\[SBRaffle\\] Congratulations go to [\u00a7_a-zA-Z0-9]+ for winning [0-9]+ Grass block with [0-9]+ (ticket|tickets)");


    // Staff Detector

    public static final Pattern staffFilter = Pattern.compile("\\[Helper\\].*|\\[Mod\\].*|\\[Dev\\].*|\\[Manager\\].*");
}
