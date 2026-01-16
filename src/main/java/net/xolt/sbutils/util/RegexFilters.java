package net.xolt.sbutils.util;

import java.util.regex.Pattern;

public class RegexFilters {

    // Auto Advert

    public static final Pattern rgbFilter = Pattern.compile("&x&[0-9a-fA-F]&[0-9a-fA-F]&[0-9a-fA-F]&[0-9a-fA-F]&[0-9a-fA-F]&[0-9a-fA-F]");
    public static final Pattern colorCodeFilter = Pattern.compile("&[0-9a-fk-or]");


    // Mentions

    public static final Pattern playerMsgFilter = Pattern.compile("(((\u00a7[0-9a-fk-or])*\\[.*\\](\u00a7[0-9a-fk-or])* )*[\u00a7_a-zA-Z0-9]+: ).*");


    // Enchant All

    public static final Pattern enchantSingleSuccess = Pattern.compile("The enchantment .* has been applied to your item in hand\\.");
    public static final Pattern enchantAllSuccess = Pattern.compile("All possible enchantments have been applied to the item in your hand\\.");
    public static final Pattern unenchantSuccess = Pattern.compile("The enchantment .* has been removed from your item in hand\\.");
    public static final Pattern enchantError = Pattern.compile("Error: .*");
    public static final Pattern noPermission = Pattern.compile("You do not have access to that command\\."); // Also used by Auto Fix

    
    // Chat Filters

    public static final Pattern tipsFilter = Pattern.compile("\\[Skyblock\\].*");
    public static final Pattern advancementsFilter = Pattern.compile("[\u00a7_ \\[\\]a-zA-Z0-9]+ has made the advancement \\[[ \"?a-zA-Z]+\\]");
    public static final Pattern welcomeFilter = Pattern.compile("Welcome [\u00a7_a-zA-Z0-9]+ to Skyblock!");
    public static final Pattern friendJoinFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ has joined [a-zA-Z0-9]+");
    public static final Pattern motdFilter = Pattern.compile("=+ Welcome to [\u00a7_a-zA-Z0-9]+'s Island =+|\\+ .*");
    public static final Pattern voteFilter = Pattern.compile("\\[Vote\\] [\u00a7_a-zA-Z0-9]+ voted at vote.skyblock.net for .*");
    public static final Pattern voteRewardFilter = Pattern.compile("\\[Vote\\] [\u00a7_a-zA-Z0-9]+ was (super |super duper )?lucky and received .*");
    public static final Pattern raffleFilter = Pattern.compile("\\[SBRaffle\\].*");
    public static final Pattern cratesFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ has just opened a (Rare|Epic|Legendary) Crate!");
    public static final Pattern perishedInVoidFilter = Pattern.compile("\\[\u2620\\] [0-9]+ players have perished in the void today\\.");
    public static final Pattern skyChatFilter = Pattern.compile("\\[\u270e\\] .*|The word was [a-z0-9 ]+");
    public static final Pattern islandTitleFilter = Pattern.compile("(§.)?-=(§.)?[\u00a7_a-zA-Z0-9]+'s Island(§.)?=-");
    public static final Pattern islandWelcomeFilter = Pattern.compile("============ Welcome to [\u00a7_a-zA-Z0-9]+'s Island ============");
    

    // Chat Logger
    
    public static final Pattern incomingTransactionFilter = Pattern.compile("Shop transaction completed by .*"); // Also used by Notifier
    public static final Pattern outgoingTransactionFilter = Pattern.compile("Transaction succeeded.");
    public static final Pattern incomingMsgFilter = Pattern.compile("\\[(\\[.+] )?([§_a-zA-Z0-9]+)(@.+)? -> me] .*");
    public static final Pattern outgoingMsgFilter = Pattern.compile("\\[me -> (\\[.+] )?[\u00a7_a-zA-Z0-9]+(@.+)?] .*");
    public static final Pattern visitFilter = Pattern.compile("[\u00a7_a-zA-Z0-9]+ is now visiting your island\\."); // Also used by Notifier
    public static final Pattern dpWinnerFilter = Pattern.compile("(Winner #[0-9]+ is .*)|(Dropping x[0-9]+ .*)");


    // Notifier

    public static final Pattern wanderingTraderFilter = Pattern.compile(". A Wandering Trader has been sighted somewhere at spawn offering a limited number of exclusive trades! *");
    public static final Pattern vpLlamaFilter = Pattern.compile("\\[Vote Party\\] A Vote Party Llama has been sighted at the spawn!");
    
    
    // Auto Fix
    
    public static final Pattern fixSuccessFilter = Pattern.compile("You have successfully repaired your: .*");
    public static final Pattern fixTimeoutFilter = Pattern.compile("You cannot type that command for(( ([0-9]+) (minutes|minute))?( ([0-9]+) (seconds|second))?| now)\\.");
    public static final Pattern fixFailedFilter = Pattern.compile("Error: This item cannot be repaired\\.");

    
    // Auto Raffle
    
    public static final Pattern raffleEndFilter = Pattern.compile("\\[SBRaffle\\] Congratulations .*");


    // Auto Crate

    public static final Pattern voterKeyFilter = Pattern.compile("Click the (Voting|Voter) Crate to use this key");
    public static final Pattern commonKeyFilter = Pattern.compile("Click the Common Crate to use this key");
    public static final Pattern rareKeyFilter = Pattern.compile("Click the Rare Crate to use this key");
    public static final Pattern epicKeyFilter = Pattern.compile("Click the Epic Crate to use this key");
    public static final Pattern legendaryKeyFilter = Pattern.compile("Click the Legendary Crate to use this key");
    public static final Pattern voterCrateFilter = Pattern.compile("Voter|Vote Crate");;
    public static final Pattern commonCrateFilter = Pattern.compile("Common|Common Crate");
    public static final Pattern rareCrateFilter = Pattern.compile("Rare|Rare Crate");
    public static final Pattern epicCrateFilter = Pattern.compile("Epic|Epic Crate");
    public static final Pattern legendaryCrateFilter = Pattern.compile("Legendary|Legendary Crate");


    // Auto Kit

    public static final Pattern kitSuccessFilter = Pattern.compile("Received kit .*");
    public static final Pattern kitFailFilter = Pattern.compile("You can't use that kit again for another(( ([0-9]+) (days|day))?( ([0-9]+) (hours|hour))?( ([0-9]+) (minutes|minute))?( ([0-9]+) (seconds|second))?| now)\\.");
    public static final Pattern kitNoPermsFilter = Pattern.compile("Error: You need the .* permission to use that kit\\.");
    public static final Pattern dailyMenuTitle = Pattern.compile("(Skyblock|Economy) \\| Daily Rewards");
    public static final Pattern dailyTimeLeft = Pattern.compile("((([0-9]+) (days|day),? ?)?(([0-9]+) (hrs|hr),? ?)?(([0-9]+) (mins|min) ?)?(and )?(([0-9]+) (secs|sec))?|now)");
    public static final Pattern dailyError = Pattern.compile("You can't do this just yet, your profile hasn't been loaded, Check back soon");


    // No GMT

    public static final Pattern emailFilter = Pattern.compile("\\[[0-9]+\\] \\[([0-9]+\\/[0-9]+\\/[0-9]+ [0-9]+:[0-9]+)\\] .*");
    public static final Pattern mailGuiFilter = Pattern.compile("Skyblock Mail");
    public static final Pattern mailLoreFilter = Pattern.compile("Sent at ([0-9]+\\/[0-9]+\\/[0-9]+ [0-9]+:[0-9]+ (?i)(am|pm)(?-i)).*");
}
