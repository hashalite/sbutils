package net.xolt.sbutils.api.response;

import java.util.List;

public class TraderResponse {
    public String entityId;
    public boolean active;
    public List<TraderItem> sellable;
    public List<TraderItem> buyable;

    public static class TraderItem {
        public String item;
        public int maximumAmount;
        public int maximumAmountPerPlayer;
        public double value;
    }
}
