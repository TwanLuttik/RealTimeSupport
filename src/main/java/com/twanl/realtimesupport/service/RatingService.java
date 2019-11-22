package com.twanl.realtimesupport.service;

import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.util.ConfigManager;
import org.bukkit.entity.Player;

public class RatingService {

    private ConfigManager config = new ConfigManager();
    private Lib lib = new Lib();


    public void rateStaff(Player p, int i) {
        config.setup();

        if (i == 0) {
            lib.updateStaffRating(p, 0);
        } else if (i == 1) {
            lib.updateStaffRating(p, 1);
        } else if (i == 2) {
            lib.updateStaffRating(p, 2);
        } else if (i == 3) {
            lib.updateStaffRating(p, 3);
        } else if (i == 4) {
            lib.updateStaffRating(p, 4);
        }


    }



    public double calculateAVG(Player p) {
        config.setup();

        if (lib.sqlEnabled()) {
            return 0;
        }
        if (!config.getPlayers().isSet("staff." + p.getUniqueId() + ".rating")) {
            return 0;
        }

        if (config.getPlayers().getString("staff." + p.getUniqueId() + ".rating").equals("0#0#0#0#0#")) {
            return 0;
        }

        String[] a = config.getPlayers().getString("staff." + p.getUniqueId() + ".rating").split("#");

        int star_1 = Integer.parseInt(a[0].replace("#",""));
        int star_2 = Integer.parseInt(a[1].replace("#",""));
        int star_3 = Integer.parseInt(a[2].replace("#",""));
        int star_4 = Integer.parseInt(a[3].replace("#",""));
        int star_5 = Integer.parseInt(a[4].replace("#",""));

        return (5*star_5 + 4*star_4 + 3*star_3 + 2*star_2 + 1*star_1) / (star_5+star_4+star_3+star_2+star_1);
    }
}
