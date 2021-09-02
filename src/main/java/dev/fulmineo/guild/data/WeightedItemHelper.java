package dev.fulmineo.guild.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedItemHelper {
	public static <T extends WeightedItem> List<T> getWeightedItems(List<T> items, int tries){
		Random random = new Random();

		int totalWeight = 0;
		for (T item : items) {
			totalWeight += item.getWeight();
		}

		List<T> result = new ArrayList<>();
		List<Integer> taken = new ArrayList<>();
		int i = 0;
		while (i < tries && taken.size() < items.size()) {
			int weight = random.nextInt(totalWeight) + 1;
			for (int j = 0; j < items.size(); j++) {
				T item = items.get(j);
				if (weight <= item.getWeight()) {
					if (!taken.contains(j)) {
						result.add(item);
						taken.add(j);
					}
					break;
				} else {
					weight -= item.getWeight();
				}
			}
			i++;
		}
		return result;
	}
}
