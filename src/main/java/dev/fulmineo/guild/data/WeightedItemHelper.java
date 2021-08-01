package dev.fulmineo.guild.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedItemHelper {
	public static <T extends WeightedItem> List<T> getWeightedItems(List<T> items, int itemsToGet){
		Random random = new Random();

		if (items.size() <= itemsToGet){
			return new ArrayList<>(items);
		}
		int totalWeight = 0;
		for (T item : items) {
			totalWeight += item.getWeight();
		}

		List<T> result = new ArrayList<>();

		List<Integer> taken = new ArrayList<>();
		for (int i = 0; i < itemsToGet; i++) {
			int weight = random.nextInt(totalWeight) + 1;
			for (int j = 0; j < items.size(); j++) {
				T item = items.get(j);
				if (weight <= item.getWeight()) {
					result.add(items.get(findNextAvailable(items, j, taken)));
					break;
				} else {
					weight -= item.getWeight();
				}
			}
		}

		return result;
	}

	private static <T extends WeightedItem> int findNextAvailable(List<T> items, int startingIndex, List<Integer> taken) {
		if (taken.contains(startingIndex)) {
			return findNextAvailable(items, startingIndex == items.size()-1 ? 0 : startingIndex + 1, taken);
		} else {
			taken.add(startingIndex);
			return startingIndex;
		}
	}
}
