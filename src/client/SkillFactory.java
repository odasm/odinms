/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.StringUtil;

public class SkillFactory {

    private static Map<Integer, ISkill> skills = new HashMap<Integer, ISkill>();
    private static final Map<Integer, SummonSkillEntry> SummonSkillInformation = new HashMap<Integer, SummonSkillEntry>();
    private static MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Skill.wz"));
    private static MapleData stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Skill.img");

    public static final ISkill getSkill(final int id) {
        if (skills.size() != 0) {
            return skills.get(Integer.valueOf(id));
        }
        System.out.println("[INFO] Carregando SkillFactory :::");
        final MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Skill.wz"));
        final MapleDataDirectoryEntry root = datasource.getRoot();

        int skillid;
        MapleData summon_data;
        SummonSkillEntry sse;

        for (MapleDataFileEntry topDir : root.getFiles()) { // Loop thru jobs
            if (topDir.getName().length() <= 8) {
                for (MapleData data : datasource.getData(topDir.getName())) { // Loop thru each jobs
                    if (data.getName().equals("skill")) {
                        for (MapleData data2 : data) { // Loop thru each jobs
                            if (data2 != null) {
                                skillid = Integer.parseInt(data2.getName());
                                skills.put(skillid, Skill.loadFromData(skillid, data2));

                                summon_data = data2.getChildByPath("summon/attack1/info");
                                if (summon_data != null) {
                                    sse = new SummonSkillEntry();
                                    sse.attackAfter = (short) MapleDataTool.getInt("attackAfter", summon_data, 999999);
                                    sse.type = (byte) MapleDataTool.getInt("type", summon_data, 0);
                                    sse.mobCount = (byte) MapleDataTool.getInt("mobCount", summon_data, 1);
                                    SummonSkillInformation.put(skillid, sse);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String getSkillName(int id) {
        String strId = Integer.toString(id);
        strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
        MapleData skillroot = stringData.getChildByPath(strId);
        if (skillroot != null) {
            return MapleDataTool.getString(skillroot.getChildByPath("name"), "");
        }
        return null;
    }

    public static void cacheSkills() {
        long now = System.currentTimeMillis();
        int skillid = 1000;
        System.out.println("[INFO] Carregando skills..");
        for (MapleData skillData : stringData) {
            skillid = Integer.parseInt(skillData.getName());
            try {
                if (isExist(skillid)) {
                    getSkill(skillid);
                }
            } catch (RuntimeException e) {
                // meh fall through
            }
        }

        System.out.println("[INFO] O carregamento  de " + skills.size() + " skills. Levou " + (System.currentTimeMillis() - now) + " ms.");
    }

    public static boolean isExist(int skillid) throws RuntimeException {
        boolean exist = true;
        ISkill skill = getSkill(skillid);
        if (skill == null) {
            exist = false;
        } else {
            exist = true;
        }
        return exist;
    }

    public static Iterable<ISkill> getAllSkills() {
        return skills.values();
    }

    public static final SummonSkillEntry getSummonData(final int skillid) {
        return SummonSkillInformation.get(skillid);
    }
}
