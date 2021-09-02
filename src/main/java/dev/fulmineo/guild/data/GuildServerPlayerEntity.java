package dev.fulmineo.guild.data;

import java.util.List;
import java.util.Map;

public interface GuildServerPlayerEntity {
	public List<Quest> getAcceptedQuests();
	public void acceptQuest(Quest quest);
	public void addAvailableQuest(String professionName, Quest quest);
	public Map<String, List<Quest>> getAvailableQuests();
	public void setAvailableQuests(Map<String, List<Quest>> availableQuests);
	public long getLastQuestGenTime();
	public void setLastQuestGenTime(long time);
	public List<String> getProfessions();
	public List<QuestProfession> getQuestProfessions();
	public Map<String, Integer> getProfessionExp();
	public int getProfessionExp(String professionName);
	public void setProfessionExp(String professionName, int exp);
	public boolean addQuestProfession(String professionName);
	public boolean removeQuestProfession(String professionName);
}
