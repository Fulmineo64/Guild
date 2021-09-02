package dev.fulmineo.guild.data;

public class QuestPool {
	public String name;
	public QuestPoolData[] data;

	public String validate() {
		for (QuestPoolData qpd: data) {
			String res = qpd.validate();
			if (res.length() > 0) return res;
		}
		return "";
	}
}
